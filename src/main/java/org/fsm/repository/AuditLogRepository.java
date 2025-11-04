package org.fsm.repository;

import org.fsm.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntity(String entity, Pageable pageable);
    List<AuditLog> findByActorIdAndCreatedAtBetween(Long actorId, LocalDateTime start, LocalDateTime end);
    Page<AuditLog> findByAction(String action, Pageable pageable);
}
