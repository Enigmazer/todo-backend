package com.Enigmazer.todo_app.config.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles redirection to the frontend application when an OAuth2 authentication error occurs.
 * Appends the error details as query parameters to the frontend's OAuth callback URL.
 */
@Component
@Slf4j
public class OAuth2FailureRedirectHandler {
    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Redirects to frontend Oauth callback endpoint with an error.
     * This is typically used when an Oauth authentication failure occurs.
     * @param response The HTTP response
     * @param errorParam the error query string
     * @throws IOException If an input or output exception occurs
     */
    void redirectToFrontendWithError(HttpServletResponse response, String errorParam) throws IOException {
        log.warn("redirecting to frontend with Oauth error : {}", errorParam);
        String redirectUrl = frontendUrl + "/oauth-callback?" + errorParam;
        response.sendRedirect(redirectUrl);
    }
}
