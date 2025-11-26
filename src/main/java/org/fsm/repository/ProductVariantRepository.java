package org.fsm.repository;

import org.fsm.entity.Product;
import org.fsm.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    Optional<ProductVariant> findBySku(String sku);
    List<ProductVariant> findByProductId(Long productId);
    boolean existsBySku(String sku);
    Optional<ProductVariant> findByProductAndAttributeJson(Product product, String attributeJson);

    Optional<ProductVariant> findByProductAndAttributeJsonContaining(Product product, String attributeJson);
}
