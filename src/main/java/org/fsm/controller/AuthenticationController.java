package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserRepository userRepository;

    @ModelAttribute
    public void populateUser(Model model, Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            String email = null;
            if (principal instanceof OAuth2User oauth2User) {
                email = oauth2User.getAttribute("email");
            } else if (principal instanceof org.springframework.security.core.userdetails.User) {
                email = ((org.springframework.security.core.userdetails.User) principal).getUsername();
            }

            if (email != null) {
                User user = userRepository.findByEmail(email).orElse(null);
                model.addAttribute("currentUser", user);
            }
        }
    }
}
