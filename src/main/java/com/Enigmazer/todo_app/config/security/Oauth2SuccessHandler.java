package com.Enigmazer.todo_app.config.security;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.cookie.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTService jwtService;
    private final CookieService cookieService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                               HttpServletResponse response,
                               Authentication authentication) throws IOException {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getAttribute("email");

            if (email == null) {
                response.sendRedirect(frontendUrl + "/oauth-callback?auth_error=authentication_failed");
                return;
            }

            TokenPair tokenPair = jwtService.generateTokens(email);
            ResponseCookie refreshCookie = cookieService.generateRefreshTokenCookie(tokenPair.refreshToken());

            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/oauth-callback" + "#at=" + tokenPair.accessToken());

        } catch (Exception e) {
            log.error("OAuth2 authentication failed", e);
            response.sendRedirect(frontendUrl + "/oauth-callback?auth_error=authentication_failed");
        }
    }
}
