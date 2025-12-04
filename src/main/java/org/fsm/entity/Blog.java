package org.fsm.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "blogs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String category;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "read_time")
    private Integer readTime; // in minutes

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BlogStatus status = BlogStatus.DRAFT;

    @Column(length = 200)
    private String author;

    @Column(name = "created_by")
    private Long createdBy; // Staff ID who created

    @Column(name = "updated_by")
    private Long updatedBy; // Staff ID who last updated

    @Column(length = 500)
    private String tags; // Comma-separated tags

    @Column(name = "meta_description", length = 160)
    private String metaDescription; // SEO description

    @Column(name = "slug", unique = true, length = 200)
    private String slug; // URL-friendly version of title

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == BlogStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == BlogStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    // Enum for Blog Status
    public enum BlogStatus {
        DRAFT("Draft - Not visible to customers"),
        PENDING_REVIEW("Pending Review - Waiting for approval"),
        PUBLISHED("Published - Live on website"),
        SCHEDULED("Scheduled - Will be published later"),
        ARCHIVED("Archived - No longer visible");

        private final String description;

        BlogStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}