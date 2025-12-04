package org.fsm.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        // Lấy email từ authentication
        String email = authentication.getName();
        
        // Tìm user trong database
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user != null) {
            // Set session attributes
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userEmail", user.getEmail());
            session.setAttribute("userFullName", user.getFullName());
            session.setAttribute("userRole", user.getRole().getCode());
            session.setAttribute("isAuthenticated", true);
        }
        
        // Lấy role và redirect
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(auth -> auth.getAuthority())
                .orElse("");
        
        String contextPath = request.getContextPath();

        if ("ROLE_ADMIN".equals(role)) {
            response.sendRedirect(contextPath + "/admin?loginSuccess=true");
        } else if ("ROLE_STAFF".equals(role)) {
            response.sendRedirect(contextPath + "/staff?loginSuccess=true");
        } else {
            response.sendRedirect(contextPath + "/shop?loginSuccess=true");
        }
    }
}
