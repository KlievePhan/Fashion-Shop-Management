package org.fsm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.Role;
import org.fsm.entity.User;
import org.fsm.dto.request.RegisterRequest;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("currentPath", "/"); // or use request
        return "home";
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
    @GetMapping("/careers") public String careers() { return "careers"; }
    @GetMapping("/faqs")    public String faqs()    { return "faqs"; }
    @GetMapping("/contact") public String contact() { return "contact"; }
    @GetMapping("/cart")    public String cart()    { return "cart"; }

    // === AUTH PAGES ===
    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("currentPath", "/login");
        return "login_signup";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("currentPath", "/signup");
        return "login_signup";
    }

    // === MANUAL REGISTRATION ===
    @PostMapping("/register")
    public String doRegister(@Valid @ModelAttribute RegisterRequest request,
                             BindingResult result,
                             Model model) {

        if (result.hasErrors()) {
            // Send validation errors back to the form
            model.addAttribute("errors", result.getAllErrors());
            return "signup"; // show same page with error messages
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            model.addAttribute("errorMessage", "Email already exists");
            return "signup";
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