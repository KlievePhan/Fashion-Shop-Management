package org.fsm.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    // ⭐ THAY ĐỔI: Từ ProductVariant → Product
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ⭐ MỚI: Lưu selected options dạng JSON
    @Column(name = "selected_options_json", columnDefinition = "JSON")
    private String selectedOptionsJson;

    // ⭐ MỚI: Giá tính toán (base_price + adjustments)
    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 1")
    private Integer qty = 1;

    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ========== HELPER METHODS ==========

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse JSON string thành Map
     */
    @Transient
    public Map<String, String> getSelectedOptions() {
        if (selectedOptionsJson == null || selectedOptionsJson.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(selectedOptionsJson, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    /**
     * Convert Map thành JSON string
     */
    public void setSelectedOptions(Map<String, String> options) {
        try {
            this.selectedOptionsJson = objectMapper.writeValueAsString(options);
        } catch (JsonProcessingException e) {
            this.selectedOptionsJson = "{}";
        }
    }

    /**
     * Helper: Lấy size đã chọn
     */
    @Transient
    public String getSelectedSize() {
        return getSelectedOptions().get("size");
    }

    /**
     */
    @Transient
    public String getSelectedColor() {
        return getSelectedOptions().get("color");
    }

    /**/
    @Transient
    public BigDecimal getLineTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(BigDecimal.valueOf(qty));
    }
}