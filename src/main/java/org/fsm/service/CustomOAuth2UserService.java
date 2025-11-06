package org.fsm.service;

import org.fsm.entity.Role;
import org.fsm.entity.User;
import org.fsm.exception.ResourceNotFoundException;
import org.fsm.repository.RoleRepository;
import org.fsm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;

        @Override
        public OAuth2User loadUser(OAuth2UserRequest userRequest) {
                OAuth2User oauth2User = super.loadUser(userRequest);

                String googleSub = oauth2User.getAttribute("sub");
                String email = oauth2User.getAttribute("email");
                String name = oauth2User.getAttribute("name");
                String picture = oauth2User.getAttribute("picture");

                User user = userRepository.findByGoogleSub(googleSub)
                                .or(() -> userRepository.findByEmail(email))
                                .orElseGet(() -> registerNewUser(googleSub, email, name, picture));

                // CRITICAL: Use "sub" or "email" as principal name, but include email in
                // attributes
                Map<String, Object> attributes = new HashMap<>(oauth2User.getAttributes());
                attributes.put("email", email); // Ensure email is always in attributes

                return new DefaultOAuth2User(
                                List.of(new SimpleGrantedAuthority(user.getRole().getCode())),
                                attributes,
                                "email" // This tells Spring to use 'email' as principal name
                );
        }

        private User registerNewUser(String googleSub, String email, String name, String picture) {
                Role userRole = roleRepository.findByCode("ROLE_USER")
                                .orElseThrow(() -> new ResourceNotFoundException("ROLE_USER not found"));

                User newUser = User.builder()
                                .googleSub(googleSub)
                                .email(email)
                                .displayName(name)
                                .fullName(name)
                                .avatarUrl(picture)
                                .role(userRole)
                                .active(true)
                                .profileCompleted(false)
                                .build();

                return userRepository.save(newUser);
        }
}