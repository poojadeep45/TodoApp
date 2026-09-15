package com.app.todoapp.controller;

import com.app.todoapp.entities.User;
import com.app.todoapp.repository.UserRepository;
import com.app.todoapp.security.ForgotPasswordRequest;
import com.app.todoapp.security.RegistrationRequest;
import com.app.todoapp.security.ResetPasswordRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Controller
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private static final long RESET_TOKEN_VALID_MINUTES = 30;

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

    @GetMapping("/forgot-password")
    public String forgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordRequest")) {
            model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        }
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
                                 BindingResult bindingResult,
                                 HttpServletRequest httpRequest,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            return "forgot-password";
        }

        var userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALID_MINUTES));
            userRepository.save(user);

            String baseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort();
            String resetLink = baseUrl + "/reset-password?token=" + token;

            log.info("\n=================================================================\n" +
                            "PASSWORD RESET requested for user '{}' ({})\n" +
                            "Reset link (valid {} min): {}\n" +
                            "=================================================================",
                    user.getUsername(), user.getEmail(), RESET_TOKEN_VALID_MINUTES, resetLink);
        } else {
            log.info("Password reset requested for an email with no matching account: {}", request.getEmail());
        }

        model.addAttribute("submitted", true);
        model.addAttribute("emailFound", userOpt.isPresent());
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(@RequestParam String token, Model model) {
        var userOpt = userRepository.findByResetToken(token);
        boolean valid = userOpt.isPresent()
                && userOpt.get().getResetTokenExpiry() != null
                && userOpt.get().getResetTokenExpiry().isAfter(LocalDateTime.now());

        model.addAttribute("tokenValid", valid);
        if (valid) {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken(token);
            model.addAttribute("resetPasswordRequest", request);
        }
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
                                BindingResult bindingResult,
                                Model model) {
        var userOpt = userRepository.findByResetToken(request.getToken());
        boolean valid = userOpt.isPresent()
                && userOpt.get().getResetTokenExpiry() != null
                && userOpt.get().getResetTokenExpiry().isAfter(LocalDateTime.now());

        if (!valid) {
            model.addAttribute("tokenValid", false);
            return "reset-password";
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("tokenValid", true);
            return "reset-password";
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        log.info("Password successfully reset for user '{}'", user.getUsername());

        return "redirect:/login?resetSuccess";
    }
}