package org.fsm.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductDTO {
    private Long id;
    private String sku;
    private String title;
    private String description;
    private BigDecimal basePrice;
    private Boolean active;

    // Nested simple objects instead of full entities
    private CategoryInfo category;
    private BrandInfo brand;

    // Image URLs
    private List<ImageInfo> images;

    // Simplified nested classes
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class CategoryInfo {
        private Integer id;
        private String name;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class BrandInfo {
        private Integer id;
        private String name;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ImageInfo {
        private Long id;
        private String url;
        private Boolean primary;
        private Integer orders;
    }

    // Convenience field for primary image
    private String primaryImageUrl;
}