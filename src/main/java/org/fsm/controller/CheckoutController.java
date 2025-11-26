package org.fsm.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.*;
import org.fsm.repository.*;
import org.fsm.service.VNPayService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VNPayService vnPayService;

    /**
     * Hiển thị trang checkout
     */
    @GetMapping
    public String showCheckoutPage(Principal principal, Model model) {
        // Check login
        if (principal == null) {
            return "redirect:/login";
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Lấy cart items
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCart(cart);

        if (cartItems.isEmpty()) {
            return "redirect:/cart/cart";
        }

        // Tính tổng
        BigDecimal subtotal = cartItems.stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(100)) > 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(10);

        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1));
        BigDecimal total = subtotal.add(shipping).add(tax);

        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("shipping", shipping);
        model.addAttribute("tax", tax);
        model.addAttribute("total", total);

        return "checkout";
    }

    /**
     * Xử lý checkout và tạo VNPay payment URL hoặc COD order
     */
    @PostMapping("/create-payment")
    public String createPayment(
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String address,
            @RequestParam(required = false) String notes,
            @RequestParam String paymentMethod, // ⭐ NEW: vnpay or cod
            Principal principal,
            HttpServletRequest request,
            Model model) {

        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String email = principal.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Lấy cart
            Cart cart = cartRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Cart not found"));

            List<CartItem> cartItems = cartItemRepository.findByCart(cart);

            if (cartItems.isEmpty()) {
                throw new RuntimeException("Cart is empty");
            }

            // Tính tổng
            BigDecimal subtotal = cartItems.stream()
                    .map(CartItem::getLineTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shipping = subtotal.compareTo(BigDecimal.valueOf(100)) > 0
                    ? BigDecimal.ZERO
                    : BigDecimal.valueOf(10);

            BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.1));
            BigDecimal total = subtotal.add(shipping).add(tax);

            // Tạo Order
            String orderCode = "ORD" + System.currentTimeMillis();

            // ⭐ Set status based on payment method
            String orderStatus = "COD".equalsIgnoreCase(paymentMethod) ? "COD_PENDING" : "PENDING";

            Order order = Order.builder()
                    .orderCode(orderCode)
                    .user(user)
                    .status(orderStatus)
                    .totalAmount(total)
                    .shippingAmount(shipping)
                    .address(address)
                    .phone(phone)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            order = orderRepository.save(order);

            // Tạo Order Items
            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = OrderItem.builder()
                        .order(order)
                        .product(cartItem.getProduct())
                        .productTitle(cartItem.getProduct().getTitle())
                        .selectedOptions(cartItem.getSelectedOptionsJson())
                        .unitPrice(cartItem.getUnitPrice())
                        .qty(cartItem.getQty())
                        .subtotal(cartItem.getLineTotal())
                        .build();

                orderItemRepository.save(orderItem);
            }

            // Xóa cart sau khi tạo order
            cartItemRepository.deleteAll(cartItems);

            // ⭐ BRANCH: COD or VNPay
            if ("COD".equalsIgnoreCase(paymentMethod)) {
                // COD: Redirect to success page immediately
                model.addAttribute("status", "success");
                model.addAttribute("orderCode", orderCode);
                model.addAttribute("totalPrice", total.multiply(BigDecimal.valueOf(100)).intValue());
                model.addAttribute("paymentMethod", "COD");
                return "payment-result";
            } else {
                // VNPay: Create payment URL and redirect
                String baseUrl = getBaseUrl(request);
                String vnpayUrl = vnPayService.createOrder(
                        total.multiply(BigDecimal.valueOf(100)).intValue(),
                        orderCode,
                        baseUrl
                );
                return "redirect:" + vnpayUrl;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/checkout?error=" + e.getMessage();
        }
    }

    /**
     * VNPay callback handler
     */
    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        int paymentStatus = vnPayService.orderReturn(request);

        String orderCode = request.getParameter("vnp_TxnRef");
        String transactionNo = request.getParameter("vnp_TransactionNo");
        String amountStr = request.getParameter("vnp_Amount"); // đã nhân 100

        BigDecimal amount = amountStr != null ? new BigDecimal(amountStr).divide(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("transactionNo", transactionNo);
        model.addAttribute("totalPrice", amount);

        if (paymentStatus == 1) {
            Order order = orderRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setStatus("PAID");
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);

            Payment payment = Payment.builder()
                    .order(order)
                    .vnpTxnref(orderCode)
                    .vnpTransDate(request.getParameter("vnp_PayDate"))
                    .vnpResponseCode(request.getParameter("vnp_ResponseCode"))
                    .vnpPaymentNo(transactionNo)
                    .amount(amount)
                    .method("VNPAY")
                    .status("SUCCESS")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            paymentRepository.save(payment);

            model.addAttribute("status", "success");
        } else {
            model.addAttribute("status", "failed");
        }
        return "payment-result";
    }

    private final PaymentRepository paymentRepository;

    /**
     * Helper: Get base URL from request
     */
    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        // Only add port if it's not default (80 for http, 443 for https)
        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }
}