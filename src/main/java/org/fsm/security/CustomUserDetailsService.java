package org.fsm.security;

import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

// src/main/java/org/fsm/security/CustomUserDetailsService.java
@Service
@RequiredArgsConstructor   // <-- add Lombok if you use it, or keep constructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new org.springframework.security.authentication.DisabledException("User account is disabled");
        }

        String roleCode = user.getRole().getCode();

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(roleCode)
                .accountLocked(!user.getActive())
                .disabled(!user.getActive())
                .build();
    }

    // Helper for @ControllerAdvice
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}