package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Cart;
import org.fsm.entity.CartItem;
import org.fsm.entity.User;
import org.fsm.repository.CartItemRepository;
import org.fsm.repository.CartRepository;
import org.fsm.repository.UserRepository;
import org.fsm.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @GetMapping("/cart")
    public String getCart(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Cart cart = cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .user(user)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return cartRepository.save(newCart);
                });

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(10);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1));
        BigDecimal total = subtotal.add(shipping).add(tax);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipping", shipping);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);
        model.addAttribute("currentPath", "/cart");

        return "cart";
    }

    @PostMapping("/add")
    @ResponseBody
    public String addToCart(Principal principal,
            @RequestParam("variantId") Long variantId,
            @RequestParam("qty") Integer qty) {
        if (principal == null) {
            return "User not logged in";
        }
        String email = principal.getName();
        User user = userRepository.findByEmail(email).get();
        cartService.addToCart(user, variantId, qty);
        return "success";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            Principal principal,
            @RequestParam("itemId") Long itemId,
            @RequestParam("qty") Integer qty) {

        Map<String, Object> response = new HashMap<>();

        try {
            if (principal == null) {
                response.put("success", false);
                response.put("message", "User not logged in");
                return ResponseEntity.status(401).body(response);
            }

            if (qty < 1) {
                response.put("success", false);
                response.put("message", "Quantity must be at least 1");
                return ResponseEntity.badRequest().body(response);
            }

            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            CartItem cartItem = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            // Verify ownership
            if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.status(403).body(response);
            }

            // Update quantity
            cartItem.setQty(qty);
            cartItemRepository.save(cartItem);

            // Recalculate totals
            List<CartItem> cartItems = cartItemRepository.findByCart(cartItem.getCart());
            Map<String, BigDecimal> totals = calculateTotals(cartItems);

            response.put("success", true);
            response.put("itemTotal", cartItem.getProductVariant().getPrice().multiply(BigDecimal.valueOf(qty)));
            response.put("subtotal", totals.get("subtotal"));
            response.put("shipping", totals.get("shipping"));
            response.put("tax", totals.get("tax"));
            response.put("total", totals.get("total"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @DeleteMapping("/delete/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCartItem(
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

            CartItem cartItem = cartItemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("Cart item not found"));

            // Verify ownership
            if (!cartItem.getCart().getUser().getId().equals(user.getId())) {
                response.put("success", false);
                response.put("message", "Unauthorized access");
                return ResponseEntity.status(403).body(response);
            }

            Cart cart = cartItem.getCart();
            cartItemRepository.delete(cartItem);

            // Recalculate totals
            List<CartItem> cartItems = cartItemRepository.findByCart(cart);
            Map<String, BigDecimal> totals = calculateTotals(cartItems);

            response.put("success", true);
            response.put("message", "Item removed successfully");
            response.put("subtotal", totals.get("subtotal"));
            response.put("shipping", totals.get("shipping"));
            response.put("tax", totals.get("tax"));
            response.put("total", totals.get("total"));
            response.put("isEmpty", cartItems.isEmpty());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Helper method to calculate cart totals
    private Map<String, BigDecimal> calculateTotals(List<CartItem> cartItems) {
        Map<String, BigDecimal> totals = new HashMap<>();

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProductVariant().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQty())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(100)) > 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(10);

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1));
        BigDecimal total = subtotal.add(shipping).add(tax);

        totals.put("subtotal", subtotal);
        totals.put("shipping", shipping);
        totals.put("tax", tax);
        totals.put("total", total);

        return totals;
    }

    @PostMapping("/addCart")
    @ResponseBody // ⭐ THÊM annotation này nếu chưa có
    public Map<String, Object> addToCartJson(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return Map.of("success", false, "message", "Please login first");
        }

        User user = (User) authentication.getPrincipal();
        Long variantId = Long.valueOf(request.get("variantId").toString());
        Integer qty = Integer.valueOf(request.get("qty").toString());

        try {
            cartService.addToCart(user, variantId, qty);
            return Map.of("success", true);
        } catch (Exception e) {
            return Map.of("success", false, "message", e.getMessage());
        }
    }

    // ⭐ SỬA LẠI METHOD NÀY - THÊM @ResponseBody
    @GetMapping("/count")
    @ResponseBody // ← QUAN TRỌNG: Phải có annotation này!
    public Map<String, Integer> getCartCount(Principal principal) {
        int count = 0;

        if (principal != null) {
            String email = principal.getName();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user != null) {
                count = cartService.getCartItemCount(user);
            }
        }

        return Map.of("count", count);
    }
}