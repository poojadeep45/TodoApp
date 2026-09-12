package com.app.todoapp.api;

import com.app.todoapp.entities.Priority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequest {

    @Schema(description = "Task title", example = "Buy groceries", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    @Schema(description = "Defaults to MEDIUM if omitted", example = "HIGH")
    private Priority priority;

    @Schema(description = "Optional due date", example = "2026-09-20")
    private LocalDate dueDate;

    @Schema(description = "Optional free-text category", example = "Personal")
    @Size(max = 50, message = "Category must be 50 characters or fewer")
    private String category;
}