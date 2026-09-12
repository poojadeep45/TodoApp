package com.app.todoapp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Title cannot be blank.")
    @Size(max = 200, message = "Title must be 200 characters or fewer")
    private String title;

    private boolean completed;

    @NotNull(message = "Priority is required.")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    //nullable - a task doesn't need a due date
    private LocalDate dueDate;

    @Size(max = 50, message = "Category must be 50 characters or fewer")
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @jakarta.persistence.Transient
    public boolean isOverdue() {
        return dueDate != null && !completed && dueDate.isBefore(LocalDate.now());
    }

    public String getFormattedDueDate() {
        if (dueDate == null) {
            return null;
        }
        return dueDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
