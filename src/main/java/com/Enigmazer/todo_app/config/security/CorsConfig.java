package com.Enigmazer.todo_app.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Global configuration for Cross-Origin Resource Sharing (CORS).
 * <p>
 * This allows the frontend (hosted on a different origin) to interact with backend APIs
 * by enabling specific HTTP methods, headers, and credentials.
 *
 * <p><strong>Allowed Methods:</strong> GET, POST, PUT, PATCH, DELETE, OPTIONS<br>
 * <strong>Allowed Headers:</strong> Authorization, Content-Type, etc.<br>
 * <strong>Exposed Headers:</strong> Authorization, Access-Control-Allow-Origin, etc.<br>
 * <strong>Credentials:</strong> Allowed (cookies, auth headers)<br>
 * <strong>Max Age:</strong> 3600 seconds (1 hour)
 */
@Configuration
@Profile("!test")
@Slf4j
public class CorsConfig {

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * Registers a CORS configuration bean to allow frontend access to backend APIs.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        log.debug("Setting up CORS configuration for origin: {}", frontendUrl);

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        configuration.setExposedHeaders(List.of(
                "Authorization",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // cache for 1h

        log.debug("CORS allowed methods: {}", configuration.getAllowedMethods());
        log.debug("CORS allowed headers: {}", configuration.getAllowedHeaders());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        log.info("CORS configuration is set successfully for origin: {}", frontendUrl);
        return source;
    }
}
