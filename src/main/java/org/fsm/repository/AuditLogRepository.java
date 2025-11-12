package org.fsm.repository;

import org.fsm.entity.AuditLog;
import org.fsm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Pageable queries
    Page<AuditLog> findByEntity(String entity, Pageable pageable);
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Find by actor ID and date range
    List<AuditLog> findByActorIdAndCreatedAtBetween(Long actorId, LocalDateTime start, LocalDateTime end);

    // Find by action (List)
    List<AuditLog> findByAction(String action);

    // Find by entity type (List)
    List<AuditLog> findByEntity(String entity);

    // Find by actor
    List<AuditLog> findByActor(User actor);

    // Find by entity and entity ID
    List<AuditLog> findByEntityAndEntityId(String entity, String entityId);

    // Find by action and entity
    List<AuditLog> findByActionAndEntity(String action, String entity);

    // Find by date range
    List<AuditLog> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Find recent logs
    List<AuditLog> findTop100ByOrderByCreatedAtDesc();

    // Find by actor and date range
    List<AuditLog> findByActorAndCreatedAtBetween(User actor, LocalDateTime startDate, LocalDateTime endDate);

    // Delete old logs
    void deleteByCreatedAtBefore(LocalDateTime beforeDate);

    // Count by action
    long countByAction(String action);

    // Count by entity
    long countByEntity(String entity);
}