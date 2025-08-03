package com.Enigmazer.todo_app.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Redirects to frontend with the error
 * if Oauth authentication is failed
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class Oauth2FailureHandler implements AuthenticationFailureHandler {

    private final OAuth2FailureRedirectHandler authErrorHandler;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException{
        String message = exception.getMessage();
        log.error("Oauth authentication failure : {}",message);
        String encodedError = message != null ? URLEncoder.encode(message, StandardCharsets.UTF_8) : "unknown";
        authErrorHandler.redirectToFrontendWithError(response, "auth_error=" + encodedError);
    }
}
