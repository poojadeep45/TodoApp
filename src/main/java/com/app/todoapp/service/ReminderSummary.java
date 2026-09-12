package com.app.todoapp.service;

import com.app.todoapp.entities.Task;

import java.util.List;

public record ReminderSummary(List<Task> overdue, List<Task> dueToday, List<Task> dueSoon) {

    public boolean isEmpty() {
        return overdue.isEmpty() && dueToday.isEmpty() && dueSoon.isEmpty();
    }
}