package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.fsm.entity.User;
import org.fsm.entity.Role;
import org.fsm.entity.Product;
import org.fsm.repository.UserRepository;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.ProductRepository;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class AdminController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;

    @GetMapping("/admin")
    public String admin(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        // Add more attributes for orders, etc., if repositories are available
        return "admin";
    }

    @PostMapping("/admin/users/{id}/edit-role")
    public String editUserRole(@PathVariable Long id, @RequestParam String roleCode) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        Role role = roleRepository.findByCode(roleCode).orElseThrow(() -> new RuntimeException("Role not found"));
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/admin";
    }

    // Additional methods for delete user, etc., can be added
}