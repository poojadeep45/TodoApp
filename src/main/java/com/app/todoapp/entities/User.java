package com.app.todoapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Enter a valid email address")
    @Column(unique = true)
    private String email;

    // Always stored BCrypt-hashed - never the raw password
    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    @lombok.ToString.Exclude
    private String password;
}
