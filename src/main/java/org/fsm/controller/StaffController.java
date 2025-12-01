package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Order;
import org.fsm.entity.Product;
import org.fsm.entity.User;
import org.fsm.repository.AuditLogRepository;
import org.fsm.repository.BrandRepository;
import org.fsm.repository.CategoryRepository;
import org.fsm.repository.OrderRepository;
import org.fsm.repository.ProductRepository;
import org.fsm.repository.UserRepository;
import org.fsm.service.AuditLogService;
import org.fsm.service.SessionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import java.util.List;

@RequiredArgsConstructor
@Controller
public class StaffController {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final AuditLogService auditLogService;
    private final SessionService sessionService;
    private final UserRepository userRepository;

    @GetMapping("/staff")
    public String staff(
            Model model,
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        // ===== Products / Brands / Categories cho staff.html =====
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);

        model.addAttribute("brands", brandRepository.findAll());

        // Categories - QUAN TRỌNG → để Category ở staff.html có data
        model.addAttribute("categories", categoryRepository.findAll());

        // ===== Orders - Load all orders sorted by createdAt descending =====
        List<Order> orders = orderRepository.findAll(Sort.by("createdAt").descending());
        model.addAttribute("orders", orders);

        // ===== Audit logs (nếu staff.html có dùng) =====
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var auditLogs = auditLogService.getAllAuditLogs(pageable);
        model.addAttribute("auditLogs", auditLogs.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", auditLogs.getTotalPages());

        // ===== Current staff info (nếu cần show trong header) =====
        Long currentUserId = sessionService.getCurrentUserId(session);
        model.addAttribute("currentUserId", currentUserId);

        if (currentUserId != null) {
            User currentUser = userRepository.findById(currentUserId).orElse(null);
            model.addAttribute("currentUser", currentUser);
        }

        // Trả về staff.html
        return "staff";
    }
}