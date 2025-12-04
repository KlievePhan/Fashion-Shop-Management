package org.fsm.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "color_variant", length = 50)
    private String colorVariant; // ⭐ ĐẢM BẢO CÓ FIELD NÀY

    @Column(name = "orders")
    private Integer orders;

    @Column(name = "is_primary")
    private Boolean primary = false;

    public String getColorVariant() {
        return colorVariant;
    }

    public void setColorVariant(String colorVariant) {
        this.colorVariant = colorVariant;
    }
}