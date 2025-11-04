package org.fsm.repository;

import org.fsm.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    List<ProductImage> findByProductIdOrderByOrdersAsc(Long productId);
    List<ProductImage> findByProductId(Long productId);
}
