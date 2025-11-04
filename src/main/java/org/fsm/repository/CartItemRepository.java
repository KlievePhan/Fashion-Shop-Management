package org.fsm.repository;

import org.fsm.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    void deleteByCartIdAndProductVariantId(Long cartId, Long productVariantId);
}
