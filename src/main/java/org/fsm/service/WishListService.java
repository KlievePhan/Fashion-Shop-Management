package org.fsm.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.Product;
import org.fsm.entity.ProductVariant;
import org.fsm.entity.User;
import org.fsm.entity.WishList;
import org.fsm.repository.ProductRepository;
import org.fsm.repository.ProductVariantRepository;
import org.fsm.repository.WishListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ObjectMapper objectMapper;

    /**
     * Add product to wishlist with optional selectedOptions
     *
     * @param user            Current user
     * @param productId       Product ID
     * @param selectedOptions Map like {"size":"41", "color":"Red"} (can be empty)
     */
    @Transactional
    public void addToWishList(User user, Long productId, Map<String, String> selectedOptions) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        Long variantId = null;

        // Only try to find variant if selectedOptions is not empty
        if (selectedOptions != null && !selectedOptions.isEmpty()) {
            String optionsJson = convertToJson(selectedOptions);

            Optional<ProductVariant> variantOpt = productVariantRepository
                    .findByProductAndAttributeJsonContaining(product, optionsJson); // or exact match

            if (variantOpt.isPresent()) {
                variantId = variantOpt.get().getId();
            }
            // → If not found → we just save without variant (still valid!)
        }

        // Check if already in wishlist (same product + same variant or no variant)
        boolean exists = wishListRepository.existsByUserIdAndProductIdAndProductVariantId(
                user.getId(), productId, variantId);

        if (!exists) {
            WishList wishListItem = WishList.builder()
                    .user(user)
                    .product(product)
                    .productVariant(variantId != null ? productVariantRepository.getReferenceById(variantId) : null)
                    .addedAt(LocalDateTime.now())
                    .build();
            wishListRepository.save(wishListItem);
        }
    }

    /**
     * Remove from wishlist
     *
     * @param userId Wishlist item ID
     * @param itemId Item ID to remove
     */
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
     * Convert Map to JSON string
     */
    private String convertToJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert options to JSON", e);
        }
    }
}