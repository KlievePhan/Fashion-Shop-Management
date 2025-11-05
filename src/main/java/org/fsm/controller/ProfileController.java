package org.fsm.controller;

import jakarta.servlet.http.HttpServletRequest; // MUST IMPORT
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fsm.dto.request.ProfileUpdateRequest;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    // inside ProfileController (imports omitted for brevity)
    @GetMapping("/setup")
    public String showSetupForm(Model model,
                                Authentication authentication,
                                HttpServletRequest request) {

        model.addAttribute("currentPath", request.getRequestURI());

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return "redirect:/login?error=not_authenticated";
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            return "redirect:/login?error=email_missing";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }

        // Pre-populate DTO with existing values
        ProfileUpdateRequest dto = new ProfileUpdateRequest();
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setDefaultAddress(user.getDefaultAddress());
        model.addAttribute("userForm", dto);

        return "profile/setup";
    }

    @PostMapping("/setup")
    public String saveProfile(@Valid @ModelAttribute("userForm") ProfileUpdateRequest form,
                              BindingResult result,
                              Authentication authentication,
                              Model model,
                              HttpServletRequest request) {

        model.addAttribute("currentPath", request.getRequestURI());

        if (result.hasErrors()) {
            // validation failed: show same page with errors
            return "profile/setup";
        }

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return "redirect:/login?error=not_authenticated";
        }

        String email = oauth2User.getAttribute("email");
        if (email == null) {
            return "redirect:/login?error=email_missing";
        }

        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing == null) {
            return "redirect:/login?error=user_not_found";
        }

        // Apply validated values
        existing.setFullName(form.getFullName());
        existing.setPhone(form.getPhone());
        existing.setDefaultAddress(form.getDefaultAddress());
        existing.setProfileCompleted(true);

        try {
            userRepository.save(existing);
        } catch (DataIntegrityViolationException dive) {
            model.addAttribute("errorMessage", "Invalid profile data or duplicate value.");
            return "profile/setup";
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Unexpected error. Please try later.");
            return "profile/setup";
        }

        return "redirect:/?profile=completed";
    }

    @GetMapping("/view")
    public String showProfile(Model model,
                              Authentication authentication,
                              HttpServletRequest request) {

        model.addAttribute("currentPath", request.getRequestURI());

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return "redirect:/login?error=not_authenticated";
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) {
            return "redirect:/login?error=email_missing";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return "redirect:/login?error=user_not_found";
        }

        model.addAttribute("user", user);
        return "profile/view"; // → src/main/resources/templates/profile/view.html
    }
}