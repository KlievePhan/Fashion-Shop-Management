package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Product;
import org.fsm.entity.Role;
import org.fsm.entity.User;
import org.fsm.repository.BrandRepository;
import org.fsm.repository.ProductRepository;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.UserRepository;
import org.fsm.service.AuditLogService;
import org.fsm.service.SessionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class AdminController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    @GetMapping("/admin")
    public String admin(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        // Load users
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        // Load products
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        // Load brands
        model.addAttribute("brands", brandRepository.findAll());
        // Load recent audit logs with pagination
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var auditLogs = auditLogService.getAllAuditLogs(pageable);
        model.addAttribute("auditLogs", auditLogs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());
        return "admin";
    }

    @PostMapping("/admin/users/{id}/edit-role")
    public String editUserRole(@PathVariable Long id, @RequestParam String roleCode, HttpSession session, RedirectAttributes redirectAttributes) {
        Long currentUserId = sessionService.getCurrentUserId(session);
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot change your own role.");
            return "redirect:/admin#users";
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        // Limit role updates to ROLE_STAFF or ROLE_USER (as per task, assuming admin can't set to ADMIN for others, but adjust if needed)
        if (!roleCode.equals("ROLE_STAFF") && !roleCode.equals("ROLE_USER")) {
            redirectAttributes.addFlashAttribute("error", "You can only update to Staff or User roles.");
            return "redirect:/admin#users";
        }
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/admin#users";
    }

    @PostMapping("/admin/users/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long currentUserId = sessionService.getCurrentUserId(session);
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot change your own active status.");
            return "redirect:/admin#users";
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(!user.getActive());
        userRepository.save(user);
        return "redirect:/admin#users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, HttpSession session, RedirectAttributes redirectAttributes) {
        Long currentUserId = sessionService.getCurrentUserId(session);
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/admin#users";
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getActive()) {
            redirectAttributes.addFlashAttribute("error", "User must be deactivated before deletion.");
            return "redirect:/admin#users";
        }
        userRepository.delete(user);
        return "redirect:/admin#users";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(
            @RequestParam(required = false) Long id,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam String roleCode,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        // Limit role to STAFF or USER for updates/creations (adjust if admins can create other admins)
        if (!roleCode.equals("ROLE_STAFF") && !roleCode.equals("ROLE_USER")) {
            redirectAttributes.addFlashAttribute("error", "You can only set Staff or User roles.");
            return "redirect:/admin#users";
        }
        User user;
        if (id != null) {
            // Update
            user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Long currentUserId = sessionService.getCurrentUserId(session);
            if (id.equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("error", "You cannot edit your own account.");
                return "redirect:/admin#users";
            }
            user.setFullName(fullName);
            user.setEmail(email);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }
            user.setRole(role);
        } else {
            // Create
            if (userRepository.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email already exists.");
                return "redirect:/admin#users";
            }
            user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setActive(true); // New users are active by default
        }
        userRepository.save(user);
        return "redirect:/admin#users";
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }
}