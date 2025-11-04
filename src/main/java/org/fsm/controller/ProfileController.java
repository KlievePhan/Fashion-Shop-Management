package org.fsm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    // GET: Show setup form
    @GetMapping("/setup")
    public String showSetupForm(Model model, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return "redirect:/login?error=not_authenticated";
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        if (email == null) {
            return "redirect:/login?error=email_missing";
        }

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }

        model.addAttribute("user", user);
        return "profile/setup"; // → src/main/resources/templates/profile/setup.html
    }

    // POST: Save profile
    @PostMapping("/setup")
    public String saveProfile(@Valid @ModelAttribute("user") User formUser,
                              BindingResult result,
                              Authentication authentication) {

        if (result.hasErrors()) {
            return "profile/setup"; // Stay on form with errors
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");

        User existing = userRepository.findByEmail(email)
                .orElse(null);

        if (existing == null) {
            return "redirect:/login?error=user_not_found";
        }

        // Update only allowed fields
        existing.setFullName(formUser.getFullName());
        existing.setPhone(formUser.getPhone());
        existing.setDefaultAddress(formUser.getDefaultAddress());
        existing.setProfileCompleted(true);

        userRepository.save(existing);

        return "redirect:/"; // Go to home/dashboard
    }
}