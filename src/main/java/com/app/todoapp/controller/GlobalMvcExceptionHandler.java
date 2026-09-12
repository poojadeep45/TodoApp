package com.app.todoapp.controller;

import com.app.todoapp.exception.TaskNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice(basePackageClasses = TaskController.class)
public class GlobalMvcExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public String handleTaskNotFound(TaskNotFoundException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("message", ex.getMessage());
        return "redirect:/tasks";
    }
}