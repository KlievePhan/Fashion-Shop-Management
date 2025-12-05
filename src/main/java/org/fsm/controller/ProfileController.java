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

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // ───── SHOW FORM (setup OR edit) ─────
    @GetMapping({"/setup", "/edit"})
    public String showProfileForm(Model model, Authentication authentication, HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        model.addAttribute("currentPath", requestUri);

        User user = getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login?error=not_authenticated";
        }

        ProfileUpdateRequest dto = new ProfileUpdateRequest();
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setDefaultAddress(user.getDefaultAddress());
        dto.setAvatarUrl(user.getAvatarUrl());

        model.addAttribute("userForm", dto);
        model.addAttribute("currentAvatarUrl", user.getAvatarUrl());
        model.addAttribute("isEdit", requestUri.contains("/edit") || user.getProfileCompleted());

        // Use setup.html template for both setup and edit
        return "profile/setup";
    }

    // ───── SAVE (setup OR edit) ─────
    @PostMapping({"/setup", "/edit"})
    @Transactional
    public String saveProfile(@Valid @ModelAttribute("userForm") ProfileUpdateRequest form,
                              BindingResult result,
                              Authentication authentication,
                              Model model,
                              HttpServletRequest request) {

        User user = getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login?error=not_authenticated";
        }

        model.addAttribute("currentPath", request.getRequestURI());
        model.addAttribute("currentAvatarUrl", user.getAvatarUrl());

        if (result.hasErrors()) {
            return request.getRequestURI().contains("/setup") ? "profile/setup" : "profile/edit";
        }

        // Avatar upload
        MultipartFile avatarFile = form.getAvatar();
        String oldAvatar = user.getAvatarUrl();

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String newUrl = fileStorageService.storeAvatar(avatarFile, user.getId());
                user.setAvatarUrl(newUrl);
                if (oldAvatar != null && !oldAvatar.contains("default")) {
                    try { fileStorageService.deleteAvatar(oldAvatar); } catch (Exception ignored) {}
                }
            } catch (Exception e) {
                result.rejectValue("avatar", "avatar.invalid", "Upload failed: " + e.getMessage());
                return request.getRequestURI().contains("/setup") ? "profile/setup" : "profile/edit";
            }
        }

        // Update fields
        user.setFullName(form.getFullName());
        user.setPhone(form.getPhone());
        user.setDefaultAddress(form.getDefaultAddress());
        user.setProfileCompleted(true);

        userRepository.save(user);

        return "redirect:/profile/view?success=true";
    }

    // ───── VIEW PROFILE ─────
    @GetMapping("/view")
    public String viewProfile(Model model, Authentication authentication, HttpServletRequest request) {
        model.addAttribute("currentPath", request.getRequestURI());

        User user = getCurrentUser(authentication);
        if (user == null) {
            return "redirect:/login?error=not_authenticated";
        }

        if (user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) {
            user.setAvatarUrl("/images/default_avatar.png");
        }

        model.addAttribute("user", user);
        return "profile/view";
    }

    // ADD THIS HELPER METHOD at the top of the class
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        Object principal = authentication.getPrincipal();

        String email;

        if (principal instanceof OAuth2User oauth2User) {
            email = oauth2User.getAttribute("email");
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            email = userDetails.getUsername(); // because you store email as username
        } else if (principal instanceof String) {
            email = (String) principal;
        } else {
            return null;
        }

        return userRepository.findByEmail(email).orElse(null);
    }
}