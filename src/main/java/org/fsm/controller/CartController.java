// org.fsm.controller.CartController
package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Cart;
import org.fsm.entity.CartItem;
import org.fsm.entity.User;
import org.fsm.repository.CartItemRepository;
import org.fsm.repository.CartRepository;
import org.fsm.repository.UserRepository;
import org.fsm.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

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
        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(100)) > 0 ? BigDecimal.ZERO : BigDecimal.valueOf(10);
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

}
