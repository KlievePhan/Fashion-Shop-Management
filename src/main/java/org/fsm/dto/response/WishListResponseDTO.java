package org.fsm.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WishListResponseDTO {
    private Long wishlistId;
    private Long productId;
    private String productTitle;
    private String productSlug;
    private String brandName;
    private String primaryImageUrl;

    private Long variantId;
    private String variantSku;
    private Object attributes;        // JSON object {size: "M", color: "Red", ...}
    private BigDecimal price;
    private Integer stock;

    private LocalDateTime addedAt;
}