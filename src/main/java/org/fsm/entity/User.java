package org.fsm.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "google_sub"),
        @UniqueConstraint(columnNames = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "google_sub", length = 255, unique = true)
    private String googleSub;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email format is invalid")
    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Size(max = 255, message = "Display name too long")
    @Column(name = "display_name", length = 255)
    private String displayName;

    @Size(max = 255, message = "Full name too long")
    @Column(name = "full_name", length = 255)
    private String fullName;

    @Size(min = 6, message = "Password must be at least 6 characters")
    @Column(name = "password", length = 255)
    private String password;

    @Pattern(regexp = "^[0-9]{10,15}$", message = "Phone number must be 10-15 digits")
    @Column(length = 50)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String defaultAddress;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean active = true;

    @Column(name = "profile_completed", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean profileCompleted = false;

    // Remember Me fields
    @Column(name = "remember_me_token", length = 255)
    private String rememberMeToken;

    @Column(name = "remember_me_expiry")
    private LocalDateTime rememberMeExpiry;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}