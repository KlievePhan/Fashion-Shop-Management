package org.fsm.repository;

import org.fsm.entity.Product;
import org.fsm.entity.ProductOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductOptionRepository extends JpaRepository<ProductOption, Long> {

    /**
     * Lấy tất cả options của một product, sắp xếp theo type và sort_order
     */
    List<ProductOption> findByProductOrderByOptionTypeAscSortOrderAsc(Product product);

    /**
     * Lấy options theo type (SIZE hoặc COLOR)
     */
    List<ProductOption> findByProductAndOptionTypeOrderBySortOrderAsc(Product product, String optionType);

    /**
     * Tìm option cụ thể theo product, type và value
     */
    Optional<ProductOption> findByProductAndOptionTypeAndOptionValue(
            Product product, String optionType, String optionValue);

    /**
     * Lấy tất cả active options của product
     */
    @Query("SELECT po FROM ProductOption po WHERE po.product.id = :productId " +
            "AND po.active = true ORDER BY po.optionType, po.sortOrder")
    List<ProductOption> findActiveOptionsByProductId(@Param("productId") Long productId);

    /**
     * Lấy options theo type và active
     */
    @Query("SELECT po FROM ProductOption po WHERE po.product.id = :productId " +
            "AND po.optionType = :optionType AND po.active = true ORDER BY po.sortOrder")
    List<ProductOption> findActiveOptionsByProductIdAndType(
            @Param("productId") Long productId,
            @Param("optionType") String optionType);
}