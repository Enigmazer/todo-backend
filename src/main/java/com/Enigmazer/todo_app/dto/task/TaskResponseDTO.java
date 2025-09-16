package com.Enigmazer.todo_app.dto.task;

import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Response object containing only required details of task ")
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private Instant dueDate;
    private boolean isCompleted;
    private boolean isEmailEnabled;
    private boolean isReminderSent;
    private Instant createdAt;
    private Instant lastUpdatedAt;
    private CategoryResponseDTO category;
}
