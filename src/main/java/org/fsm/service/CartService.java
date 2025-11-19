package org.fsm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.*;
import org.fsm.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ObjectMapper objectMapper;

    /**
     * ⭐ UPDATED: Add to cart với selectedOptions
     *
     * @param user            User hiện tại
     * @param productId       ID của product
     * @param selectedOptions Map chứa {"size":"41", "color":"Red"}
     * @param qty             Số lượng
     */
    @Transactional
    public void addToCart(User user, Long productId, Map<String, String> selectedOptions, Integer qty) {
        // 1. Lấy hoặc tạo cart
        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()));

        // 2. Lấy product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // 3. Tính giá dựa trên base_price + price_adjustments
        BigDecimal finalPrice = calculatePrice(product, selectedOptions);

        // 4. Convert selectedOptions thành JSON string
        String optionsJson = convertToJson(selectedOptions);

        // 5. Kiểm tra xem item với options này đã có trong cart chưa
        CartItem existingItem = cartItemRepository
                .findByCartAndProductAndSelectedOptionsJson(cart, product, optionsJson)
                .orElse(null);

        if (existingItem != null) {
            // Tăng số lượng
            existingItem.setQty(existingItem.getQty() + qty);
            existingItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(existingItem);
        } else {
            // Tạo cart item mới
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .selectedOptionsJson(optionsJson)
                    .unitPrice(finalPrice)
                    .qty(qty)
                    .addedAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            cartItemRepository.save(newItem);
        }

        // Update cart timestamp
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    /**
     * Tính giá cuối cùng = base_price + sum(price_adjustments)
     */
    private BigDecimal calculatePrice(Product product, Map<String, String> selectedOptions) {
        BigDecimal price = product.getBasePrice();

        // Cộng price adjustment của từng option
        for (Map.Entry<String, String> entry : selectedOptions.entrySet()) {
            String optionType = entry.getKey().toUpperCase(); // "SIZE" hoặc "COLOR"
            String optionValue = entry.getValue(); // "41", "Red"

            ProductOption option = productOptionRepository
                    .findByProductAndOptionTypeAndOptionValue(product, optionType, optionValue)
                    .orElse(null);

            if (option != null && option.getPriceAdjustment() != null) {
                price = price.add(option.getPriceAdjustment());
            }
        }

        return price;
    }

    /**
     * Convert Map thành JSON string
     */
    private String convertToJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert options to JSON", e);
        }
    }

    /**
     * Đếm tổng số items trong cart
     */
    public int getCartItemCount(User user) {
        return cartRepository.findByUser(user)
                .map(cart -> cartItemRepository.findByCart(cart).stream()
                        .mapToInt(CartItem::getQty)
                        .sum())
                .orElse(0);
    }

    /**
     * Lấy cart của user
     */
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> cartRepository.save(
                        Cart.builder()
                                .user(user)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .build()));
    }
}