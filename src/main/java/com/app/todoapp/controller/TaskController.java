package com.app.todoapp.controller;

import com.app.todoapp.entities.Priority;
import com.app.todoapp.entities.Task;
import com.app.todoapp.entities.User;
//import org.springframework.security.core.userdetails.User;
import com.app.todoapp.security.CustomUserDetails;
import com.app.todoapp.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public String getTasks(@AuthenticationPrincipal CustomUserDetails principal,
                           @RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Priority priority,
                           @RequestParam(required = false) Boolean completed,
                           @RequestParam(required = false) String category,
                           Model model) {
        User owner = principal.getUser();
        model.addAttribute("tasks", taskService.searchTasks(owner, keyword, priority, completed, category));
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("existingCategories", taskService.getDistinctCategories(owner));
        model.addAttribute("stats", taskService.getTaskStats(owner));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCompleted", completed);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("filterQuery", buildFilterQuery(keyword, priority, completed, category));
        model.addAttribute("username", owner.getUsername());
        model.addAttribute("reminders", taskService.getReminders(owner));
        if (!model.containsAttribute("task")) {
            model.addAttribute("task", new Task());
        }
        return "tasks";
    }

    @PostMapping
    public String createTask(@AuthenticationPrincipal CustomUserDetails principal,
                             @Valid @ModelAttribute("task") Task task,
                             BindingResult bindingResult,
                             @RequestParam(required = false, defaultValue = "") String filterQueryString,
                             Model model) {
        User owner = principal.getUser();
        if (bindingResult.hasErrors()) {
            populateFilteredListModel(owner, filterQueryString, model);
            return "tasks";
        }
        taskService.createTask(owner, task.getTitle(), task.getPriority(), task.getDueDate(), task.getCategory());
        return "redirect:/tasks" + filterQueryString;
    }

    @GetMapping("/{id}/edit")
    public String editTaskForm(@AuthenticationPrincipal CustomUserDetails principal,
                               @PathVariable Long id,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) Priority priority,
                               @RequestParam(required = false) Boolean completed,
                               @RequestParam(required = false) String category,
                               Model model) {
        User owner = principal.getUser();
        model.addAttribute("editTask", taskService.getTaskById(id, owner));
        model.addAttribute("tasks", taskService.searchTasks(owner, keyword, priority, completed, category));
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("existingCategories", taskService.getDistinctCategories(owner));
        model.addAttribute("stats", taskService.getTaskStats(owner));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCompleted", completed);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("filterQuery", buildFilterQuery(keyword, priority, completed, category));
        model.addAttribute("username", owner.getUsername());
        model.addAttribute("reminders", taskService.getReminders(owner));
        model.addAttribute("task", new Task());
        return "tasks";
    }

    @PostMapping("/{id}")
    public String updateTask(@AuthenticationPrincipal CustomUserDetails principal,
                             @PathVariable Long id,
                             @Valid @ModelAttribute("editTask") Task editTask,
                             BindingResult bindingResult,
                             @RequestParam(required = false, defaultValue = "") String filterQueryString,
                             Model model) {
        User owner = principal.getUser();
        if (bindingResult.hasErrors()) {
            populateFilteredListModel(owner, filterQueryString, model);
            model.addAttribute("task", new Task());
            return "tasks";
        }
        taskService.updateTask(id, owner, editTask.getTitle(), editTask.getPriority(), editTask.getDueDate(), editTask.getCategory());
        return "redirect:/tasks" + filterQueryString;
    }

    @GetMapping("/{id}/delete")
    public String deleteTask(@AuthenticationPrincipal CustomUserDetails principal,
                             @PathVariable Long id,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Priority priority,
                             @RequestParam(required = false) Boolean completed,
                             @RequestParam(required = false) String category,
                             RedirectAttributes redirectAttributes) {
        taskService.deleteTask(id, principal.getUser());
        redirectAttributes.addFlashAttribute("message", "Task deleted");
        return "redirect:/tasks" + buildFilterQuery(keyword, priority, completed, category);
    }

    @GetMapping("/{id}/toggle")
    public String toggleTask(@AuthenticationPrincipal CustomUserDetails principal,
                             @PathVariable Long id,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Priority priority,
                             @RequestParam(required = false) Boolean completed,
                             @RequestParam(required = false) String category) {
        taskService.toggleTask(id, principal.getUser());
        return "redirect:/tasks" + buildFilterQuery(keyword, priority, completed, category);
    }

    @PostMapping("/bulk/complete-all")
    public String markAllComplete(@AuthenticationPrincipal CustomUserDetails principal,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Priority priority,
                                  @RequestParam(required = false) Boolean completed,
                                  @RequestParam(required = false) String category,
                                  RedirectAttributes redirectAttributes) {
        int count = taskService.markAllComplete(principal.getUser(), keyword, priority, completed, category);
        redirectAttributes.addFlashAttribute("message",
                count == 0 ? "No tasks to complete" : "Marked " + count + " task(s) complete");
        return "redirect:/tasks" + buildFilterQuery(keyword, priority, completed, category);
    }

    @PostMapping("/bulk/clear-completed")
    public String clearCompleted(@AuthenticationPrincipal CustomUserDetails principal,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(required = false) Priority priority,
                                 @RequestParam(required = false) Boolean completed,
                                 @RequestParam(required = false) String category,
                                 RedirectAttributes redirectAttributes) {
        int count = taskService.clearCompleted(principal.getUser(), keyword, priority, category);
        redirectAttributes.addFlashAttribute("message",
                count == 0 ? "No completed tasks to clear" : "Cleared " + count + " completed task(s)");
        return "redirect:/tasks" + buildFilterQuery(keyword, priority, completed, category);
    }

    private String buildFilterQuery(String keyword, Priority priority, Boolean completed, String category) {
        UriComponentsBuilder builder = UriComponentsBuilder.newInstance();
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword);
        }
        if (priority != null) {
            builder.queryParam("priority", priority);
        }
        if (completed != null) {
            builder.queryParam("completed", completed);
        }
        if (category != null && !category.isBlank()) {
            builder.queryParam("category", category);
        }
        String query = builder.build().encode().toUriString();
        return query.isEmpty() ? "" : query;
    }

    private void populateFilteredListModel(User owner, String filterQueryString, Model model) {
        String query = (filterQueryString == null) ? "" : filterQueryString;
        var params = UriComponentsBuilder
                .fromUriString("/tasks" + query)
                .build()
                .getQueryParams();

        String keyword = params.getFirst("keyword");
        String priorityParam = params.getFirst("priority");
        String completedParam = params.getFirst("completed");
        String category = params.getFirst("category");

        Priority priority = null;
        if (priorityParam != null && !priorityParam.isBlank()) {
            try {
                priority = Priority.valueOf(priorityParam);
            } catch (IllegalArgumentException ignored) {
                // Malformed filter value in the hidden field - just ignore it rather than 500
            }
        }
        Boolean completed = (completedParam == null || completedParam.isBlank())
                ? null
                : Boolean.valueOf(completedParam);

        model.addAttribute("tasks", taskService.searchTasks(owner, keyword, priority, completed, category));
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("existingCategories", taskService.getDistinctCategories(owner));
        model.addAttribute("stats", taskService.getTaskStats(owner));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("selectedCompleted", completed);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("filterQuery", query);
        model.addAttribute("username", owner.getUsername());
        model.addAttribute("reminders", taskService.getReminders(owner));
    }
}