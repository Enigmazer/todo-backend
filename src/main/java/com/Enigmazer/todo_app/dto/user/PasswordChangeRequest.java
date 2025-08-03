package com.Enigmazer.todo_app.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body for password change api")
public class PasswordChangeRequest {

    @Schema(
            description = "User's email address. Must be a valid email format.",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email")
    private String email;

    @Schema(
            description = "User's password. Must be at least 6 characters long.",
            example = "MyS3cret!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Please enter old password")
    @Size(min = 6, message = "Password must be at least 6 characters long.")
    @Size(max = 50, message = "Password can't be this long.")
    private String oldPassword;

    @Schema(
            description = "User's password. Must be at least 6 characters long.",
            example = "MyS3cret!",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Please enter new password")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    @Size(max = 50, message = "Password can't be this long.")
    private String newPassword;
}

