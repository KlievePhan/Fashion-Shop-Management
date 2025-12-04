package org.fsm.security;

import lombok.RequiredArgsConstructor;
import org.fsm.service.CustomOAuth2UserService;
import org.fsm.service.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.fsm.service.CustomOidcUserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler oauth2SuccessHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/staff/**").hasAuthority("ROLE_STAFF")
                        .requestMatchers("/profile/**", "/cart/**", "/wishlist/**", "/checkout/**").authenticated()
                        .requestMatchers(
                                "/", "/shop", "/shop/**", "/product/**",
                                "/css/**", "/js/**", "/images/**", "/fonts/**",
                                "/login", "/signup", "/register",
                                "/forgot-password", "/reset-password"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")           // Important: POST here
                        .usernameParameter("email")             // Because you use email, not username
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            String role = authentication.getAuthorities().stream()
                                    .findFirst().get().getAuthority();

                            if ("ROLE_ADMIN".equals(role)) {
                                response.sendRedirect("/fashionshop/admin");
                            } else if ("ROLE_STAFF".equals(role)) {
                                response.sendRedirect("/fashionshop/staff");
                            } else {
                                response.sendRedirect("/fashionshop");
                            }
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService())
                        )
                        .successHandler(oauth2SuccessHandler)
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("remember-me", "JSESSIONID")
                        .permitAll()
                )
                .rememberMe(remember -> remember
                        .rememberMeParameter("rememberMe")  // matches your checkbox name
                        .rememberMeCookieName("remember-me")
                        .tokenValiditySeconds(30 * 24 * 60 * 60) // 30 days
                        .userDetailsService(userDetailsService)
                )
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .expiredUrl("/login?expired=true")
                );

        return http.build();
    }

    @Bean
    public CustomOidcUserService customOidcUserService() {
        return new CustomOidcUserService(customOAuth2UserService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}