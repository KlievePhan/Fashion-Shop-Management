package org.fsm.repository;

import org.fsm.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {

    // Find brand by slug
    Optional<Brand> findBySlug(String slug);

    // Check if brand name exists
    boolean existsByName(String name);

    // Check if brand slug exists
    boolean existsBySlug(String slug);

    // Search brands by name (case-insensitive)
    List<Brand> findByNameContainingIgnoreCase(String keyword);
}