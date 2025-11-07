package org.fsm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.security.CustomUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class AuthenticationController {

    private final CustomUserDetailsService userDetailsService;

    @ModelAttribute
    public void populateCurrentUser(Model model, Authentication authentication) {
        // Always reset to avoid stale data
        model.addAttribute("currentUser", null);

        if (authentication == null || !authentication.isAuthenticated()) {
            return;
        }

        // Skip anonymous users
        if (authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal())) {
            return;
        }

        String email = extractEmail(authentication);
        if (email != null) {
            userDetailsService.findUserByEmail(email)
                    .ifPresent(user -> model.addAttribute("currentUser", user));
        }
    }

    private String extractEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof org.springframework.security.core.userdetails.User springUser) {
            return springUser.getUsername(); // email
        }

        if (principal instanceof OAuth2User oauth2User) {
            return oauth2User.getAttribute("email");
        }

        // Fallback: check if principal is a String (rare)
        if (principal instanceof String) {
            return principal.toString();
        }

        return null;
    }
}