package com.Enigmazer.todo_app.dto.task;

import com.Enigmazer.todo_app.dto.category.CategoryResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Response object containing only required details of task ")
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private boolean isCompleted;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private CategoryResponseDTO category;
}
