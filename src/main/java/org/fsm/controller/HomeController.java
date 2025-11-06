package org.fsm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.fsm.entity.ContactMessage;
import org.fsm.entity.Role;
import org.fsm.entity.User;
import org.fsm.dto.request.RegisterRequest;
import org.fsm.repository.ContactMessageRepository;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.UserRepository;
import org.fsm.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContactMessageRepository contactMessageRepository;
    private final EmailService emailService;


    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("currentPath", "/");
        return "home";
    }

     @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("currentPath", "/shop");
        return "shop";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("currentPath", "/about");
        return "about";
    }

    @GetMapping("/careers")
    public String careers() { return "careers"; }

    @GetMapping("/faqs")
    public String faqs() { return "faqs"; }

//    @GetMapping("/contact")
//    public String contact() { return "contact"; }

    @GetMapping("/cart")
    public String cart() { return "cart"; }
    
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
            Model model
    ) {
        if (result.hasErrors()) {
            return "contact";
        }

        // Lưu vào DB
        contactMessageRepository.save(contactMessage);

        // Gửi email đến admin
        String adminEmail = "qdx2005@gmail.com";
        String subject = "New Contact Message from " + contactMessage.getFullName();
        String body = String.format(
                "Name: %s%nEmail: %s%nSubject: %s%n%nMessage:%n%s",
                contactMessage.getFullName(),
                contactMessage.getEmail(),
                contactMessage.getSubject(),
                contactMessage.getMessage()
        );

        emailService.sendContactEmail(adminEmail, subject, body);

        model.addAttribute("successMessage", "Your message has been sent successfully!");
        model.addAttribute("contactMessage", new ContactMessage()); // reset form
        return "contact";
    }
    // =================================================
    // === AUTH PAGES ===
    @GetMapping("/login")
    public String login(Model model, @RequestParam(required = false) String error) {
        model.addAttribute("currentPath", "/login");
        if (error != null) {
            model.addAttribute("loginError", "Invalid email or password");
        }
        return "login_signup"; // same template
    }

    @GetMapping("/signup")
    public String signup(Model model, RegisterRequest request) {
        model.addAttribute("currentPath", "/signup");
        model.addAttribute("registerRequest", request); // pre-fill form
        return "login_signup";
    }

    // === MANUAL REGISTRATION ===
    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute("registerRequest") RegisterRequest request,
                             BindingResult result,
                             Model model) {

        model.addAttribute("currentPath", "/signup");
        if (result.hasErrors()) {
            model.addAttribute("hasValidationErrors", true); // ← ADD THIS
            return "login_signup";
        }
        if (result.hasErrors()) {
            return "login_signup"; // same page
        }
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            model.addAttribute("registerError", "Email already exists");
            return "login_signup";
        }

        Role userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

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