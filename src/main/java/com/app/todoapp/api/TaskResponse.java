package com.app.todoapp.api;

import com.app.todoapp.entities.Priority;
import com.app.todoapp.entities.Task;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private boolean completed;
    private Priority priority;
    private LocalDate dueDate;
    private String category;

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(), task.getTitle(), task.isCompleted(),
                task.getPriority(), task.getDueDate(), task.getCategory()
        );
    }
}