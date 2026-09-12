package com.app.todoapp.api;

import com.app.todoapp.entities.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    private Priority priority;

    private LocalDate dueDate;

    @Size(max = 50, message = "Category must be 50 characters or fewer")
    private String category;
}