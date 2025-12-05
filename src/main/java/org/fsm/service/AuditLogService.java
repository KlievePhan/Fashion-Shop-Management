package org.fsm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsm.entity.AuditLog;
import org.fsm.entity.User;
import org.fsm.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    /**
     * Create audit log entry
     */
    @Transactional
    public AuditLog createAuditLog(
            User actor,
            String entity,
            String entityId,
            String action,
            Object changes,
            HttpServletRequest request
    ) {
        try {
            String changesJson = changes != null ? objectMapper.writeValueAsString(changes) : null;

            AuditLog auditLog = AuditLog.builder()
                    .actor(actor)
                    .actorRole(actor != null ? actor.getRole().getCode() : "SYSTEM")
                    .entity(entity)
                    .entityId(entityId)
                    .action(action)
                    .changes(changesJson)
                    .ipAddress(getClientIp(request))
                    .userAgent(request.getHeader("User-Agent"))
                    .createdAt(LocalDateTime.now())
                    .build();

            return auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to create audit log", e);
            return null;
        }
    }

    /**
     * Get all audit logs with pagination
     */
    public Page<AuditLog> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable);
    }

    /**
     * Get audit logs by action with pagination
     */
    public Page<AuditLog> getAuditLogsByAction(String action, Pageable pageable) {
        return auditLogRepository.findByAction(action, pageable);
    }

    /**
     * Get audit logs by action (List)
     */
    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    /**
     * Get audit logs by entity with pagination
     */
    public Page<AuditLog> getAuditLogsByEntity(String entity, Pageable pageable) {
        return auditLogRepository.findByEntity(entity, pageable);
    }

    /**
     * Get audit logs by entity (List)
     */
    public List<AuditLog> getAuditLogsByEntity(String entity) {
        return auditLogRepository.findByEntity(entity);
    }

    /**
     * Get audit logs by actor
     */
    public List<AuditLog> getAuditLogsByActor(User actor) {
        return auditLogRepository.findByActor(actor);
    }

    /**
     * Get audit logs by actor ID and date range
     */
    public List<AuditLog> getAuditLogsByActorAndDateRange(Long actorId, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByActorIdAndCreatedAtBetween(actorId, start, end);
    }

    /**
     * Get audit logs by date range
     */
    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return auditLogRepository.findByCreatedAtBetween(startDate, endDate);
    }

    /**
     * Get audit logs for specific entity instance
     */
    public List<AuditLog> getAuditLogsForEntity(String entity, String entityId) {
        return auditLogRepository.findByEntityAndEntityId(entity, entityId);
    }

    /**
     * Filter audit logs with multiple criteria
     */
    public List<AuditLog> filterAuditLogs(
            String action,
            String entity,
            LocalDate date,
            User actor
    ) {
        if (action != null && !action.equals("all")) {
            if (entity != null && !entity.equals("all")) {
                return auditLogRepository.findByActionAndEntity(action, entity);
            }
            return auditLogRepository.findByAction(action);
        }

        if (entity != null && !entity.equals("all")) {
            return auditLogRepository.findByEntity(entity);
        }

        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();
            return auditLogRepository.findByCreatedAtBetween(startOfDay, endOfDay);
        }

        if (actor != null) {
            return auditLogRepository.findByActor(actor);
        }

        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    /**
     * Create change map for tracking updates
     */
    public Map<String, Object> createChangeMap(Object oldValue, Object newValue) {
        Map<String, Object> changes = new HashMap<>();
        changes.put("old", oldValue);
        changes.put("new", newValue);
        changes.put("timestamp", LocalDateTime.now());
        return changes;
    }

    /**
     * Extract client IP address from request
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0].trim() : "unknown";
    }

    /**
     * Delete old audit logs (cleanup)
     */
    @Transactional
    public void deleteOldAuditLogs(LocalDateTime beforeDate) {
        auditLogRepository.deleteByCreatedAtBefore(beforeDate);
    }

    /**
     * Count by action
     */
    public long countByAction(String action) {
        return auditLogRepository.countByAction(action);
    }

    /**
     * Count by entity
     */
    public long countByEntity(String entity) {
        return auditLogRepository.countByEntity(entity);
    }

    /**
     * Delete single audit log by ID
     */
    @Transactional
    public void deleteAuditLog(Long id) {
        auditLogRepository.deleteById(id);
    }

    /**
     * Delete multiple audit logs by IDs
     */
    @Transactional
    public void deleteAuditLogs(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            auditLogRepository.deleteAllById(ids);
        }
    }
}