package com.Enigmazer.todo_app.controller;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.cookie.CookieService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JWTService jwtService;
    private final CookieService cookieService;

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
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
        TokenPair tokenPair = jwtService.refreshBothTokens(refreshToken);

        ResponseCookie refreshCookie = cookieService.buildRefreshTokenCookie(tokenPair.refreshToken());

        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.ok(Map.of("accessToken", tokenPair.accessToken()));
    }

}