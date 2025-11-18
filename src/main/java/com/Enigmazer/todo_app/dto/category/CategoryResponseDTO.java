package com.Enigmazer.todo_app.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing only required details of category ")
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private boolean global;
}
