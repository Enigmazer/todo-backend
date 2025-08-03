package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.PasswordChangeRequest;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.dto.user.UserResponseDTO;
import com.Enigmazer.todo_app.service.cookie.CookieService;
import com.Enigmazer.todo_app.service.user.UserDetailsServiceImpl;
import com.Enigmazer.todo_app.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * UserController handles all user related endpoints.
 * <p>
 * All endpoints excluding /login are secured using JWT
 * and all Are managed by {@link UserDetailsServiceImpl}
 * <p>
 * user controller handles operations like:
 * <ul>
 *     <li>Logging in a user</li>
 *     <li>Logging out a user</li>
 *     <li>Checking, is password available</li>
 *     <li>Setting a password</li>
 *     <li>Changing the password</li>
 *     <li>Returning a self object to display profile details</li>
 * </ul>
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final CookieService cookieService;

    /**
     * login method takes a login request dto and verifies the user
     * details, and then return a jwt token if user verified
     *
     * @param user contains email & password of the user who is trying to log in
     * @return a HashMap containing jwt token
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody UserLoginRequest user) {
        TokenPair tokenPair = userService.verify(user);

        log.debug("Token pair received refresh token is {}", tokenPair.refreshToken());
        ResponseCookie refreshTokenCookie = cookieService.buildRefreshTokenCookie(tokenPair.refreshToken());

        log.debug("Token pair from refresh cookie is {}", refreshTokenCookie.toString());
        log.info("User with email {} is successfully logged in", user.getEmail());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of("accessToken", tokenPair.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        log.info("User log out request.");
        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // Remove "Bearer " prefix
        }
        
        userService.logout(token);

        ResponseCookie refreshTokenCookie = cookieService.clearRefreshTokenCookie();
        log.info("Refresh Token has been removed from cookie.");

        log.info("user successfully logged out");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body("Logged out successfully.");
    }
    /**
     * check if the user's account have a password or not
     *
     * @return true if account have password set and false if it doesn't
     */
    @GetMapping("/password-check")
    public ResponseEntity<Boolean> isPasswordAvailable(){
        Boolean passwordAvailable = userService.isPasswordAvailable();
        log.info("check successful and result is: {}", passwordAvailable);
        return ResponseEntity.ok(passwordAvailable);
    }

    /**
     * set a password to the account only if it doesn't have one
     *
     * @param request contains email and a password they want to set
     */
    @PostMapping("/set-password")
    public ResponseEntity<String> setAPassword(@Valid @RequestBody UserLoginRequest request) {
        log.info("password set request for: {}", request.getEmail());
        userService.setAPassword(request);
        return ResponseEntity.ok("Password set successfully");
    }

    /**
     * change the password of the account
     *
     * @param request   contains email, old and new passwords
     */
    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        log.info("password change request for: {}", request.getEmail());
        userService.changePassword(request);
        return ResponseEntity.ok("Password changed successfully");
    }

    /**
     * returns an object containing public
     * details of the logged-in user
     *
     * @return details of logged-in user
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(){
        UserResponseDTO user = userService.getCurrentUser();
        log.info("current user returned successfully");
        return ResponseEntity.ok(user);
    }

}
