package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Brand;
import org.fsm.entity.Category;
import org.fsm.entity.Product;
import org.fsm.repository.BrandRepository;
import org.fsm.repository.CategoryRepository;
import org.fsm.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    /**
     * Shop page với Pagination, Filter, Search, Sort
     *
     * @param search     - Tìm kiếm theo title
     * @param categoryId - Filter theo category
     * @param brandId    - Filter theo brand
     * @param minPrice   - Filter giá tối thiểu
     * @param maxPrice   - Filter giá tối đa
     * @param sort       - Sắp xếp (latest, price-low, price-high)
     * @param page       - Trang hiện tại (mặc định 0)
     * @param size       - Số items mỗi trang (mặc định 12)
     */
    @GetMapping("/shop")
    public String getShopPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // 1. Xác định sort order
        Sort sortOrder = getSortOrder(sort);

        // 2. Tạo Pageable
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // 3. Query products với filters
        Page<Product> productPage = getFilteredProducts(
                search, categoryId, brandId, minPrice, maxPrice, pageable);

        // 4. Load categories và brands cho filter sidebar
        List<Category> categories = categoryRepository.findAll();
        List<Brand> brands = brandRepository.findAll();

        // 5. Add attributes to model
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", size);

        // Filter options
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);

        // Current filters (để giữ state khi reload)
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategoryId", categoryId);
        model.addAttribute("currentBrandId", brandId);
        model.addAttribute("currentMinPrice", minPrice);
        model.addAttribute("currentMaxPrice", maxPrice);
        model.addAttribute("currentSort", sort);

        return "shop";
    }

    /**
     * Helper: Lấy Sort order dựa trên sort parameter
     */
    private Sort getSortOrder(String sort) {
        return switch (sort) {
            case "price-low" -> Sort.by("basePrice").ascending();
            case "price-high" -> Sort.by("basePrice").descending();
            case "popularity" -> Sort.by("id").descending(); // Tạm thời sort by id
            case "rating" -> Sort.by("id").descending(); // Cần thêm rating field sau
            default -> Sort.by("createdAt").descending(); // latest
        };
    }

    /**
     * Helper: Query products với filters
     */
    private Page<Product> getFilteredProducts(
            String search,
            Integer categoryId,
            Integer brandId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        // Case 1: Có search query
        if (search != null && !search.trim().isEmpty()) {
            // Search với price filter
            if (minPrice != null || maxPrice != null) {
                BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
                BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999");
                return productRepository.searchByTitleAndPriceRange(search.trim(), min, max, pageable);
            }
            return productRepository.searchByTitle(search.trim(), pageable);
        }

        // Case 2: Filter by category
        if (categoryId != null) {
            // Category + price filter
            if (minPrice != null || maxPrice != null) {
                BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
                BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999");
                return productRepository.findByCategoryIdAndBasePriceBetween(categoryId, min, max, pageable);
            }
            return productRepository.findByCategoryId(categoryId, pageable);
        }

        // Case 3: Filter by brand
        if (brandId != null) {
            // Brand + price filter
            if (minPrice != null || maxPrice != null) {
                BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
                BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999");
                return productRepository.findByBrandIdAndBasePriceBetween(brandId, min, max, pageable);
            }
            return productRepository.findByBrandId(brandId, pageable);
        }

        // Case 4: Only price filter
        if (minPrice != null || maxPrice != null) {
            BigDecimal min = minPrice != null ? minPrice : BigDecimal.ZERO;
            BigDecimal max = maxPrice != null ? maxPrice : new BigDecimal("999999");
            return productRepository.findByActiveTrueAndBasePriceBetween(min, max, pageable);
        }

        // Default: Lấy tất cả active products
        return productRepository.findByActiveTrue(pageable);
    }
}