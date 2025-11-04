package org.fsm.repository;

import org.fsm.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
