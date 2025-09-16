package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.auth.AuthService;
import com.Enigmazer.todo_app.service.cookie.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * AuthController handles authentication related operations.
 * <p>
 * auth controller handles operations like:
 * <ul>
 *     <li>Logging in a user</li>
 *     <li>Logging out a user</li>
 *     <li>Set or change the password</li>
 *     <li>Refresh access and refresh tokens</li>
 *     <li>Store refresh token for oauth2 login</li>
 * </ul>
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JWTService jwtService;
    private final AuthService authService;
    private final CookieService cookieService;


    /**
     * login method takes a login request dto and verifies the user
     * details, and then return an access token and set a refresh token
     * in cookies if user verified
     *
     * @param user contains email & password of the user who is trying to log in
     * @return an access token
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody UserLoginRequest user) {
        TokenPair tokenPair = authService.login(user);

        log.debug("Token pair received refresh token is {}", tokenPair.refreshToken());
        ResponseCookie refreshTokenCookie = cookieService.buildRefreshTokenCookie(tokenPair.refreshToken());

        log.debug("Token pair from refresh cookie is {}", refreshTokenCookie.toString());
        log.info("User with email {} is successfully logged in", user.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of("accessToken", tokenPair.accessToken()));
    }

    /**
     * logout the user by invalidating the tokens
     *
     * @param request incoming http request
     * @return success message and set a blank refresh token in cookie
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.info("User log out request.");
        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remove "Bearer " prefix
        }
        authService.logout(token);
        ResponseCookie refreshTokenCookie = cookieService.clearRefreshTokenCookie();
        log.info("Refresh Token has been removed from cookie.");
        log.info("user successfully logged out");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body("Logged out successfully.");
    }

    /**
     * change or set the password for the account
     *
     * @param request contains email and password
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody UserLoginRequest request) {
        log.info("password change request for: {}", request.getEmail());
        authService.changePassword(request);
        return ResponseEntity.ok("Password changed successfully");
    }

    /**
     * these methods retrieve the refresh token from
     * cookie and if available then validates it and set a new
     * refresh token in cookie and return a new access token
     *
     * @param request the incoming http request
     * @return an access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> renewTokens(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String refreshToken = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Refresh token missing"));
        }

        // Generate JWT token
        TokenPair tokenPair = jwtService.renewTokens(refreshToken);

        ResponseCookie refreshCookie = cookieService.buildRefreshTokenCookie(tokenPair.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("accessToken", tokenPair.accessToken()));
    }

    /**
     * used for storing refresh token
     * when logged in using oauth
     *
     * @return refresh token
     */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/session")
    public ResponseEntity<Map<String, String>> setRefreshTokenCookie() {

        String refreshToken = authService.getLatestRefreshTokenForUser();
        ResponseCookie refreshCookie = cookieService.buildRefreshTokenCookie(refreshToken);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .build();
    }

}