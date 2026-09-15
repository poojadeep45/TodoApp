package com.app.todoapp.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank
    private String token;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{15,}$",
            message = "Password must be at least 15 characters and include an uppercase letter, a lowercase letter, a number, and a special character"
    )
    private String newPassword;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
}