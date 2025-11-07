package org.fsm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login", "/signup", "/register", "/logout",
                        "/forgot-password", "/reset-password",
                        "/oauth2/**", "/error",
                        "/css/**", "/js/**", "/images/**", "/webjars/**",
                        "/", "/shop", "/shop/**", "/about", "/about/**",
                        "/contact", "/contact/**", "/careers", "/careers/**",
                        "/faqs", "/faqs/**", "/cart", "/cart/**"
                );
    }
}