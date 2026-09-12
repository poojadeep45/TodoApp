package com.app.todoapp.api;

import com.app.todoapp.entities.Priority;
import com.app.todoapp.entities.Task;
import com.app.todoapp.entities.User;
import com.app.todoapp.security.CustomUserDetails;
import com.app.todoapp.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Manage the authenticated user's tasks")
@SecurityRequirement(name = "basicAuth")
public class TaskApiController {

    @Autowired
    private TaskService taskService;

    @Operation(summary = "List tasks", description = "Returns the authenticated user's tasks, optionally filtered by keyword, priority, status, and category.")
    @GetMapping
    public List<TaskResponse> getAllTasks(
            @AuthenticationPrincipal CustomUserDetails principal,
            @Parameter(description = "Case-insensitive substring match on title") @RequestParam(required = false) String keyword,
            @Parameter(description = "LOW, MEDIUM, or HIGH") @RequestParam(required = false) Priority priority,
            @Parameter(description = "true = completed only, false = active only, omit = both") @RequestParam(required = false) Boolean completed,
            @Parameter(description = "Exact category match") @RequestParam(required = false) String category) {
        User owner = principal.getUser();
        return taskService.searchTasks(owner, keyword, priority, completed, category).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @Operation(summary = "Get a task by id", description = "Returns 404 if the task doesn't exist OR belongs to a different user.")
    @GetMapping("/{id}")
    public TaskResponse getTask(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return TaskResponse.from(taskService.getTaskById(id, principal.getUser()));
    }

    @Operation(summary = "Create a task")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@AuthenticationPrincipal CustomUserDetails principal,
                                   @Valid @RequestBody TaskRequest request) {
        Task created = taskService.createTask(principal.getUser(), request.getTitle(), request.getPriority(),
                request.getDueDate(), request.getCategory());
        return TaskResponse.from(created);
    }

    @Operation(summary = "Update a task", description = "Replaces title, priority, due date, and category for an existing task.")
    @PutMapping("/{id}")
    public TaskResponse updateTask(@AuthenticationPrincipal CustomUserDetails principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody TaskRequest request) {
        Task updated = taskService.updateTask(id, principal.getUser(), request.getTitle(), request.getPriority(),
                request.getDueDate(), request.getCategory());
        return TaskResponse.from(updated);
    }

    @Operation(summary = "Toggle completed status", description = "Flips a task between completed and not completed.")
    @PatchMapping("/{id}/toggle")
    public TaskResponse toggleTask(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return TaskResponse.from(taskService.toggleTask(id, principal.getUser()));
    }

    @Operation(summary = "Delete a task")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        taskService.deleteTask(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}