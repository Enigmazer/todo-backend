package com.Enigmazer.todo_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * The main entry point for the Todo Application.
 * <p>
 * This class serves as the configuration class and bootstrap class for the Spring Boot application.
 * It enables various Spring Boot features and configurations needed for the application to run.
 *
 * <p>Key features enabled:
 * <ul>
 *     <li>{@code @SpringBootApplication}: Enables auto-configuration, component scanning, and property support</li>
 *     <li>{@code @EnableScheduling}: Enables Spring's scheduled task execution capability</li>
 *     <li>{@code @EnableSpringDataWebSupport}: Enables Spring Data web support with DTO-based pagination serialization</li>
 * </ul>
 */
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
@SpringBootApplication
@EnableScheduling
public class TodoAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(TodoAppApplication.class, args);
    }
}
