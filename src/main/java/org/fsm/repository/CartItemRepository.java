package org.fsm.repository;

import org.fsm.entity.Cart;
import org.fsm.entity.CartItem;
import org.fsm.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCartId(Long cartId);
    void deleteByCartIdAndProductVariantId(Long cartId, Long productVariantId);
    Optional<CartItem> findByCartAndProductVariant(Cart cart, ProductVariant variant);

    List<CartItem> findByCart(Cart cart);
}
