package com.app.todoapp.security;

import com.app.todoapp.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationEventListener {

    @Autowired
    private LoginAttemptService loginAttemptService;

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        loginAttemptService.recordSuccessfulLogin(event.getAuthentication().getName());
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        if (event.getException() instanceof org.springframework.security.authentication.BadCredentialsException) {
            loginAttemptService.recordFailedAttempt(event.getAuthentication().getName());
        }
    }
}