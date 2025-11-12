package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.fsm.annotation.Audited;
import org.fsm.entity.Brand;
import org.fsm.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    // Get all brands
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    // Get brand by ID
    public Optional<Brand> getBrandById(Integer id) {
        return brandRepository.findById(id);
    }

    // Get brand by slug
    public Optional<Brand> getBrandBySlug(String slug) {
        return brandRepository.findBySlug(slug);
    }

    // Create new brand
    @Transactional
    @Audited(entity = "Brand", action = "CREATE")
    public Brand createBrand(Brand brand) {
        // Check if name or slug already exists
        if (brandRepository.existsByName(brand.getName())) {
            throw new RuntimeException("Brand name already exists");
        }
        if (brandRepository.existsBySlug(brand.getSlug())) {
            throw new RuntimeException("Brand slug already exists");
        }

        brand.setCreatedAt(LocalDateTime.now());
        return brandRepository.save(brand);
    }

    // Update existing brand
    @Transactional
    @Audited(entity = "Brand", action = "UPDATE")
    public Brand updateBrand(Integer id, Brand updatedBrand) {
        Brand existingBrand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        // Check if new name conflicts with other brands
        if (!existingBrand.getName().equals(updatedBrand.getName())
                && brandRepository.existsByName(updatedBrand.getName())) {
            throw new RuntimeException("Brand name already exists");
        }

        // Check if new slug conflicts with other brands
        if (!existingBrand.getSlug().equals(updatedBrand.getSlug())
                && brandRepository.existsBySlug(updatedBrand.getSlug())) {
            throw new RuntimeException("Brand slug already exists");
        }

        existingBrand.setName(updatedBrand.getName());
        existingBrand.setSlug(updatedBrand.getSlug());

        return brandRepository.save(existingBrand);
    }

    // Delete brand
    @Transactional
    @Audited(entity = "Brand", action = "DELETE")
    public void deleteBrand(Integer id) {
        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
    }

    // Search brands by name
    public List<Brand> searchBrandsByName(String keyword) {
        return brandRepository.findByNameContainingIgnoreCase(keyword);
    }
}