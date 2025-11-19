package org.fsm.repository;

import org.fsm.entity.Cart;
import org.fsm.entity.CartItem;
import org.fsm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all cart items by cart ID
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Find all cart items by cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find cart item by cart, product and selected options JSON
     * (để check xem item này đã có trong cart chưa)
     */
    Optional<CartItem> findByCartAndProductAndSelectedOptionsJson(
            Cart cart,
            Product product,
            String selectedOptionsJson);

    /**
     * Delete all cart items by cart
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart = :cart")
    void deleteByCart(@Param("cart") Cart cart);

    /**
     * Count cart items by cart
     */
    long countByCart(Cart cart);
}