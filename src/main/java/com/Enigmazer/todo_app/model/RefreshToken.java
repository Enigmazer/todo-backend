package com.Enigmazer.todo_app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents a refresh token used for maintaining user authentication sessions.
 * This entity is used to generate new access tokens without requiring users to log in again.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    /**
     * The unique token string used for refresh token validation.
     * This is a cryptographically secure random string.
     */
    @Id
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * The user associated with this refresh token.
     * This establishes a many-to-one relationship with the User entity.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * The timestamp when this refresh token was created.
     */
    @Column(nullable = false)
    private Instant createdAt;

    /**
     * The timestamp when this refresh token expires.
     * After this time, the token can no longer be used to obtain a new access token.
     */
    @Column(nullable = false)
    private Instant expiry;

    /**
     * Flag indicating whether this refresh token has been revoked.
     * If true, the token cannot be used to obtain new access tokens.
     */
    @Column(nullable = false)
    private boolean revoked = false;

    /**
     * Constructs a new RefreshToken with the specified parameters.
     *
     * @param token     The unique token string
     * @param user      The user associated with this token
     * @param createdAt The creation timestamp of the token
     * @param expiry    The expiration timestamp of the token
     */
    public RefreshToken(String token, User user, Instant createdAt, Instant expiry) {
        this.token = token;
        this.user = user;
        this.createdAt = createdAt;
        this.expiry = expiry;
    }
}

