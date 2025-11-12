package org.fsm.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.fsm.annotation.Audited;
import org.fsm.entity.User;
import org.fsm.repository.UserRepository;
import org.fsm.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    /**
     * Audit after successful method execution
     */
    @AfterReturning(value = "@annotation(audited)", returning = "result")
    public void auditAfterReturning(JoinPoint joinPoint, Audited audited, Object result) {
        try {
            User actor = getCurrentUser();
            HttpServletRequest request = getCurrentRequest();

            if (request == null) {
                log.warn("No HTTP request found in context");
                return;
            }

            String entityId = extractEntityId(result);

            auditLogService.createAuditLog(
                    actor,
                    audited.entity(),
                    entityId,
                    audited.action(),
                    result,
                    request
            );

            log.debug("Audit log created: {} {} on {} [{}]",
                    actor != null ? actor.getEmail() : "System",
                    audited.action(),
                    audited.entity(),
                    entityId);

        } catch (Exception e) {
            log.error("Failed to create audit log", e);
        }
    }

    /**
     * Audit after exception
     */
    @AfterThrowing(value = "@annotation(audited)", throwing = "exception")
    public void auditAfterThrowing(JoinPoint joinPoint, Audited audited, Exception exception) {
        try {
            User actor = getCurrentUser();
            HttpServletRequest request = getCurrentRequest();

            if (request == null) {
                return;
            }

            auditLogService.createAuditLog(
                    actor,
                    audited.entity(),
                    "ERROR",
                    audited.action() + "_FAILED",
                    exception.getMessage(),
                    request
            );

        } catch (Exception e) {
            log.error("Failed to create error audit log", e);
        }
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("No authenticated user found");
                return null;
            }

            Object principal = authentication.getPrincipal();

            // Case 1: Principal is already a User entity
            if (principal instanceof User) {
                return (User) principal;
            }

            // Case 2: Principal is UserDetails (Spring Security's default)
            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email).orElse(null);
            }

            // Case 3: Principal is a String (username/email)
            if (principal instanceof String) {
                String email = (String) principal;
                return userRepository.findByEmail(email).orElse(null);
            }

            log.warn("Unknown principal type: {}", principal.getClass().getName());
            return null;

        } catch (Exception e) {
            log.error("Error getting current user", e);
            return null;
        }
    }

    /**
     * Get current HTTP request
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.warn("Could not get current request", e);
            return null;
        }
    }

    /**
     * Extract entity ID from result object
     */
    private String extractEntityId(Object result) {
        if (result == null) {
            return null;
        }

        try {
            // Try to get ID using reflection
            var idField = result.getClass().getDeclaredField("id");
            idField.setAccessible(true);
            Object id = idField.get(result);
            return id != null ? id.toString() : null;
        } catch (NoSuchFieldException e) {
            log.debug("No 'id' field found in {}", result.getClass().getSimpleName());
            return null;
        } catch (Exception e) {
            log.warn("Could not extract entity ID", e);
            return result.toString();
        }
    }
}