package com.Enigmazer.todo_app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;


@Entity
@Getter
@Setter
@Table(
        name = "task",
        indexes = {
                @Index(name = "idx_task_user_update", columnList = "user_id, lastUpdatedAt"),
                @Index(name = "idx_task_user_update_status", columnList = "user_id, lastUpdatedAt, isCompleted"),
                @Index(name = "idx_task_user_due", columnList = "user_id, dueDate"),
                @Index(name = "idx_task_user_due_status", columnList = "user_id, dueDate, isCompleted")
        }
)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Instant dueDate;

    @Column(nullable = false)
    private boolean isCompleted;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant lastUpdatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @PrePersist
    public void onCreate(){
        this.isCompleted = false;
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.lastUpdatedAt = Instant.now();
    }
}
