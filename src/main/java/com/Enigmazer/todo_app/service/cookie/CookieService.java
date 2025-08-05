package com.Enigmazer.todo_app.service.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    public ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true) // Prevent JS access
                .secure(true)   // true in production (HTTPS required)
                .path("/")
                .sameSite("None") // "None" because using cross-site
                .partitioned(true)
                .maxAge(Duration.ofDays(7))
                .build();
    }

    public ResponseCookie clearRefreshTokenCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true) // match production setting
                .path("/")
                .partitioned(true)
                .maxAge(0)
                .build();
    }
}
