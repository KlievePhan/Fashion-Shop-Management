package org.fsm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.Product;
import org.fsm.entity.User;
import org.fsm.entity.WishList;
import org.fsm.repository.ProductRepository;
import org.fsm.repository.WishListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    /**
     * ⭐ FIXED: Add to wishlist with REQUIRED selectedOptions (same as cart)
     */
    @Transactional
    public void addToWishList(User user, Long productId, Map<String, String> selectedOptions) {
        // Validate selectedOptions
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            throw new RuntimeException("Please select size and color before adding to wishlist");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ⭐ Normalize and convert to JSON (same as CartService)
        String optionsJson = normalizeAndConvertToJson(selectedOptions);

        System.out.println("❤️ DEBUG - Adding to wishlist:");
        System.out.println("   User ID: " + user.getId());
        System.out.println("   Product ID: " + productId);
        System.out.println("   Options JSON: [" + optionsJson + "]");

        // Check if already exists with same options
        boolean exists = wishListRepository.existsByUserAndProductAndSelectedOptionsJson(
                user, product, optionsJson);

        if (exists) {
            throw new RuntimeException("This item is already in your wishlist");
        }

        // Create new wishlist item
        WishList wishListItem = WishList.builder()
                .user(user)
                .product(product)
                .selectedOptionsJson(optionsJson)
                .addedAt(LocalDateTime.now())
                .build();

        wishListRepository.save(wishListItem);
        System.out.println("✅ Added to wishlist successfully");
    }

    /**
     * Remove from wishlist
     */
    @Transactional
    public void removeFromWishList(Long userId, Long itemId) {
        wishListRepository.findById(itemId)
                .ifPresent(item -> {
                    if (item.getUser().getId().equals(userId)) {
                        wishListRepository.delete(item);
                    }
                });
    }

    /**
     * Get count of wishlist items
     */
    public int getWishListCount(User user) {
        return (int) wishListRepository.countByUser(user);
    }

    /**
     * ⭐ CRITICAL: Normalize and convert Map to JSON string (same as CartService)
     */
    private String normalizeAndConvertToJson(Map<String, String> map) {
        try {
            // Use TreeMap for automatic alphabetical sorting
            Map<String, String> sortedMap = new TreeMap<>();

            // Normalize: lowercase keys, trim values
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey().toLowerCase().trim();
                String value = entry.getValue().trim();
                sortedMap.put(key, value);
            }

            // Convert to JSON
            String json = objectMapper.writeValueAsString(sortedMap);

            // Remove ALL spaces
            json = json.replaceAll("\\s+", "");

            System.out.println("🔧 Normalized JSON: [" + json + "]");
            return json;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert options to JSON", e);
        }
    }
}