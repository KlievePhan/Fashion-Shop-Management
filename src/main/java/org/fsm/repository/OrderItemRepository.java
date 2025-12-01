package org.fsm.repository;

import org.fsm.entity.Order;
import org.fsm.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

    // Analytics helpers
    List<OrderItem> findByOrderIn(Collection<Order> orders);
}