package org.fsm.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.fsm.dto.request.LoginRequest;
import org.fsm.dto.request.ForgotPasswordRequest;
import org.fsm.dto.request.ResetPasswordRequest;
import org.fsm.entity.User;
import org.fsm.service.AuthenticationService;
import org.fsm.service.SessionService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;
    private final SessionService sessionService;

    @GetMapping("/login")
    public String loginPage(Model model,
                            @RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String success,
                            @RequestParam(required = false) String resetSuccess) {
        model.addAttribute("currentPath", "/login");
        model.addAttribute("loginRequest", new LoginRequest());

        if (error != null) {
            model.addAttribute("loginError", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("logoutSuccess", "Logged out successfully");
        }
        if (resetSuccess != null) {
            model.addAttribute("resetSuccess", "Password reset successfully. Please login.");
        }
        if ("registered".equals(success)) {
            model.addAttribute("registerSuccess", "Registration successful! Please login.");
        }

        return "login_signup";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginRequest") LoginRequest request,
            BindingResult result,
            @RequestParam(defaultValue = "false") boolean rememberMe,
            Model model,
            HttpServletResponse response) {

        if (result.hasErrors()) {
            return "login_signup";
        }

        try {
            // Let Spring Security handle authentication via formLogin

            return "login_signup";

        } catch (Exception e) {
            model.addAttribute("loginError", "Invalid email or password");
            return "login_signup";
        }
    }

    /**
     * Authenticate user with Spring Security so it recognizes the session
     */
    private void authenticateWithSpringSecurity(User user, HttpSession session) {
        // Create Spring Security authentication token
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getCode());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        user.getEmail(),
                        null, // credentials (password) - not needed after authentication
                        Collections.singletonList(authority)
                );

        // Set in SecurityContext
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Store SecurityContext in session so Spring Security remembers it
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext
        );
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute ForgotPasswordRequest request,
                                 BindingResult result,
                                 Model model) {
        if (result.hasErrors()) {
            return "forgot_password";
        }

        try {
            authService.sendPasswordResetEmail(request.getEmail());
            model.addAttribute("successMessage", "Password reset link sent to your email!");
        } catch (Exception e) {
            System.out.println("Forgot password error: " + e.getMessage());
            model.addAttribute("errorMessage", e.getMessage());
        }

        return "forgot_password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        System.out.println("GET /reset-password with token: " + token);

        if (!authService.validateResetToken(token)) {
            model.addAttribute("errorMessage", "Invalid or expired reset token. Please request a new password reset.");
            return "error";
        }

        model.addAttribute("token", token);
        model.addAttribute("resetPasswordRequest", new ResetPasswordRequest());
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute ResetPasswordRequest request,
                                BindingResult result,
                                @RequestParam String token,
                                Model model) {

        System.out.println("POST /reset-password with token: " + token);

        if (result.hasErrors()) {
            model.addAttribute("token", token);
            return "reset_password";
        }

        // Validate passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match");
            model.addAttribute("token", token);
            return "reset_password";
        }

        try {
            authService.resetPassword(token, request.getPassword());
            return "redirect:/login?resetSuccess=true";
        } catch (Exception e) {
            System.out.println("Error resetting password: " + e.getMessage());
            model.addAttribute("errorMessage", "Failed to reset password: " + e.getMessage());
            model.addAttribute("token", token);
            return "reset_password";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletResponse response) {
        System.out.println("=== LOGOUT ===");

        // Clear Spring Security context
        SecurityContextHolder.clearContext();

        // Invalidate session
        if (session != null) {
            session.invalidate();
        }

        // Clear remember-me cookie
        Cookie cookie = new Cookie("remember-me", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "redirect:/login?logout=true";
    }
}