package com.Enigmazer.todo_app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
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
@Schema(description = "Request body for user login API")
public class UserLoginRequest {

    @Schema(
            description = "User's email address. Must be a valid email format.",
            example = "a@g.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email")
    private String email;

    @Schema(
            description = "User's password. Must be at least 6 characters long.",
            example = "111111",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Size(max = 50, message = "Password can't be this long.")
    private String password;
}
