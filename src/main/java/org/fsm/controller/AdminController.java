package org.fsm.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.fsm.dto.response.AnalyticsOverviewResponse;
import org.fsm.entity.Role;
import org.fsm.entity.User;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.UserRepository;
import org.fsm.entity.Blog;
import org.fsm.service.AnalyticsService;
import org.fsm.service.AuditLogService;
import org.fsm.service.BlogService;
import org.fsm.service.SessionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    private final AnalyticsService analyticsService;
    private final BlogService blogService;
    private final org.fsm.repository.OrderRepository orderRepository;
    private final org.fsm.repository.ProductRepository productRepository;
    private final org.fsm.repository.BrandRepository brandRepository;
    private final org.fsm.repository.OrderItemRepository orderItemRepository;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    // ==========================================
    // 1. VIEW METHODS (Trả về giao diện HTML)
    // ==========================================

        @GetMapping("/admin")
    public String admin(Model model, HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String tab,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int blogPage) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);

        Long currentUserId = sessionService.getCurrentUserId(session);
        User currentUser = null;
        
        // Fallback: nếu session chưa có userId, lấy từ Spring Security authentication
        if (currentUserId == null) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                String email = auth.getName();
                currentUser = userRepository.findByEmail(email).orElse(null);
                if (currentUser != null) {
                    currentUserId = currentUser.getId();
                }
            }
        } else {
            currentUser = userRepository.findById(currentUserId).orElse(null);
        }

        List<Role> roles = roleRepository.findAllByCodeIn(List.of("ROLE_USER", "ROLE_STAFF"));
        model.addAttribute("roles", roles);

        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var auditLogs = auditLogService.getAllAuditLogs(pageable);
        model.addAttribute("auditLogs", auditLogs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());

        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("currentUserRole", currentUser != null ? currentUser.getRole().getCode() : null);

        // ===== Blogs với phân trang =====
        Page<Blog> blogPageResult;
        int blogPageSize = "blogs".equals(tab) ? 5 : 50; // 5 blogs per page cho tab blogs
        
        if ("blogs".equals(tab)) {
            Pageable blogPageable = PageRequest.of(blogPage, blogPageSize, Sort.by("createdAt").descending());
            
            // Nếu có keyword, search
            if (keyword != null && !keyword.trim().isEmpty()) {
                blogPageResult = blogService.searchAllBlogs(keyword.trim(), blogPageable);
            }
            // Nếu có status filter
            else if (status != null && !status.isEmpty()) {
                try {
                    org.fsm.entity.Blog.BlogStatus blogStatus = org.fsm.entity.Blog.BlogStatus.valueOf(status.toUpperCase());
                    blogPageResult = blogService.getBlogsByStatus(blogStatus, blogPageable);
                } catch (IllegalArgumentException e) {
                    blogPageResult = blogService.getAllBlogsForStaff(blogPageable);
                }
            }
            // Lấy tất cả blog
            else {
                blogPageResult = blogService.getAllBlogsForStaff(blogPageable);
            }
            
            model.addAttribute("blogs", blogPageResult.getContent());
            model.addAttribute("blogCurrentPage", blogPageResult.getNumber());
            model.addAttribute("blogTotalPages", blogPageResult.getTotalPages());
            model.addAttribute("blogTotalElements", blogPageResult.getTotalElements());
            model.addAttribute("blogPageSize", blogPageSize);
        } else {
            // Nếu không phải tab blogs, lấy tất cả (không phân trang) để tính stats
            List<Blog> allBlogs = blogService.getAllBlogsForStaff();
            model.addAttribute("blogs", allBlogs);
        }
        
        model.addAttribute("blogStatuses", org.fsm.entity.Blog.BlogStatus.values());
        model.addAttribute("currentBlogStatus", status);
        model.addAttribute("blogKeyword", keyword);
        
        // Blog stats (tính từ tất cả blogs)
        model.addAttribute("totalBlogs", blogService.countAllBlogs());
        model.addAttribute("draftCount", blogService.countBlogsByStatus(org.fsm.entity.Blog.BlogStatus.DRAFT));
        model.addAttribute("pendingCount", blogService.countBlogsByStatus(org.fsm.entity.Blog.BlogStatus.PENDING_REVIEW));
        model.addAttribute("publishedCount", blogService.countBlogsByStatus(org.fsm.entity.Blog.BlogStatus.PUBLISHED));
        model.addAttribute("archivedCount", blogService.countBlogsByStatus(org.fsm.entity.Blog.BlogStatus.ARCHIVED));
        model.addAttribute("pendingDeleteCount", blogService.countBlogsByStatus(org.fsm.entity.Blog.BlogStatus.PENDING_DELETE));
        
        // Set active tab nếu có
        if (tab != null && !tab.isEmpty()) {
            model.addAttribute("activeTab", tab);
        }

        return "admin";
    }

    // ==========================================
    // Analytics APIs
    // ==========================================

    @GetMapping("/admin/analytics/overview")
    @ResponseBody
    public ResponseEntity<AnalyticsOverviewResponse> getAnalyticsOverview(
            @RequestParam(name = "days", defaultValue = "30") int days) {
        AnalyticsOverviewResponse overview = analyticsService.getOverview(days);
        return ResponseEntity.ok(overview);
    }

    @GetMapping("/admin/dashboard/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // User stats
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.findAll().stream()
                .filter(u -> u.getActive() != null && u.getActive())
                .count();
        long inactiveUsers = totalUsers - activeUsers;
        long adminCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ROLE_ADMIN".equals(u.getRole().getCode()))
                .count();
        long staffCount = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "ROLE_STAFF".equals(u.getRole().getCode()))
                .count();
        
        // Order stats
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByStatus("PENDING") + orderRepository.countByStatus("COD_PENDING");
        long completedOrders = orderRepository.countByStatus("PAID") + orderRepository.countByStatus("CONFIRMED") + orderRepository.countByStatus("DELIVERED");
        long cancelledOrders = orderRepository.countByStatus("CANCELLED");
        
        // Product stats
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.findAll().stream()
                .filter(p -> p.getActive() != null && p.getActive())
                .count();
        long inactiveProducts = totalProducts - activeProducts;
        
        // Brand stats
        long totalBrands = brandRepository.count();
        
        // Revenue (from completed orders)
        java.math.BigDecimal totalRevenue = orderRepository.findAll().stream()
                .filter(o -> List.of("PAID", "CONFIRMED", "DELIVERED").contains(o.getStatus()))
                .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        
        stats.put("users", Map.of(
                "total", totalUsers,
                "active", activeUsers,
                "inactive", inactiveUsers,
                "admin", adminCount,
                "staff", staffCount
        ));
        
        stats.put("orders", Map.of(
                "total", totalOrders,
                "pending", pendingOrders,
                "completed", completedOrders,
                "cancelled", cancelledOrders
        ));
        
        stats.put("products", Map.of(
                "total", totalProducts,
                "active", activeProducts,
                "inactive", inactiveProducts
        ));
        
        stats.put("brands", Map.of("total", totalBrands));
        stats.put("revenue", totalRevenue);
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/admin/dashboard/recent-orders")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getRecentOrders(
            @RequestParam(defaultValue = "10") int limit) {
        List<org.fsm.entity.Order> orders = orderRepository.findAll(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt")
        ).stream().limit(limit).toList();
        
        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("id", order.getId());
            orderMap.put("orderCode", order.getOrderCode());
            orderMap.put("customerName", order.getUser() != null ? order.getUser().getFullName() : "Guest");
            orderMap.put("totalAmount", order.getTotalAmount());
            orderMap.put("status", order.getStatus());
            orderMap.put("createdAt", order.getCreatedAt());
            return orderMap;
        }).toList();
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/admin/dashboard/top-products")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getTopProducts(
            @RequestParam(defaultValue = "5") int limit) {
        // Get all completed orders
        List<org.fsm.entity.Order> completedOrders = orderRepository.findAll().stream()
                .filter(o -> List.of("PAID", "CONFIRMED", "DELIVERED").contains(o.getStatus()))
                .toList();
        
        // Count product sales
        Map<Long, Integer> productSales = new HashMap<>();
        for (org.fsm.entity.Order order : completedOrders) {
            List<org.fsm.entity.OrderItem> items = orderItemRepository.findByOrder(order);
            for (org.fsm.entity.OrderItem item : items) {
                if (item.getProduct() != null) {
                    Long productId = item.getProduct().getId();
                    int quantity = item.getQty() != null ? item.getQty() : 0;
                    productSales.put(productId, productSales.getOrDefault(productId, 0) + quantity);
                }
            }
        }
        
        // Sort by sales and get top products
        List<Map<String, Object>> topProducts = productSales.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(limit)
                .map(entry -> {
                    org.fsm.entity.Product product = productRepository.findById(entry.getKey()).orElse(null);
                    if (product == null) return null;
                    Map<String, Object> productMap = new HashMap<>();
                    productMap.put("id", product.getId());
                    productMap.put("title", product.getTitle());
                    productMap.put("sales", entry.getValue());
                    return productMap;
                })
                .filter(p -> p != null)
                .toList();
        
        return ResponseEntity.ok(topProducts);
    }

    // API lấy thông tin user để hiển thị lên Modal
    @GetMapping("/admin/users/{id}")
    @ResponseBody
    public ResponseEntity<?> getUser(@PathVariable Long id, HttpSession session) {
        try {
            Long currentUserId = sessionService.getCurrentUserId(session);
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

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
            response.put("role", roleInfo);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error loading user");
        }
    }

    @GetMapping("/admin/check-email")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email,
            @RequestParam(required = false) Long id) {
        boolean exists = userRepository.existsByEmail(email);
        if (id != null) {
            User existingUser = userRepository.findById(id).orElse(null);
            if (existingUser != null && existingUser.getEmail().equals(email)) {
                exists = false;
            }
        }
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    // ==========================================
    // 2. ACTION METHODS (AJAX - JSON)
    // ==========================================

    // DTO để hứng dữ liệu JSON từ form save
    @Data
    public static class UserDto {
        private Long id;
        private String fullName;
        private String email;
        private String password;
        private Boolean resetPassword;
        private String roleCode;
    }

    @PostMapping("/admin/users/save")
    @ResponseBody
    public ResponseEntity<?> saveUser(@RequestBody UserDto userDto, HttpSession session, HttpServletRequest request) {
        try {
            Long currentUserId = sessionService.getCurrentUserId(session);
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            if (currentUser == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired");

            Role role = roleRepository.findByCode(userDto.getRoleCode())
                    .orElseThrow(() -> new RuntimeException("Role not found"));

            // Validate quyền admin
            if ("ROLE_ADMIN".equals(userDto.getRoleCode()) && !"ROLE_ADMIN".equals(currentUser.getRole().getCode())) {
                return ResponseEntity.badRequest().body("Only admin can assign admin role.");
            }

            User user;
            boolean isNew = (userDto.getId() == null);
            String action;
            Map<String, Object> changes = new HashMap<>();

            if (!isNew) {
                // --- UPDATE ---
                if (userDto.getId().equals(currentUserId)) {
                    return ResponseEntity.badRequest().body("You cannot edit your own account here.");
                }
                user = userRepository.findById(userDto.getId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

                if (!user.getEmail().equals(userDto.getEmail()) && userRepository.existsByEmail(userDto.getEmail())) {
                    return ResponseEntity.badRequest().body("Email already exists.");
                }

                if (!user.getFullName().equals(userDto.getFullName()))
                    changes.put("fullName", userDto.getFullName());

                if (Boolean.TRUE.equals(userDto.getResetPassword())) {
                    user.setPassword(passwordEncoder.encode("12345678"));
                    changes.put("password", "Reset to default");
                }

                user.setFullName(userDto.getFullName());
                user.setEmail(userDto.getEmail());
                user.setRole(role);
                action = "UPDATE";
            } else {
                // --- CREATE ---
                if (userRepository.existsByEmail(userDto.getEmail())) {
                    return ResponseEntity.badRequest().body("Email already exists.");
                }
                if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
                    return ResponseEntity.badRequest().body("Password is required.");
                }
                if (!PASSWORD_PATTERN.matcher(userDto.getPassword()).matches()) {
                    return ResponseEntity.badRequest()
                            .body("Password weak: Need 8+ chars, 1 Upper, 1 Number, 1 Special.");
                }

                user = new User();
                user.setFullName(userDto.getFullName());
                user.setEmail(userDto.getEmail());
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
                user.setRole(role);
                user.setActive(true);
                action = "CREATE";
                changes.put("email", userDto.getEmail());
            }

            user = userRepository.save(user);
            auditLogService.createAuditLog(currentUser, "User", user.getId().toString(), action, changes, request);

            return ResponseEntity.ok("User saved successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/admin/users/{id}/toggle-active")
    @ResponseBody
    public ResponseEntity<?> toggleActive(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        try {
            Long currentUserId = sessionService.getCurrentUserId(session);
            if (id.equals(currentUserId))
                return ResponseEntity.badRequest().body("Cannot deactivate your own account.");

            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            if ("ROLE_ADMIN".equals(user.getRole().getCode()))
                return ResponseEntity.badRequest().body("Cannot deactivate admin accounts.");

            boolean oldStatus = user.getActive();
            user.setActive(!oldStatus);
            userRepository.save(user);

            User currentUser = userRepository.findById(currentUserId).orElse(null);
            Map<String, Object> changes = new HashMap<>();
            changes.put("status", user.getActive() ? "Active" : "Inactive");
            changes.put("userEmail", user.getEmail());
            changes.put("userFullName", user.getFullName() != null ? user.getFullName() : user.getEmail());
            auditLogService.createAuditLog(currentUser, "User", id.toString(), "UPDATE", changes, request);

            return ResponseEntity.ok("Status updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/users/{id}/delete")
    @ResponseBody
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpSession session, HttpServletRequest request) {
        try {
            Long currentUserId = sessionService.getCurrentUserId(session);
            if (id.equals(currentUserId))
                return ResponseEntity.badRequest().body("Cannot delete yourself.");

            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
            if ("ROLE_ADMIN".equals(user.getRole().getCode()))
                return ResponseEntity.badRequest().body("Cannot delete admin.");
            if (user.getActive())
                return ResponseEntity.badRequest().body("User must be deactivated first.");

            User currentUser = userRepository.findById(currentUserId).orElse(null);
            Map<String, Object> changes = new HashMap<>();
            changes.put("email", user.getEmail());

            userRepository.delete(user);
            auditLogService.createAuditLog(currentUser, "User", id.toString(), "DELETE", changes, request);

            return ResponseEntity.ok("User deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Edit Role giữ nguyên dạng redirect vì code UI bạn chưa yêu cầu sửa phần
    // dropdown
    @PostMapping("/admin/users/{id}/edit-role")
    @ResponseBody // Trả về data cho AJAX
    public ResponseEntity<?> editUserRole(
            @PathVariable Long id,
            @RequestParam String roleCode,
            HttpSession session,
            HttpServletRequest request) {

        try {
            // 1. Kiểm tra Session & User hiện tại
            Long currentUserId = sessionService.getCurrentUserId(session);
            User currentUser = userRepository.findById(currentUserId).orElse(null);

            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired. Please login again.");
            }

            // 2. Validate logic cơ bản (Fail-fast)
            if (id.equals(currentUserId)) {
                return ResponseEntity.badRequest().body("You cannot change your own role.");
            }

            // Chỉ cho phép set quyền STAFF hoặc USER (Hard check string để đỡ tốn query DB)
            if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_USER".equals(roleCode)) {
                return ResponseEntity.badRequest().body("Invalid role selected. Only Staff or User allowed.");
            }

            // 3. Lấy User mục tiêu
            User targetUser = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // 4. Validate quyền Admin (Bảo mật nghiệp vụ)
            if ("ROLE_ADMIN".equals(targetUser.getRole().getCode())) {
                return ResponseEntity.badRequest().body("Cannot modify an Administrator account.");
            }

            // 5. Lấy Role mới từ DB
            Role newRole = roleRepository.findByCode(roleCode)
                    .orElseThrow(() -> new RuntimeException("Role code not found in database"));

            // 6. Thực hiện Update
            String oldRoleCode = targetUser.getRole().getCode();

            // Nếu role không đổi thì return luôn, đỡ tốn công save và log
            if (oldRoleCode.equals(roleCode)) {
                return ResponseEntity.ok("Role is already " + newRole.getName());
            }

            targetUser.setRole(newRole);
            userRepository.save(targetUser);

            // 7. Ghi Audit Log (Sử dụng Map.of cho Java 9+ cho gọn, hoặc HashMap như cũ)
            Map<String, Object> changes = new HashMap<>();
            changes.put("oldRole", oldRoleCode);
            changes.put("newRole", roleCode);

            auditLogService.createAuditLog(
                    currentUser,
                    "User",
                    id.toString(),
                    "UPDATE",
                    changes,
                    request);

            // 8. Trả về Success Message
            return ResponseEntity.ok("Role updated successfully to " + newRole.getName());

        } catch (Exception e) {
            // Catch-all để frontend không bị treo nếu có lỗi hệ thống
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Delete single audit log
     */
    @DeleteMapping("/admin/audit-log/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAuditLog(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            auditLogService.deleteAuditLog(id);
            response.put("success", true);
            response.put("message", "Audit log deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete multiple audit logs
     */
    @DeleteMapping("/admin/audit-log/bulk")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteAuditLogs(@RequestBody List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (ids == null || ids.isEmpty()) {
                response.put("success", false);
                response.put("message", "No audit logs selected");
                return ResponseEntity.badRequest().body(response);
            }
            
            auditLogService.deleteAuditLogs(ids);
            response.put("success", true);
            response.put("message", ids.size() + " audit log(s) deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
