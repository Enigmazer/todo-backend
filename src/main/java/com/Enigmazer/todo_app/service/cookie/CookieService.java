package com.Enigmazer.todo_app.service.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class CookieService {

    @Value("${jwt.refreshTokenExpiry}")
    private Duration refreshTokenExpiration;

    @Value("${server.servlet.context-path:}")
    private String contextPath;


    public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(contextPath + "/auth/")
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .build();
    }

    public ResponseCookie generateLogoutCookies() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path(contextPath + "/auth/")
                .maxAge(0)
                .sameSite("None")
                .build();
    }
}
