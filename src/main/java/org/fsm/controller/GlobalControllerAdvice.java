package org.fsm.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.fsm.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Makes user session data available to all Thymeleaf templates
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;
    private final CartService cartService;  // ← THÊM DÒNG NÀY

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated(HttpSession session) {
        // Check session first
        Boolean sessionAuth = (Boolean) session.getAttribute("isAuthenticated");
        if (Boolean.TRUE.equals(sessionAuth)) {
            return true;
        }

        // Fallback to Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal());
    }

    @ModelAttribute("currentUserId")
    public Long currentUserId(HttpSession session) {
        return (Long) session.getAttribute("userId");
    }

    @ModelAttribute("currentUserEmail")
    public String currentUserEmail(HttpSession session) {
        String email = (String) session.getAttribute("userEmail");
        if (email != null) return email;

        // Fallback to Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return null;
    }

    @ModelAttribute("currentUserFullName")
    public String currentUserFullName(HttpSession session) {
        return (String) session.getAttribute("userFullName");
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole(HttpSession session) {
        return (String) session.getAttribute("userRole");
    }

    @ModelAttribute("isAdmin")
    public boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("userRole");
        return "ROLE_ADMIN".equals(role);
    }

    @ModelAttribute("currentUser")
    public User currentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            return userRepository.findById(userId).orElse(null);
        }

        // Fallback to Spring Security
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String email = auth.getName();
            return userRepository.findByEmail(email).orElse(null);
        }

        return null;
    }

    // ============ THÊM PHẦN NÀY ĐỂ HIỂN THỊ SỐ LƯỢNG CART ============
    /**
     * Số lượng sản phẩm trong giỏ hàng - hiển thị ở header badge
     */
    @ModelAttribute("cartItemCount")
    public Integer cartItemCount(HttpSession session) {
        // Lấy user hiện tại
        User user = currentUser(session);
        
        if (user == null) {
            return 0;
        }

        try {
            return cartService.getCartItemCount(user);
        } catch (Exception e) {
            // Log error nếu cần
            System.err.println("Error getting cart item count: " + e.getMessage());
            return 0;
        }
    }
    // ============ KẾT THÚC PHẦN THÊM ============
}