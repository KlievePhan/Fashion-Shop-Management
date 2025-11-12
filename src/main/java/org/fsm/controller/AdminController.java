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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class AdminController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final AuditLogService auditLogService;

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
    public String editUserRole(@PathVariable Long id, @RequestParam String roleCode) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/admin#users";
    }

    // Additional methods for delete user, etc., can be added
}