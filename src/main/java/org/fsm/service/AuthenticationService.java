package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.entity.PasswordResetToken;
import org.fsm.repository.UserRepository;
import org.fsm.repository.PasswordResetTokenRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public User authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email"));

        if (!user.getActive()) {
            throw new RuntimeException("Account is disabled");
        }

        if (user.getPassword() == null) {
            throw new RuntimeException("This account uses social login. Please login with Google.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    public String createRememberMeToken(User user) {
        // Generate unique token
        String token = UUID.randomUUID().toString();

        // Store token hash in user record (add rememberMeToken field to User entity)
        user.setRememberMeToken(passwordEncoder.encode(token));
        user.setRememberMeExpiry(LocalDateTime.now().plusDays(30));
        userRepository.save(user);

        return user.getId() + ":" + token;
    }

    public Optional<User> validateRememberMeToken(String cookieValue) {
        try {
            String[] parts = cookieValue.split(":");
            if (parts.length != 2) return Optional.empty();

            Long userId = Long.parseLong(parts[0]);
            String token = parts[1];

            return userRepository.findById(userId)
                    .filter(user -> user.getRememberMeToken() != null)
                    .filter(user -> user.getRememberMeExpiry() != null)
                    .filter(user -> user.getRememberMeExpiry().isAfter(LocalDateTime.now()))
                    .filter(user -> passwordEncoder.matches(token, user.getRememberMeToken()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));

        // Generate reset token
        String token = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        tokenRepository.save(resetToken);

        // Send email
        String resetLink = "http://localhost:8080/fashionshop/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = String.format(
                "Hello %s,\n\n" +
                        "You requested to reset your password. Click the link below to reset:\n\n" +
                        "%s\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't request this, please ignore this email.\n\n" +
                        "Best regards,\n" +
                        "Fashion Shop Team",
                user.getFullName(),
                resetLink
        );

        emailService.sendContactEmail(email, subject, body);
    }

    public boolean validateResetToken(String token) {
        try {
            Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

            if (resetToken.isEmpty()) {
                System.out.println("Token not found: " + token);
                return false;
            }

            PasswordResetToken t = resetToken.get();
            System.out.println("Token found. Used: " + t.getUsed() + ", Expiry: " + t.getExpiryDate());

            if (t.getUsed()) {
                System.out.println("Token already used");
                return false;
            }

            if (t.getExpiryDate().isBefore(LocalDateTime.now())) {
                System.out.println("Token expired");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.out.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .filter(t -> !t.getUsed())
                .filter(t -> t.getExpiryDate().isAfter(LocalDateTime.now()))
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        System.out.println("Password reset successful for user: " + user.getEmail());
    }
}