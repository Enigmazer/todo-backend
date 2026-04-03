package com.Enigmazer.todo_app.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryCreationRequest {

    @Schema(
            description = "Name of the category.",
            example = "Personal",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Category name can't be empty")
    @Size(max = 25, message = "!Too long name for a category.")
    private String name;

}
