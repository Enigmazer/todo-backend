package com.Enigmazer.todo_app.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @Column(nullable = false, unique = true)
    private String token;  // UUID or secure random string

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiry;

    @Column(nullable = false)
    private boolean revoked = false;

    public RefreshToken(String token, User user, Instant createdAt, Instant expiry) {
        this.token = token;
        this.user = user;
        this.createdAt = createdAt;
        this.expiry = expiry;
    }
}

