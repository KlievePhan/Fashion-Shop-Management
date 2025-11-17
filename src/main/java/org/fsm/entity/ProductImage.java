package org.fsm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonBackReference("product-variants")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 1000)
    private String url;

    @Column(name = "orders", columnDefinition = "INT DEFAULT 0")
    private Integer orders = 0;

    @Column(name = "is_primary", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean primary = false;
}
