package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.entity.WishList;
import org.fsm.repository.UserRepository;
import org.fsm.repository.WishListRepository;
import org.fsm.service.WishListService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;
    private final UserRepository userRepository;
    private final WishListRepository wishListRepository;

    /**
     * Display wishlist page
     */
    @GetMapping("/wishlist")
    public String getWishList(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<WishList> wishListItems = wishListRepository.findByUserIdOrderByAddedAtDesc(user.getId());

        model.addAttribute("wishListItems", wishListItems);
        model.addAttribute("currentPath", "/wishlist");

        return "wishlist";
    }

    /**
     * Add to wishlist
     * Request body: {
     *   "productId": 1,
     *   "selectedOptions": {"size":"41", "color":"Red"}
     * }
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToWishList(
            @RequestBody Map<String, Object> request,
            Principal principal) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "Please login first");
                return ResponseEntity.status(401).body(response);
            }

            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Parse request
            Long productId = Long.valueOf(request.get("productId").toString());

            @SuppressWarnings("unchecked")
            Map<String, String> selectedOptions = (Map<String, String>) request.getOrDefault("selectedOptions", new HashMap<>());

            // Add to wishlist
            wishListService.addToWishList(user, productId, selectedOptions);

            response.put("success", true);
            response.put("message", "Product added to wishlist successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Remove wishlist item
     */
    @DeleteMapping("/delete/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteWishListItem(
            Principal principal,
            @PathVariable("itemId") Long itemId) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "User not logged in");
                return ResponseEntity.status(401).body(response);
            }

            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            WishList wishListItem = wishListRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Wishlist item not found"));

            // Verify ownership
            if (!wishListItem.getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.status(403).body(response);
            }

            wishListRepository.delete(wishListItem);

            response.put("success", true);
            response.put("message", "Item removed successfully");
            response.put("isEmpty", wishListRepository.countByUser(user) == 0);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get wishlist item count (for badge)
     */
    @GetMapping("/count")
    @ResponseBody
    public Map<String, Integer> getWishListCount(Principal principal) {
        int count = 0;

        if (principal != null) {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                count = wishListService.getWishListCount(user);
            }
        }

        return Map.of("count", count);
    }
}