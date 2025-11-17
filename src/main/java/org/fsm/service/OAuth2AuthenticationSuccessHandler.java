package org.fsm.service;

import org.fsm.entity.User;
import org.fsm.exception.ResourceNotFoundException;
import org.fsm.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/login?error=oauth_email_missing");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    String sub = oauth2User.getAttribute("sub");
                    return userRepository.findByGoogleSub(sub)
                            .orElse(null);
                });

        String contextPath = request.getContextPath();
        String roleCode = (user.getRole() != null) ? user.getRole().getCode() : null;

        System.out.println("User role from DB: " + roleCode);

        if ("ROLE_ADMIN".equals(roleCode)) {
            response.sendRedirect(contextPath + "/admin");
            return;
        }

        if ("ROLE_STAFF".equals(roleCode)) {
            response.sendRedirect(contextPath + "/staff");
            return;
        }

        if (Boolean.TRUE.equals(user.getProfileCompleted())) {
            response.sendRedirect(contextPath + "/");
        } else {
            response.sendRedirect(contextPath + "/profile/setup");
        }
    }
}