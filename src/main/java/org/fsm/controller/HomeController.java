package org.fsm.controller;

// HomeController.java
import lombok.RequiredArgsConstructor;
import org.fsm.repository.UserRepository;
import org.fsm.repository.RoleRepository;
import org.fsm.entity.Role;
import org.fsm.entity.User;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import java.util.Optional;

@RequiredArgsConstructor
@Controller
public class HomeController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder; // inject BCryptPasswordEncoder bean

    @GetMapping("/")
    public String home() { return "home"; }

    @GetMapping("/login") public String login() { return "login"; }

    @GetMapping("/register") public String register() { return "register"; }

    @PostMapping("/register")
    public String doRegister(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String password,
                             Model model) {

        if (userRepository.findByEmail(email).isPresent()) {
            // redirect back with error param
            return "redirect:/register?error=exists";
        }

        Role userRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

        User user = User.builder()
                .fullName(fullName)
                .displayName(fullName)
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(userRole)
                .active(true)
                .profileCompleted(false)
                .build();

        userRepository.save(user);

        // redirect to login page with success flag (works with your existing template)
        return "redirect:/login?success=registered";
    }
}
