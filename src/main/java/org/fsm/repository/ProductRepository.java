package org.fsm.repository;

import org.fsm.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);

    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);
    Page<Product> findByBrandId(Integer brandId, Pageable pageable);
    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    // New: Search by both SKU and Title
    @Query("SELECT p FROM Product p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchBySkuOrTitle(@Param("keyword") String keyword, Pageable pageable);

    List<Product> findTop10ByOrderByCreatedAtDesc();

}
