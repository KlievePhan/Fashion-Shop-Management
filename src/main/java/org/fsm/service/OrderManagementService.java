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
}