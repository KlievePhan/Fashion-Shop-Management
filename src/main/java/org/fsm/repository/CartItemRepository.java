package org.fsm.repository;

import org.fsm.entity.Cart;
import org.fsm.entity.CartItem;
import org.fsm.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Tìm tất cả items trong cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * ⭐ SIMPLE VERSION: Exact string match
     */
    @Query("SELECT ci FROM CartItem ci " +
            "WHERE ci.cart.id = :cartId " +
            "AND ci.product.id = :productId " +
            "AND ci.selectedOptionsJson = :optionsJson")
    Optional<CartItem> findByCartAndProductAndSelectedOptionsJson(
            @Param("cartId") Long cartId,
            @Param("productId") Long productId,
            @Param("optionsJson") String optionsJson
    );

    /**
     * ⭐ ALTERNATIVE: Exact match (nếu JSON đã được normalized đúng)
     */
    @Query("SELECT ci FROM CartItem ci " +
            "WHERE ci.cart = :cart " +
            "AND ci.product = :product " +
            "AND ci.selectedOptionsJson = :optionsJson")
    Optional<CartItem> findExactMatch(
            @Param("cart") Cart cart,
            @Param("product") Product product,
            @Param("optionsJson") String optionsJson
    );

    /**
     * ⭐ DEBUG: Tìm tất cả items của product trong cart
     */
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart AND ci.product = :product")
    List<CartItem> findByCartAndProduct(
            @Param("cart") Cart cart,
            @Param("product") Product product
    );

    /**
     * Xóa tất cả items trong cart
     */
    void deleteByCart(Cart cart);

    /**
     * Đếm số items trong cart
     */
    long countByCart(Cart cart);
}