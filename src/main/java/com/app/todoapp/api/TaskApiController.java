package com.app.todoapp.api;

import com.app.todoapp.entities.Priority;
import com.app.todoapp.entities.Task;
import com.app.todoapp.entities.User;
import com.app.todoapp.security.CustomUserDetails;
import com.app.todoapp.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskApiController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public List<TaskResponse> getAllTasks(@AuthenticationPrincipal CustomUserDetails principal,
                                          @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Priority priority,
                                          @RequestParam(required = false) Boolean completed,
                                          @RequestParam(required = false) String category) {
        User owner = principal.getUser();
        return taskService.searchTasks(owner, keyword, priority, completed, category).stream()
                .map(TaskResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public TaskResponse getTask(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return TaskResponse.from(taskService.getTaskById(id, principal.getUser()));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@AuthenticationPrincipal CustomUserDetails principal,
                                   @Valid @RequestBody TaskRequest request) {
        Task created = taskService.createTask(principal.getUser(), request.getTitle(), request.getPriority(),
                request.getDueDate(), request.getCategory());
        return TaskResponse.from(created);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(@AuthenticationPrincipal CustomUserDetails principal,
                                   @PathVariable Long id,
                                   @Valid @RequestBody TaskRequest request) {
        Task updated = taskService.updateTask(id, principal.getUser(), request.getTitle(), request.getPriority(),
                request.getDueDate(), request.getCategory());
        return TaskResponse.from(updated);
    }

    @PatchMapping("/{id}/toggle")
    public TaskResponse toggleTask(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        return TaskResponse.from(taskService.toggleTask(id, principal.getUser()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@AuthenticationPrincipal CustomUserDetails principal, @PathVariable Long id) {
        taskService.deleteTask(id, principal.getUser());
        return ResponseEntity.noContent().build();
    }
}