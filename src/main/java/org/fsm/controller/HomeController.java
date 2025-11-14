package org.fsm.controller;

import org.springframework.security.core.Authentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fsm.dto.request.RegisterRequest;
import org.fsm.entity.ContactMessage;
import org.fsm.entity.Product;
import org.fsm.entity.Role;
import org.fsm.entity.User;
import org.fsm.repository.ContactMessageRepository;
import org.fsm.repository.ProductRepository;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.UserRepository;
import org.fsm.service.CartService;
import org.fsm.service.EmailService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Controller
public class HomeController {
    private final CartService cartService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContactMessageRepository contactMessageRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("currentPath", "/");
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "home";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        model.addAttribute("currentPath", "/product/" + id);
        model.addAttribute("product", product);
        List<Product> relatedProducts = productRepository.findAll()
                .stream()
                .filter(p -> !p.getId().equals(id))
                .limit(4)
                .toList();
        model.addAttribute("relatedProducts", relatedProducts);
        return "product-detail";
    }

    // @GetMapping("/admin")
    // public String admin() {
    // return "admin";
    // }

    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("currentPath", "/shop");
        List<Product> products = productRepository.findAll();
        model.addAttribute("products", products);
        return "shop";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPath", "/about");
        return "about";
    }

    @GetMapping("/careers")
    public String careers() {
        return "careers";
    }

    @GetMapping("/faqs")
    public String faqs() {
        return "faqs";
    }

    @GetMapping("/cart")
    public String cart() {
        return "cart";
    }

    // ================== CONTACT =====================
    @GetMapping("/contact")
    public String contact(Model model) {
        model.addAttribute("contactMessage", new ContactMessage());
        return "contact";
    }

    @PostMapping("/contact")
    public String handleContact(
            @Valid @ModelAttribute("contactMessage") ContactMessage contactMessage,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "contact";
        }

        // Save to DB
        contactMessageRepository.save(contactMessage);

        // Send email to admin
        String adminEmail = "tuanmagero@gmail.com";
        String subject = "New Contact Message from " + contactMessage.getFullName();
        String body = String.format(
                "Name: %s%nEmail: %s%nSubject: %s%n%nMessage:%n%s",
                contactMessage.getFullName(),
                contactMessage.getEmail(),
                contactMessage.getSubject(),
                contactMessage.getMessage());

        emailService.sendContactEmail(adminEmail, subject, body);

        model.addAttribute("successMessage", "Your message has been sent successfully!");
        model.addAttribute("contactMessage", new ContactMessage());
        return "contact";
    }

    // =================================================
    // === REGISTRATION (Keep this for backward compatibility) ===
    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("currentPath", "/signup");
        return "redirect:/login"; // Redirect to AuthController
    }

    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model) {

        model.addAttribute("currentPath", "/signup");

        if (result.hasErrors()) {
            model.addAttribute("hasValidationErrors", true);
            return "login_signup";
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            model.addAttribute("registerError", "Email already exists");
            return "login_signup";
        }

        Role userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found in database! Run migration!"));

        User user = User.builder()
                .fullName(request.getFullName())
                .displayName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .active(true)
                .profileCompleted(false)
                .build();
        userRepository.save(user);
        return "redirect:/login?success=registered";
    }

}