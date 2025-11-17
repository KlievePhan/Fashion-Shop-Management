package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Role;
import org.fsm.entity.User;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
@Controller
public class AdminController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    // Password validation pattern: At least 8 chars, 1 uppercase, 1 number, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$"
    );

    @GetMapping("/admin")
    public String admin(
            Model model,
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        // Load users
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        // Load available roles - NEVER include ROLE_ADMIN in dropdown for safety
        Long currentUserId = sessionService.getCurrentUserId(session);
        User currentUser = userRepository.findById(currentUserId).orElse(null);

        // Only allow USER and STAFF roles in dropdown (admin role cannot be assigned via UI)
        List<Role> roles = roleRepository.findAllByCodeIn(List.of("ROLE_USER", "ROLE_STAFF"));
        model.addAttribute("roles", roles);

        // Load recent audit logs with pagination
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var auditLogs = auditLogService.getAllAuditLogs(pageable);
        model.addAttribute("auditLogs", auditLogs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());

        // Current user info
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserRole", currentUser != null ? currentUser.getRole().getCode() : null);

        return "admin";
    }

    @PostMapping("/admin/users/{id}/edit-role")
    public String editUserRole(
            @PathVariable Long id,
            @RequestParam String roleCode,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        Long currentUserId = sessionService.getCurrentUserId(session);
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired.");
            return "redirect:/admin#users";
        }

        // Check if trying to edit own role
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot change your own role.");
            return "redirect:/admin#users";
        }

        User targetUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent changing admin role - Admin role cannot be changed
        if ("ROLE_ADMIN".equals(targetUser.getRole().getCode())) {
            redirectAttributes.addFlashAttribute("error", "Cannot change administrator role.");
            return "redirect:/admin#users";
        }

        // Prevent assigning admin role - Admin role can only be set in database
        if ("ROLE_ADMIN".equals(roleCode)) {
            redirectAttributes.addFlashAttribute("error", "Administrator role cannot be assigned through UI.");
            return "redirect:/admin#users";
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Only allow ROLE_STAFF or ROLE_USER
        if (!roleCode.equals("ROLE_STAFF") && !roleCode.equals("ROLE_USER")) {
            redirectAttributes.addFlashAttribute("error", "You can only update to Staff or User roles.");
            return "redirect:/admin#users";
        }

        // Store old role for audit
        String oldRole = targetUser.getRole().getCode();
        targetUser.setRole(role);
        userRepository.save(targetUser);

        // Create audit log
        Map<String, Object> changes = new HashMap<>();
        changes.put("userId", id);
        changes.put("oldRole", oldRole);
        changes.put("newRole", roleCode);
        changes.put("userName", targetUser.getFullName());
        auditLogService.createAuditLog(
                currentUser,
                "User",
                id.toString(),
                "UPDATE",
                changes,
                request
        );

        redirectAttributes.addFlashAttribute("success", "User role updated successfully.");
        return "redirect:/admin#users";
    }

    @PostMapping("/admin/users/{id}/toggle-active")
    public String toggleActive(
            @PathVariable Long id,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        Long currentUserId = sessionService.getCurrentUserId(session);

        // Check if trying to deactivate own account
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot change your own active status.");
            return "redirect:/admin#users";
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent deactivating admin accounts
        if ("ROLE_ADMIN".equals(user.getRole().getCode())) {
            redirectAttributes.addFlashAttribute("error", "Cannot deactivate admin accounts.");
            return "redirect:/admin#users";
        }

        boolean oldStatus = user.getActive();
        user.setActive(!user.getActive());
        userRepository.save(user);

        // Create audit log
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        Map<String, Object> changes = new HashMap<>();
        changes.put("userId", id);
        changes.put("userName", user.getFullName());
        changes.put("oldStatus", oldStatus ? "Active" : "Inactive");
        changes.put("newStatus", user.getActive() ? "Active" : "Inactive");
        auditLogService.createAuditLog(
                currentUser,
                "User",
                id.toString(),
                "UPDATE",
                changes,
                request
        );

        redirectAttributes.addFlashAttribute("success", user.getActive() ? "User activated successfully." : "User deactivated successfully.");
        return "redirect:/admin#users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        Long currentUserId = sessionService.getCurrentUserId(session);

        // Check if trying to delete own account
        if (id.equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You cannot delete your own account.");
            return "redirect:/admin#users";
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent deleting admin accounts
        if ("ROLE_ADMIN".equals(user.getRole().getCode())) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete admin accounts.");
            return "redirect:/admin#users";
        }

        // Check if user is active
        if (user.getActive()) {
            redirectAttributes.addFlashAttribute("error", "User must be deactivated before deletion.");
            return "redirect:/admin#users";
        }

        // Store user info for audit log
        String userName = user.getFullName();
        String userEmail = user.getEmail();
        userRepository.delete(user);

        // Create audit log
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        Map<String, Object> changes = new HashMap<>();
        changes.put("userId", id);
        changes.put("userName", userName);
        changes.put("userEmail", userEmail);
        changes.put("action", "Permanently deleted");
        auditLogService.createAuditLog(
                currentUser,
                "User",
                id.toString(),
                "DELETE",
                changes,
                request
        );

        redirectAttributes.addFlashAttribute("success", "User deleted successfully.");
        return "redirect:/admin#users";
    }

    @PostMapping("/admin/users/save")
    public String saveUser(
            @RequestParam(required = false) Long id,
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String resetPassword,
            @RequestParam String roleCode,
            HttpSession session,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes
    ) {
        Long currentUserId = sessionService.getCurrentUserId(session);
        User currentUser = userRepository.findById(currentUserId).orElse(null);
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Session expired.");
            return "redirect:/admin#users";
        }

        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Only admin can assign admin role
        if ("ROLE_ADMIN".equals(roleCode) && !"ROLE_ADMIN".equals(currentUser.getRole().getCode())) {
            redirectAttributes.addFlashAttribute("error", "Only admin can assign admin role.");
            return "redirect:/admin#users";
        }

        // Only admin can set any role
        if (!"ROLE_ADMIN".equals(currentUser.getRole().getCode())) {
            if (!roleCode.equals("ROLE_STAFF") && !roleCode.equals("ROLE_USER")) {
                redirectAttributes.addFlashAttribute("error", "You can only set Staff or User roles.");
                return "redirect:/admin#users";
            }
        }

        User user;
        boolean isNew = (id == null);
        String action;
        Map<String, Object> changes = new HashMap<>();

        if (!isNew) {
            // Update existing user
            if (id.equals(currentUserId)) {
                redirectAttributes.addFlashAttribute("error", "You cannot edit your own account here.");
                return "redirect:/admin#users";
            }

            user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Prevent editing admin users by non-admin
            if ("ROLE_ADMIN".equals(user.getRole().getCode()) && !"ROLE_ADMIN".equals(currentUser.getRole().getCode())) {
                redirectAttributes.addFlashAttribute("error", "You cannot edit admin users.");
                return "redirect:/admin#users";
            }

            // Check if email is being changed and already exists
            if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email already exists.");
                return "redirect:/admin#users";
            }

            // Track changes
            if (!user.getFullName().equals(fullName)) {
                changes.put("fullName", Map.of("old", user.getFullName(), "new", fullName));
            }
            if (!user.getEmail().equals(email)) {
                changes.put("email", Map.of("old", user.getEmail(), "new", email));
            }
            if ("on".equals(resetPassword)) {
                changes.put("password", "Reset");
                user.setPassword(passwordEncoder.encode("12345678"));
            }
            if (!user.getRole().getCode().equals(roleCode)) {
                changes.put("role", Map.of("old", user.getRole().getCode(), "new", roleCode));
            }

            user.setFullName(fullName);
            user.setEmail(email);
            user.setRole(role);
            action = "UPDATE";
        } else {
            // Create new user
            if (userRepository.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Email already exists.");
                return "redirect:/admin#users";
            }

            if (password == null || password.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Password is required for new users.");
                return "redirect:/admin#users";
            }

            // Validate password
            if (!PASSWORD_PATTERN.matcher(password).matches()) {
                redirectAttributes.addFlashAttribute("error", "Password must be at least 8 characters with 1 uppercase, 1 number, and 1 special character (@#$%^&+=!)");
                return "redirect:/admin#users";
            }

            user = new User();
            user.setFullName(fullName);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setActive(true);

            changes.put("fullName", fullName);
            changes.put("email", email);
            changes.put("role", roleCode);
            changes.put("active", true);
            action = "CREATE";
        }

        user = userRepository.save(user);

        // Create audit log
        changes.put("userId", user.getId());
        auditLogService.createAuditLog(
                currentUser,
                "User",
                user.getId().toString(),
                action,
                changes,
                request
        );

        redirectAttributes.addFlashAttribute("success", isNew ? "User created successfully." : "User updated successfully.");
        return "redirect:/admin#users";
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id, HttpSession session) {
        Long currentUserId = sessionService.getCurrentUserId(session);
        User currentUser = userRepository.findById(currentUserId).orElse(null);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("fullName", user.getFullName());
        response.put("email", user.getEmail());
        response.put("phone", user.getPhone());
        response.put("defaultAddress", user.getDefaultAddress());
        response.put("active", user.getActive());
        response.put("profileCompleted", user.getProfileCompleted());
        response.put("createdAt", user.getCreatedAt());
        response.put("googleSub", user.getGoogleSub());

        Map<String, Object> roleInfo = new HashMap<>();
        roleInfo.put("code", user.getRole().getCode());
        roleInfo.put("name", user.getRole().getName());
        roleInfo.put("description", user.getRole().getDescription());
        response.put("role", roleInfo);

        // Check if current user can edit this user
        boolean canEdit = !id.equals(currentUserId);
        if ("ROLE_ADMIN".equals(user.getRole().getCode()) && currentUser != null) {
            canEdit = canEdit && "ROLE_ADMIN".equals(currentUser.getRole().getCode());
        }
        response.put("canEdit", canEdit);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(
            @RequestParam String email,
            @RequestParam(required = false) Long id
    ) {
        boolean exists = userRepository.existsByEmail(email);
        if (id != null) {
            User existingUser = userRepository.findById(id).orElse(null);
            if (existingUser != null && existingUser.getEmail().equals(email)) {
                exists = false;
            }
        }
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users/{id}/view")
    public String viewUserPage(@PathVariable Long id, Model model, HttpSession session) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long currentUserId = sessionService.getCurrentUserId(session);
        User currentUser = userRepository.findById(currentUserId).orElse(null);

        model.addAttribute("user", user);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserRole", currentUser != null ? currentUser.getRole().getCode() : null);

        return "admin-user-view";
    }

    @GetMapping("/admin/users/{id}/edit")
    public String editUserPage(@PathVariable Long id, Model model, HttpSession session) {
        Long currentUserId = sessionService.getCurrentUserId(session);
        User currentUser = userRepository.findById(currentUserId).orElse(null);

        if (id.equals(currentUserId)) {
            return "redirect:/admin#users?error=Cannot edit your own account";
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent editing admin users by non-admin
        if ("ROLE_ADMIN".equals(user.getRole().getCode()) && currentUser != null && !"ROLE_ADMIN".equals(currentUser.getRole().getCode())) {
            return "redirect:/admin#users?error=Cannot edit admin users";
        }

        List<Role> roles;
        if (currentUser != null && "ROLE_ADMIN".equals(currentUser.getRole().getCode())) {
            roles = roleRepository.findAll();
        } else {
            roles = roleRepository.findAllByCodeIn(List.of("ROLE_USER", "ROLE_STAFF"));
        }

        model.addAttribute("user", user);
        model.addAttribute("roles", roles);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserRole", currentUser != null ? currentUser.getRole().getCode() : null);

        return "admin-user-edit";
    }
}