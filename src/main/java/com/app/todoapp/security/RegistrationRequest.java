package com.app.todoapp.security;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrationRequest {

    @NotBlank(message = "Username cannot be blank")
    @jakarta.validation.constraints.Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{15,}$",
            message = "Password must be at least 15 characters and include an uppercase letter, a lowercase letter, a number, and a special character"
    )
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;
}