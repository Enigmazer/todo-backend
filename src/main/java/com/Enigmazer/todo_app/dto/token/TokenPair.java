package com.Enigmazer.todo_app.dto.token;

/**
 * Represents a pair of tokens used for authentication and session management.
 *
 * @param accessToken  the short-lived token used for accessing resources
 * @param refreshToken the long-lived token used to refresh or renew the access token
 */
public record TokenPair(String accessToken, String refreshToken) {}
