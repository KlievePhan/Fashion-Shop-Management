package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Order;
import org.fsm.entity.OrderItem;
import org.fsm.entity.User;
import org.fsm.entity.CartItem;
import org.fsm.repository.*;
import org.fsm.service.CartService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final ProductRepository productRepository;

    /**
     * Hiển thị danh sách orders của user
     */
    @GetMapping
    public String getOrderHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage;

        // Filter logic
        if (status != null && !status.isEmpty()) {
            orderPage = orderRepository.findByUserAndStatus(user, status, pageable);
        } else if (search != null && !search.isEmpty()) {
            orderPage = orderRepository.findByUserAndOrderCodeContaining(user, search, pageable);
        } else {
            orderPage = orderRepository.findByUser(user, pageable);
        }

        // Calculate statistics
        long totalOrders = orderRepository.countByUser(user);
        long pendingOrders = orderRepository.countByUserAndStatus(user, "PENDING");
        long codPendingOrders = orderRepository.countByUserAndStatus(user, "COD_PENDING");
        long paidOrders = orderRepository.countByUserAndStatus(user, "PAID");
        long shippedOrders = orderRepository.countByUserAndStatus(user, "SHIPPED");

        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders + codPendingOrders);
        model.addAttribute("paidOrders", paidOrders);
        model.addAttribute("shippedOrders", shippedOrders);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentSearch", search);

        return "order-history";
    }

    /**
     * Xem chi tiết 1 order
     */
    @GetMapping("/{orderId}")
    public String getOrderDetail(@PathVariable Long orderId, Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Verify ownership
        if (!order.getUser().getId().equals(user.getId())) {
            return "redirect:/orders?error=unauthorized";
        }

        List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

        model.addAttribute("order", order);
        model.addAttribute("orderItems", orderItems);

        return "order-detail";
    }

    /**
     * Hủy order
     */
    @PostMapping("/{orderId}/cancel")
    @ResponseBody
    public Map<String, Object> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam(required = false) String reason,
            Principal principal) {

        if (principal == null) {
            return Map.of("success", false, "message", "Please login");
        }

        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Verify ownership
            if (!order.getUser().getId().equals(user.getId())) {
                return Map.of("success", false, "message", "Unauthorized");
            }

            // Only allow cancel if status is PENDING or COD_PENDING
            if (!order.getStatus().equals("PENDING") && !order.getStatus().equals("COD_PENDING")) {
                return Map.of("success", false, "message",
                        "Cannot cancel order with status: " + order.getStatus());
            }

            order.setStatus("CANCELLED");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            return Map.of("success", true, "message", "Order cancelled successfully");

        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * Re-order: Thêm lại các sản phẩm từ order cũ vào cart
     */
    @PostMapping("/{orderId}/reorder")
    @ResponseBody
    public Map<String, Object> reorder(@PathVariable Long orderId, Principal principal) {
        if (principal == null) {
            return Map.of("success", false, "message", "Please login");
        }

        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            // Verify ownership
            if (!order.getUser().getId().equals(user.getId())) {
                return Map.of("success", false, "message", "Unauthorized");
            }

            List<OrderItem> orderItems = orderItemRepository.findByOrder(order);

            // Add each item to cart
            int addedCount = 0;
            for (OrderItem orderItem : orderItems) {
                try {
                    // Parse selected options from JSON
                    Map<String, String> selectedOptions = parseOptions(orderItem.getSelectedOptions());

                    cartService.addToCart(
                            user,
                            orderItem.getProduct().getId(),
                            selectedOptions,
                            orderItem.getQty()
                    );
                    addedCount++;
                } catch (Exception e) {
                    // Skip if product/variant not available
                    continue;
                }
            }

            return Map.of(
                    "success", true,
                    "message", addedCount + " items added to cart",
                    "addedCount", addedCount
            );

        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * Track order by order code (public - không cần login)
     */
    @GetMapping("/track")
    public String trackOrderPage() {
        return "order-tracking";
    }

    @PostMapping("/track")
    @ResponseBody
    public Map<String, Object> trackOrder(@RequestParam String orderCode) {
        try {
            Order order = orderRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            return Map.of(
                    "success", true,
                    "orderCode", order.getOrderCode(),
                    "status", order.getStatus(),
                    "totalAmount", order.getTotalAmount(),
                    "createdAt", order.getCreatedAt().toString(),
                    "updatedAt", order.getUpdatedAt().toString()
            );

        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    /**
     * Helper: Parse JSON options
     */
    private Map<String, String> parseOptions(String json) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper =
                    new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}