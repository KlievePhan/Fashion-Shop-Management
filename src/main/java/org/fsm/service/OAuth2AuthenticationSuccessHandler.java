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

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login?error=user_not_found");
            return;
        }
        String contextPath = request.getContextPath();
        if (user.getProfileCompleted()) {
            response.sendRedirect(contextPath + "/");
        } else {
            response.sendRedirect(contextPath + "/profile/setup");
        }
    }

}