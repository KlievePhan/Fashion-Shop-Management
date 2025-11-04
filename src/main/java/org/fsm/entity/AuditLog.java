package org.fsm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private User actor;

    @Column(name = "actor_role", length = 100)
    private String actorRole;

    @Column(nullable = false, length = 100)
    private String entity;          // table name

    @Column(name = "entity_id", length = 255)
    private String entityId;

    @Column(nullable = false, length = 50)
    private String action;          // CREATE, UPDATE, DELETE, VIEW

    @Column(columnDefinition = "JSON")
    private String changes;         // diff / payload

    @Column(name = "ip_address", length = 100)
    private String ipAddress;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}

