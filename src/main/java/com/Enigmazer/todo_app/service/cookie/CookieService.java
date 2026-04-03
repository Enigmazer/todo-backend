package com.Enigmazer.todo_app.service.cookie;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class CookieService {

    @Value("${jwt.accessTokenExpiry}")
    private Duration accessTokenExpiration;

    @Value("${jwt.refreshTokenExpiry}")
    private Duration refreshTokenExpiration;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    public ResponseCookie generateAccessTokenCookie(String token) {
        return ResponseCookie.from("accessToken", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(accessTokenExpiration)
                .sameSite("None") // Required for cross-domain
                .build();
    }

    public ResponseCookie generateRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(contextPath + "/auth/")
                .maxAge(refreshTokenExpiration)
                .sameSite("None")
                .build();
    }

    public List<ResponseCookie> generateLogoutCookies() {
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true).secure(true).path("/").maxAge(0).sameSite("None").build();

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true).secure(true).path(contextPath + "/auth/").maxAge(0).sameSite("None").build();

        return List.of(accessCookie, refreshCookie);
    }
}
