package org.fsm.repository;

import org.fsm.entity.Order;
import org.fsm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, String status, Pageable pageable);
    Page<Order> findByUserAndOrderCodeContaining(User user, String orderCode, Pageable pageable);

    // New methods for staff order management
    Page<Order> findByStatus(String status, Pageable pageable);

    long countByUser(User user);
    long countByUserAndStatus(User user, String status);
    long countByStatus(String status);

    // =====================
    // Analytics helpers
    // =====================

    List<Order> findByCreatedAtAfterAndStatusIn(LocalDateTime createdAt, Collection<String> statuses);
}