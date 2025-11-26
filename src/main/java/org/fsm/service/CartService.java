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
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ObjectMapper objectMapper;

    /**
     * ⭐ FIXED: Add to cart với selectedOptions - Sử dụng normalized JSON
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

        // 4. ⭐ NORMALIZE và convert selectedOptions thành JSON string
        String optionsJson = normalizeAndConvertToJson(selectedOptions);

        System.out.println("🔍 DEBUG - Looking for existing item:");
        System.out.println("   Cart ID: " + cart.getId());
        System.out.println("   Product ID: " + productId);
        System.out.println("   Options JSON: " + optionsJson);

        // 5. ⭐ DEBUG: List all existing items for this product
        cartItemRepository.findByCartAndProduct(cart, product).forEach(item -> {
            System.out.println("   Existing item: ID=" + item.getId() +
                    ", JSON=[" + item.getSelectedOptionsJson() + "]" +
                    ", Length=" + item.getSelectedOptionsJson().length());
        });

        // 6. Kiểm tra xem item với options này đã có trong cart chưa
        CartItem existingItem = cartItemRepository
                .findByCartAndProductAndSelectedOptionsJson(cart.getId(), product.getId(), optionsJson)
                .orElse(null);

        if (existingItem != null) {
            // ⭐ Tăng số lượng
            int newQty = existingItem.getQty() + qty;
            existingItem.setQty(newQty);
            existingItem.setUpdatedAt(LocalDateTime.now());
            cartItemRepository.save(existingItem);

            System.out.println("✅ Updated existing item ID=" + existingItem.getId() +
                    " - New quantity: " + newQty);
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

            System.out.println("✅ Created new cart item with quantity: " + qty);
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

        for (Map.Entry<String, String> entry : selectedOptions.entrySet()) {
            String optionType = entry.getKey().toUpperCase();
            String optionValue = entry.getValue();

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
     * ⭐ CRITICAL FIX: Normalize và convert Map thành JSON string
     * - Đảm bảo keys luôn sorted alphabetically
     * - Không có khoảng trắng
     * - Format consistent: {"color":"Black","size":"M"}
     */
    private String normalizeAndConvertToJson(Map<String, String> map) {
        try {
            // ⭐ Sử dụng TreeMap để tự động sort keys (CASE-SENSITIVE alphabetical order)
            Map<String, String> sortedMap = new TreeMap<>();

            // Normalize: lowercase keys, trim values
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey().toLowerCase().trim();
                String value = entry.getValue().trim();
                sortedMap.put(key, value);
            }

            // ⭐ Convert to JSON
            String json = objectMapper.writeValueAsString(sortedMap);

            // ⭐ CRITICAL: Remove ALL spaces (after colons, after commas, everywhere)
            json = json.replaceAll("\\s+", "");

            System.out.println("🔧 Normalized JSON: [" + json + "] (length: " + json.length() + ")");
            return json;

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