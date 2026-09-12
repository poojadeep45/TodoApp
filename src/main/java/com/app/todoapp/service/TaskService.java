package com.app.todoapp.service;

import com.app.todoapp.entities.Priority;
import com.app.todoapp.entities.Task;
import com.app.todoapp.entities.User;
import com.app.todoapp.exception.TaskNotFoundException;
import com.app.todoapp.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    public List<Task> getAllTasks(User owner) {
        return taskRepository.findAllByOwnerOrderByCompletedAscDueDateAscPriorityDesc(owner);
    }

    public List<Task> searchTasks(User owner, String keyword, Priority priority, Boolean completed, String category) {
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String normalizedCategory = (category == null || category.isBlank()) ? null : category.trim();
        return taskRepository.search(owner, normalizedKeyword, priority, completed, normalizedCategory);
    }

    public List<String> getDistinctCategories(User owner) {
        return taskRepository.findDistinctCategoriesByOwner(owner);
    }

    public TaskStats getTaskStats(User owner) {
        long total = taskRepository.countByOwner(owner);
        long completed = taskRepository.countByOwnerAndCompleted(owner, true);
        return new TaskStats(total, completed);
    }

    public Task getTaskById(Long id, User owner) {
        return taskRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public Task createTask(User owner, String title, Priority priority, LocalDate dueDate, String category) {
        Task task = Task.builder()
                .owner(owner)
                .title(title)
                .completed(false)
                .priority(priority != null ? priority : Priority.MEDIUM)
                .dueDate(dueDate)
                .category(normalizeCategory(category))
                .build();
        return taskRepository.save(task);
    }

    public Task updateTask(Long id, User owner, String title, Priority priority, LocalDate dueDate, String category) {
        Task task = getTaskById(id, owner);
        task.setTitle(title);
        task.setPriority(priority != null ? priority : Priority.MEDIUM);
        task.setDueDate(dueDate);
        task.setCategory(normalizeCategory(category));
        return taskRepository.save(task);
    }

    public void deleteTask(Long id, User owner) {
        Task task = getTaskById(id, owner);
        taskRepository.delete(task);
    }

    public Task toggleTask(Long id, User owner) {
        Task task = getTaskById(id, owner);
        task.setCompleted(!task.isCompleted());
        return taskRepository.save(task);
    }

    public int markAllComplete(User owner, String keyword, Priority priority, Boolean completed, String category) {
        List<Task> matching = searchTasks(owner, keyword, priority, completed, category);
        matching.forEach(task -> task.setCompleted(true));
        taskRepository.saveAll(matching);
        return matching.size();
    }

    public int clearCompleted(User owner, String keyword, Priority priority, String category) {
        List<Task> matching = searchTasks(owner, keyword, priority, true, category);
        taskRepository.deleteAll(matching);
        return matching.size();
    }

    private String normalizeCategory(String category) {
        return (category == null || category.isBlank()) ? null : category.trim();
    }

    public ReminderSummary getReminders(User owner) {
        LocalDate today = LocalDate.now();
        LocalDate soonCutoff = today.plusDays(3);

        List<Task> overdue = taskRepository.findByOwnerAndCompletedFalseAndDueDateBeforeOrderByDueDateAsc(owner, today);
        List<Task> dueToday = taskRepository.findByOwnerAndCompletedFalseAndDueDateOrderByDueDateAsc(owner, today);
        List<Task> dueSoon = taskRepository.findByOwnerAndCompletedFalseAndDueDateBetweenOrderByDueDateAsc(
                owner, today.plusDays(1), soonCutoff);

        return new ReminderSummary(overdue, dueToday, dueSoon);
    }
}