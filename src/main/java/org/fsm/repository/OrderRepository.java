package org.fsm.repository;

import org.fsm.entity.Order;
import org.fsm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderCode);

    Page<Order> findByUser(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, String status, Pageable pageable);
    Page<Order> findByUserAndOrderCodeContaining(User user, String orderCode, Pageable pageable);

    long countByUser(User user);
    long countByUserAndStatus(User user, String status);
}