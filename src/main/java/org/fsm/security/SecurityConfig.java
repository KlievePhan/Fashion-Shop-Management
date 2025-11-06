package org.fsm.security;

import org.fsm.service.CustomOAuth2UserService;
import org.fsm.service.OAuth2AuthenticationSuccessHandler;
import org.fsm.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomOAuth2UserService oauth2UserService;
        private final OAuth2AuthenticationSuccessHandler successHandler;
        private final UserRepository userRepository;

        public SecurityConfig(CustomOAuth2UserService oauth2UserService,
                              OAuth2AuthenticationSuccessHandler successHandler,
                              UserRepository userRepository) {
                this.oauth2UserService = oauth2UserService;
                this.successHandler = successHandler;
                this.userRepository = userRepository;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                        .csrf(csrf -> csrf.disable())
                        .authorizeHttpRequests(authz -> authz
                                .requestMatchers("/", "/login", "/signup", "/register", "/error",
                                        "/oauth2/**", "/shop/**", "/about/**", "/contact/**",
                                        "/careers/**", "/faqs/**", "/css/**", "/js/**", "/images/**")
                                .permitAll()
                                .anyRequest().authenticated()
                        )
                        .formLogin(form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .usernameParameter("email")
                                .passwordParameter("password")
                                .defaultSuccessUrl("/", true)
                                .failureUrl("/login?error=true")
                                .permitAll()
                        )
                        .oauth2Login(oauth2 -> oauth2
                                .loginPage("/login")
                                .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                                .successHandler(successHandler)
                        )
                        .logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout=true")
                                .permitAll()
                        );

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public UserDetailsService userDetailsService() {
                return email -> {
                        var user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

                        if (!user.getActive()) {
                                throw new org.springframework.security.authentication.DisabledException("User is disabled");
                        }

                        return org.springframework.security.core.userdetails.User
                                .withUsername(user.getEmail())
                                .password(user.getPassword())
                                .authorities(user.getRole().getCode())
                                .build();
                };
        }
}