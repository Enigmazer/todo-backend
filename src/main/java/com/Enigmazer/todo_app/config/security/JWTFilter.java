package com.Enigmazer.todo_app.config.security;

import com.Enigmazer.todo_app.service.JWTService;
import com.Enigmazer.todo_app.service.user.UserDetailsServiceImpl;
import io.micrometer.common.lang.NonNullApi;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JWTFilter intercepts each incoming HTTP request, checks for a Bearer token,
 * validates the token using {@link JWTService}, and sets the Spring SecurityContext
 * with the authenticated user if valid.
 * <p>
 * This allows stateless authentication across the application.
 */
@Component
@Slf4j
@RequiredArgsConstructor
@NonNullApi
public class JWTFilter extends OncePerRequestFilter {

    private final JWTService jwtService;

    /**
     * Intercepts each request, extracts access token from Authorization
     * header, and authenticates the user if the token is valid.
     *
     * @param request     incoming HTTP request
     * @param response    outgoing HTTP response
     * @param filterChain remaining filters in the chain
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // Only process requests that have access token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                log.debug("Extracting email from access token.");
                email = jwtService.extractEmail(token);
                log.debug("Email is successfully extracted from the access token: {}", email);
            } catch (Exception e) {
                log.warn("Invalid access token: {}", e.getMessage());
                // Don't return here - let Spring Security handle the authorization
            }
        }

        // If we have a valid email and no existing authentication, set up the security context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                log.debug("Authenticating user: {}", email);
                Set<String> role = jwtService.extractRole(token);
                Set<GrantedAuthority> authorities = role.stream()
                        .map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
                log.debug("Mapped role {} to user {}", role, email);
                if (jwtService.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Authentication successful for user: {}", email);
                } else {
                    log.warn("Access token validation failed for user: {}", email);
                    // Don't return here - let Spring Security handle the authorization
                }
            } catch (Exception e) {
                log.error("User authentication failed for email {}: {}", email, e.getMessage());
                // Don't return here - let Spring Security handle the authorization
            }
        }

        // Continue the filter chain let Spring Security decide if authentication is required
        filterChain.doFilter(request, response);
    }

}
