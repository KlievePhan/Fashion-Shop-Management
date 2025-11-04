package org.fsm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "google_sub"),
        @UniqueConstraint(columnNames = "email")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_sub", length = 255, unique = true)
    private String googleSub;

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "display_name", length = 255)
    private String displayName;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "password", length = 255)
    private String password;

    @Column(length = 50)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String defaultAddress;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    @Column(name = "profile_completed", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean profileCompleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
