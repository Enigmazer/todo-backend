package com.Enigmazer.todo_app.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Schema(description = "Request object for creating or updating a task")
public class TaskCreationOrUpdateRequest {

    @Schema(
            description = "Title of the task. This field is required.",
            example = "Bill Payment",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Title can't be empty")
    private String title;

    @Schema(
            description = "Detailed description of the task. Optional field.",
            example = "I have to pay my internet bill"
    )
    @Lob
    @Size(max = 2000, message = "Description can't be longer than 2000 characters")
    private String description;

    @Schema(
            description = "Due date for the task in yyyy-MM-dd format. Optional but recommended.",
            example = "2025-01-31",
            type = "string",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "DueDate can't be empty")
    private Instant dueDate;

    @Schema(
            description = "Id of the category you want to set.",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Please select a category")
    private Long categoryId;
}
