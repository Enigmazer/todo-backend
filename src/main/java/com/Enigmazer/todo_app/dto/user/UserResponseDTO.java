package com.Enigmazer.todo_app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Response object containing public user info")
public class UserResponseDTO {

    private String name;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

}
