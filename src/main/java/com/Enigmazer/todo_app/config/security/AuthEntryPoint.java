package com.Enigmazer.todo_app.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point for handling unauthorized access attempts.
 * Returns a JSON response with a 401 status when unauthenticated requests hit
 * protected endpoints.
 */
@Component
@Slf4j
public class AuthEntryPoint implements AuthenticationEntryPoint {
    /**
     * Handles unauthorized access attempts by returning a 401 JSON response.
     * This is triggered when an unauthenticated user tries to access a protected resource.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param authException the exception that caused the authentication to fail
     * @throws IOException in case of response write issues
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.warn("Unauthorized access attempt to '{}': {}", request.getRequestURI(), authException.getMessage());
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" +
                authException.getMessage() + "\"}");
    }
}
