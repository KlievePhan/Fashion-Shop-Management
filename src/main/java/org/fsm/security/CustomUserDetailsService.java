package org.fsm.security;

import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Find user by email
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Check if user is active
        if (user.getActive() == null || !user.getActive()) {
            throw new org.springframework.security.authentication.DisabledException("User account is disabled");
        }

        // Access role.getCode() within transaction (eager fetch or lazy load within session)
        String roleCode = user.getRole().getCode();

        // Build UserDetails
        boolean enabled = Boolean.TRUE.equals(user.getActive());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword() != null ? user.getPassword() : "")
                .authorities(roleCode)
                .accountLocked(!enabled)
                .disabled(!enabled)
                .build();
    }
}
