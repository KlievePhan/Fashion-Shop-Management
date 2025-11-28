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
    List<CartItem> findByCart(Cart cart);
    @Query("SELECT ci FROM CartItem ci " +
            "WHERE ci.cart = :cart " +
            "AND ci.product = :product " +
            "AND ci.selectedOptionsJson = :optionsJson")
    Optional<CartItem> findByCartAndProductAndSelectedOptionsJson(
            @Param("cart") Cart cart,
            @Param("product") Product product,
            @Param("optionsJson") String optionsJson
    );


    @Query("SELECT ci FROM CartItem ci " +
            "WHERE ci.cart = :cart " +
            "AND ci.product = :product")
    List<CartItem> findByCartAndProduct(
            @Param("cart") Cart cart,
            @Param("product") Product product
    );

    void deleteByCart(Cart cart);
    long countByCart(Cart cart);
}