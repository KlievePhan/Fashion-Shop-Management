// src/main/java/org/fsm/controller/ProfileController.java
package org.fsm.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fsm.dto.request.ProfileUpdateRequest;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.fsm.service.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // ───── SHOW FORM (setup OR edit) ─────
    @GetMapping({"/setup"})
    public String showForm(Model model,
                           Authentication authentication,
                           HttpServletRequest request) {

        model.addAttribute("currentPath", request.getRequestURI());

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return "redirect:/login?error=not_authenticated";
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) return "redirect:/login?error=email_missing";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/login?error=user_not_found";

        ProfileUpdateRequest dto = new ProfileUpdateRequest();
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setDefaultAddress(user.getDefaultAddress());
        dto.setAvatarUrl(user.getAvatarUrl());

        model.addAttribute("userForm", dto);
        model.addAttribute("currentAvatarUrl", user.getAvatarUrl());

        return "profile/setup";   // SAME FORM for both
    }

    // ───── SAVE (setup OR edit) ─────
    @PostMapping({"/setup", "/edit"})
    @Transactional
    public String saveProfile(@Valid @ModelAttribute("userForm") ProfileUpdateRequest form,
                              BindingResult result,
                              Authentication authentication,
                              Model model,
                              HttpServletRequest request) {

        model.addAttribute("currentPath", request.getRequestURI());

        // Keep avatar preview on validation error
        String currentAvatarUrl = null;
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            User u = userRepository.findByEmail(email).orElse(null);
            currentAvatarUrl = u != null ? u.getAvatarUrl() : null;
        }
        model.addAttribute("currentAvatarUrl", currentAvatarUrl);

        if (result.hasErrors()) {
            String path = request.getRequestURI();
            if (path.contains("/setup")) {
                model.addAttribute("currentPath", "/profile/setup");
            } else {
                model.addAttribute("currentPath", "/profile/edit");
            }
        }

        // ---- Auth check ----
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oauth2User)) {
            return "redirect:/login?error=not_authenticated";
        }
        String email = oauth2User.getAttribute("email");
        if (email == null) return "redirect:/login?error=email_missing";

        User existing = userRepository.findByEmail(email).orElse(null);
        if (existing == null) return "redirect:/login?error=user_not_found";

        // ---- 1. Avatar (optional) ----
        MultipartFile avatarFile = form.getAvatar();
        String oldAvatar = existing.getAvatarUrl();

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String newUrl = fileStorageService.storeAvatar(avatarFile, existing.getId());
                existing.setAvatarUrl(newUrl);
                if (oldAvatar != null) {
                    try { fileStorageService.deleteAvatar(oldAvatar); } catch (Exception ignored) {}
                }
            } catch (IllegalArgumentException e) {
                result.rejectValue("avatar", "avatar.invalid", e.getMessage());
                return "profile/setup";
            } catch (IOException e) {
                model.addAttribute("errorMessage", "Avatar upload failed: " + e.getMessage());
                return "profile/setup";
            }
        }

        // ---- 2. TEXT FIELDS (THIS IS THE PART THAT WAS MISSING) ----
        existing.setFullName(form.getFullName());
        existing.setPhone(form.getPhone());
        existing.setDefaultAddress(form.getDefaultAddress());
        existing.setProfileCompleted(true);   // <-- ALWAYS true after save

        // ---- 3. SAVE TO DB ----
        try {
            userRepository.save(existing);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Save failed: " + ex.getMessage());
            model.addAttribute("currentAvatarUrl", oldAvatar);
            return "profile/setup";
        }

        return "redirect:/profile/view?success=true";
    }

    // ───── VIEW PROFILE ─────
    @GetMapping("/view")
    public String viewProfile(Model model,
                              Authentication authentication,
                              HttpServletRequest request) {
        model.addAttribute("currentPath", request.getRequestURI());

        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User)) {
            return "redirect:/login?error=not_authenticated";
        }

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        if (email == null) return "redirect:/login?error=email_missing";

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return "redirect:/login?error=user_not_found";

        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl("images/default_avatar.png");
        }

        model.addAttribute("user", user);
        return "profile/view";
    }
}