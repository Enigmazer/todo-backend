package com.Enigmazer.todo_app.service.cookie;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service for managing HTTP-only secure cookies used for authentication.
 * Handles creation and invalidation of refresh token cookies with proper security settings.
 */
@Service
public class CookieService {

    /**
     * Builds a secure, HTTP-only cookie containing the refresh token.
     * The cookie is configured with the following security settings:
     * <ul>
     *     <li>HTTP-only to prevent JavaScript access</li>
     *     <li>Secure flag for HTTPS-only transmission</li>
     *     <li>SameSite=None for cross-site usage</li>
     *     <li>Partitioned for cross-site cookie access in Chrome</li>
     *     <li>7-day expiration</li>
     * </ul>
     *
     * @param refreshToken The JWT refresh token to store in the cookie
     * @return A configured ResponseCookie instance
     */
    public ResponseCookie buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .partitioned(true)
                .maxAge(Duration.ofDays(7))
                .build();
    }

    /**
     * Builds a cookie that clears the refresh token from the client.
     * This is done by setting an empty value and immediate expiration.
     * Maintains the same security settings as the original cookie.
     *
     * @return A ResponseCookie that clears the refresh token
     */
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
