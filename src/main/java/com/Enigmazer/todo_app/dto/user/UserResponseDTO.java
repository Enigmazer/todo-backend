package com.Enigmazer.todo_app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing public user info")
public class UserResponseDTO {

    private String name;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

}
