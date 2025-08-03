package com.Enigmazer.todo_app.dto.category;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Response object containing only required details of category ")
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private boolean isGlobal;
}
