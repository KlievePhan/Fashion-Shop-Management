package org.fsm.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.User;
import org.fsm.service.AuthenticationService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthenticationService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        // If user is already logged in via session, continue
        if (session != null && session.getAttribute("userId") != null) {
            return true;
        }

        // Check for remember-me cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Optional<Cookie> rememberMeCookie = Arrays.stream(cookies)
                    .filter(c -> "remember-me".equals(c.getName()))
                    .findFirst();

            if (rememberMeCookie.isPresent()) {
                String cookieValue = rememberMeCookie.get().getValue();
                Optional<User> user = authService.validateRememberMeToken(cookieValue);

                if (user.isPresent()) {
                    // Auto-login: create new session with user data (not entity)
                    User u = user.get();
                    HttpSession newSession = request.getSession(true);
                    newSession.setAttribute("userId", u.getId());
                    newSession.setAttribute("userEmail", u.getEmail());
                    newSession.setAttribute("userFullName", u.getFullName());
                    newSession.setAttribute("userRole", u.getRole().getCode());
                    newSession.setAttribute("isAuthenticated", true);
                }
            }
        }

        return true;
    }
}