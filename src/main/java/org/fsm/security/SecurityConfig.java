package org.fsm.security;

import org.fsm.service.CustomOAuth2UserService;
import org.fsm.service.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        private final CustomOAuth2UserService oauth2UserService;
        private final OAuth2AuthenticationSuccessHandler successHandler;

        public SecurityConfig(CustomOAuth2UserService oauth2UserService,
                              OAuth2AuthenticationSuccessHandler successHandler) {
                this.oauth2UserService = oauth2UserService;
                this.successHandler = successHandler;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                http
                        .csrf(csrf -> csrf.disable()) // Only if you don't need CSRF (or enable it)
                        .authorizeHttpRequests(authz -> authz
                                .requestMatchers("/", "/login", "/signup", "/register", "/error",
                                        "/oauth2/**", "/shop/**", "/about/**", "/contact/**",
                                        "/careers/**", "/faqs/**", "/products/**",
                                        "/css/**", "/js/**", "/images/**")
                                .permitAll()
                                .anyRequest().authenticated()
                        )
                        // OAUTH2
                        .oauth2Login(oauth2 -> oauth2
                                .loginPage("/login")
                                .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                                .successHandler(successHandler)
                        )
                        // FORM LOGIN
                        .formLogin(form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")  // Must match form action
                                .usernameParameter("email")    // Match input name
                                .passwordParameter("password")
                                .defaultSuccessUrl("/", true)
                                .failureUrl("/login?error=true")
                                .permitAll()
                        )
                        // LOGOUT
                        .logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/login?logout=true")
                                .invalidateHttpSession(true)
                                .deleteCookies("JSESSIONID")
                                .permitAll()
                        );

                return http.build();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}