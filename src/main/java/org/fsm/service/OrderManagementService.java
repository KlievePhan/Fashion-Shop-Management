package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Order;
import org.fsm.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderManagementService {

    private final OrderRepository orderRepository;
    private final EmailService emailService;

    /**
     * Get all orders with pagination
     */
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    /**
     * Get orders filtered by status
     */
    public Page<Order> getOrdersByStatus(String status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
    }

    /**
     * Update order status from PENDING to PAID
     */
    @Transactional
    public Order updateOrderToPaid(Long orderId) {
        Order order = getOrderById(orderId);

        // Validate current status - handle both PENDING and COD_PENDING
        String currentStatus = order.getStatus().toUpperCase();
        if (!currentStatus.equals("PENDING") && !currentStatus.equals("COD_PENDING")) {
            throw new IllegalStateException("Only PENDING or COD_PENDING orders can be updated to PAID. Current status: " + order.getStatus());
        }

        // Update status to PAID (uppercase to match database convention)
        order.setStatus("PAID");
        return orderRepository.save(order);
    }

    /**
     * Get total count of all orders
     */
    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    /**
     * Get count of orders by status
     */
    public long getOrdersCountByStatus(String status) {
        return orderRepository.countByStatus(status);
    }

    /**
     * Cancel order with reason and send email notification to customer
     */
    @Transactional
    public Order cancelOrder(Long orderId, String reason) {
        Order order = getOrderById(orderId);

        // Validate current status - only allow cancel PENDING or COD_PENDING orders
        String currentStatus = order.getStatus().toUpperCase();
        if (!currentStatus.equals("PENDING") && !currentStatus.equals("COD_PENDING")) {
            throw new IllegalStateException(
                "Only PENDING or COD_PENDING orders can be cancelled. Current status: " + order.getStatus()
            );
        }

        // Update status to CANCELLED
        order.setStatus("CANCELLED");
        Order savedOrder = orderRepository.save(order);

        // Send email notification to customer
        if (order.getUser() != null && order.getUser().getEmail() != null) {
            String customerEmail = order.getUser().getEmail();
            String customerName = order.getUser().getFullName() != null 
                ? order.getUser().getFullName() 
                : order.getUser().getDisplayName();
            
            try {
                emailService.sendOrderCancellationEmail(
                    customerEmail,
                    customerName,
                    order.getOrderCode(),
                    reason != null ? reason : "Product out of stock or technical issue"
                );
                // Log success (you can use proper logger instead)
                System.out.println("Cancellation email sent to: " + customerEmail + " for order: " + order.getOrderCode());
            } catch (Exception e) {
                // Log error but don't fail the cancellation
                System.err.println("Failed to send cancellation email to " + customerEmail + ": " + e.getMessage());
            }
        } else {
            System.out.println("Order " + order.getOrderCode() + " has no customer email - skipping email notification");
        }

        return savedOrder;
    }
}