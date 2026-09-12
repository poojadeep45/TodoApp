package com.app.todoapp.controller;

import com.app.todoapp.entities.User;
import com.app.todoapp.repository.UserRepository;
import com.app.todoapp.security.RegistrationRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("registrationRequest")) {
            model.addAttribute("registrationRequest", new RegistrationRequest());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationRequest") RegistrationRequest request,
                           BindingResult bindingResult,
                           Model model) {
        if (userRepository.existsByUsername(request.getUsername())) {
            bindingResult.rejectValue("username", "duplicate", "That username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            bindingResult.rejectValue("email", "duplicate", "That email is already registered");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (bindingResult.hasErrors()) {
            return "register";
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        userRepository.save(user);

        return "redirect:/login?registered";
    }
}