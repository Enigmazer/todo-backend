package com.Enigmazer.todo_app.service.cookie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class CookieServiceTest {

    private CookieService cookieService;

    @BeforeEach
    void setUp() {
        cookieService = new CookieService();
    }

    @Test
    void buildRefreshTokenCookie_ShouldBuildCookieWithCorrectProperties() {
        String refreshToken = "dummyToken";

        ResponseCookie cookie = cookieService.buildRefreshTokenCookie(refreshToken);

        assertThat(cookie.getName()).isEqualTo("refreshToken");
        assertThat(cookie.getValue()).isEqualTo(refreshToken);
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getSameSite()).isEqualTo("None");
        assertThat(cookie.isPartitioned()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void clearRefreshTokenCookie_ShouldBuildCookieThatClearsValue() {
        ResponseCookie cookie = cookieService.clearRefreshTokenCookie();

        assertThat(cookie.getName()).isEqualTo("refreshToken");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.isSecure()).isTrue();
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isPartitioned()).isTrue();
        assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofSeconds(0));
    }

}
