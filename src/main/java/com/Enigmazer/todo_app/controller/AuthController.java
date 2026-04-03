package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.dto.user.UserLoginRequest;
import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.auth.AuthService;
import com.Enigmazer.todo_app.service.cookie.CookieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final JWTService jwtService;
    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<Map<String,String>> login(@Valid @RequestBody UserLoginRequest user) {
        TokenPair tokenPair = authService.login(user);

        ResponseCookie refreshTokenCookie = cookieService.generateRefreshTokenCookie(tokenPair.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(Map.of("access token", tokenPair.accessToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if (refreshToken!=null) {
            authService.logout(refreshToken);
        }

        ResponseCookie logoutCookie = cookieService.generateLogoutCookies();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, logoutCookie.toString()).build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody UserLoginRequest request) {
        log.info("password change request for: {}", request.getEmail());
        authService.changePassword(request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> renewTokens(
            @CookieValue(name = "refreshToken", required = false) String refreshToken) {

        if(refreshToken == null || refreshToken.isBlank()){
            throw new IllegalArgumentException("No refresh token found.");
        }

        // Generate JWT token
        TokenPair tokenPair = jwtService.renewTokens(refreshToken);

        ResponseCookie refreshCookie = cookieService.generateRefreshTokenCookie(tokenPair.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(Map.of("access token", tokenPair.accessToken()));
    }

}