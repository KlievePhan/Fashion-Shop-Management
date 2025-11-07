package org.fsm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes user session data available to all Thymeleaf templates
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(HttpSession session) {
        // Check session first
        Boolean sessionAuth = (Boolean) session.getAttribute("isAuthenticated");
        if (Boolean.TRUE.equals(sessionAuth)) {
            return true;
        }

        // Fallback to Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    @ModelAttribute("currentUserId")
    public Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email != null) return email;

        // Fallback to Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }

    @ModelAttribute("currentUserFullName")
    public String currentUserFullName(HttpSession session) {
        return (String) session.getAttribute("userFullName");
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole(HttpSession session) {
        return (String) session.getAttribute("userRole");
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        return "ROLE_ADMIN".equals(role);
    }

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }

        // Fallback to Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        }

        return null;
    }
}