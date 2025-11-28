package org.fsm.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "wish_list",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"user_id", "product_id", "product_variant_id"},
                name = "uniq_user_product_variant"
        ),
        indexes = {
                @Index(name = "idx_wishlist_user", columnList = "user_id"),
                @Index(name = "idx_wishlist_product", columnList = "product_id")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WishList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id")
    private ProductVariant productVariant;  // nullable → user likes the product in general

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(name = "selected_options_json", columnDefinition = "TEXT")
    private String selectedOptionsJson;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }
}