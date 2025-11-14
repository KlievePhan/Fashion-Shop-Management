// org.fsm.service.CartService
package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.*;
import org.fsm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public void addToCart(User user, Long variantId, Integer qty) {
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Product variant not found"));
        CartItem cartItem = cartItemRepository.findByCartAndProductVariant(cart, variant)
                .orElse(null);
        if (cartItem != null) {
            cartItem.setQty(cartItem.getQty() + qty);
        } else {
            cartItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .qty(qty)
                    .build();
        }
        cartItemRepository.save(cartItem);
    }

    public int getCartItemCount(User user) {
        return cartRepository.findByUser(user)
                .map(cart -> cartItemRepository.findByCart(cart).stream()
                        .mapToInt(CartItem::getQty)
                        .sum())
                .orElse(0);
    }

}
