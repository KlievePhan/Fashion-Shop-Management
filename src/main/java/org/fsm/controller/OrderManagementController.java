package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Order;
import org.fsm.service.OrderManagementService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/staff/orders")
@RequiredArgsConstructor
public class OrderManagementController {

    private final OrderManagementService orderManagementService;

    /**
     * Get all orders with pagination and optional status filter
     */
    @GetMapping
    public String getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> ordersPage;

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("all")) {
            ordersPage = orderManagementService.getOrdersByStatus(status, pageable);
        } else {
            ordersPage = orderManagementService.getAllOrders(pageable);
        }

        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", ordersPage.getTotalPages());
        model.addAttribute("totalItems", ordersPage.getTotalElements());
        model.addAttribute("selectedStatus", status);

        return "staff";
    }

    /**
     * Get order details by ID (AJAX endpoint)
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderManagementService.getOrderById(id);
        return ResponseEntity.ok(order);
    }

    /**
     * Update order status from PENDING to PAID
     */
    @PostMapping("/{id}/update-to-paid")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateOrderToPaid(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            Order updatedOrder = orderManagementService.updateOrderToPaid(id);
            response.put("success", true);
            response.put("message", "Order status updated to PAID successfully");
            response.put("newStatus", updatedOrder.getStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to update order status");
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Cancel order with reason
     */
    @PostMapping("/{id}/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String reason
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            Order cancelledOrder = orderManagementService.cancelOrder(id, reason);
            response.put("success", true);
            response.put("message", "Order cancelled successfully. Customer has been notified via email.");
            response.put("newStatus", cancelledOrder.getStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to cancel order: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get order statistics
     */
    @GetMapping("/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Long>> getOrderStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", orderManagementService.getTotalOrdersCount());
        stats.put("pending", orderManagementService.getOrdersCountByStatus("pending"));
        stats.put("paid", orderManagementService.getOrdersCountByStatus("paid"));
        stats.put("shipped", orderManagementService.getOrdersCountByStatus("shipped"));

        return ResponseEntity.ok(stats);
    }
}