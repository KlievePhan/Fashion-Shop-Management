package org.fsm.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private static final String USER_ID = "userId";
    private static final String USER_EMAIL = "userEmail";
    private static final String USER_FULL_NAME = "userFullName";
    private static final String USER_ROLE = "userRole";
    private static final String IS_AUTH = "isAuthenticated";

    // Called from AuthController
    public void login(HttpSession session, User user) {
        session.setAttribute(USER_ID, user.getId());
        session.setAttribute(USER_EMAIL, user.getEmail());
        session.setAttribute(USER_FULL_NAME, user.getFullName());
        session.setAttribute(USER_ROLE, user.getRole().getCode());
        session.setAttribute(IS_AUTH, true);
    }

    public boolean isAuthenticated(HttpSession session) {
        // 1. Check session
        if (Boolean.TRUE.equals(session.getAttribute("isAuthenticated"))) {
            return true;
        }
        // 2. Fallback to Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
    }

    public String getCurrentUserEmail(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email != null) return email;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public Long getCurrentUserId(HttpSession session) {
        return (Long) session.getAttribute(USER_ID);
    }

    public String getCurrentUserFullName(HttpSession session) {
        return (String) session.getAttribute(USER_FULL_NAME);
    }

    public String getCurrentUserRole(HttpSession session) {
        return (String) session.getAttribute(USER_ROLE);
    }

    public boolean isAdmin(HttpSession session) {
        String role = getCurrentUserRole(session);
        return "ROLE_ADMIN".equals(role);
    }

    public void logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }
}