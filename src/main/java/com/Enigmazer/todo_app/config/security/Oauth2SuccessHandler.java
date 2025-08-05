package com.Enigmazer.todo_app.config.security;

import com.Enigmazer.todo_app.dto.token.TokenPair;
import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.cookie.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Custom OAuth2 authentication success handler.
 * <p>
 * This handler is triggered when an OAuth2 login is successful.
 * It performs the following tasks:
 * <ul>
 *     <li>Extracts the authenticated user's email from the {@link OAuth2User}.</li>
 *     <li>Generates access and refresh JWT tokens using the {@link JWTService}.</li>
 *     <li>Sets the refresh token in an HttpOnly cookie using {@link CookieService}.</li>
 *     <li>Sends the access token to the frontend via redirect URL.</li>
 *     <li>If any error occurs during token generation or redirection, it redirects with an error message.</li>
 * </ul>
 */

@Component
@Slf4j
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2FailureRedirectHandler authErrorHandler;
    private final JWTService jwtService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                               HttpServletResponse response,
                               Authentication authentication) {
        try {
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
            String email = oAuth2User.getName();

            // optional paranoid check
            if (email == null) {
                authErrorHandler.redirectToFrontendWithError(response, "auth_error=no_email_found");
                return;
            }

            // Generate JWT token
            TokenPair tokenPair = jwtService.generateTokens(email);

//            // Set Refresh Token in Secure HttpOnly Cookie
//            ResponseCookie refreshCookie = cookieService.buildRefreshTokenCookie(tokenPair.refreshToken());
//            response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

            // Redirect to frontend with access token only
            String redirectUrl = frontendUrl + "/oauth-callback?accessToken=" + tokenPair.accessToken();
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            try {
                String message = e.getMessage();
                String encodedError = message != null ? URLEncoder.encode(message, StandardCharsets.UTF_8) : "unknown";
                authErrorHandler.redirectToFrontendWithError(response, "auth_error=" + encodedError);
            } catch (IOException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }
}
