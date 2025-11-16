package org.fsm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.*;
import org.fsm.repository.UserRepository;
import org.fsm.service.AuditLogService;
import org.fsm.service.ProductService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Get single product by ID (for AJAX edit)
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get products by category filter
     */
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<List<Product>> filterProductsByCategory(@RequestParam String category) {
        if (category.equals("all")) {
            return ResponseEntity.ok(productService.getAllProducts());
        }

        List<Product> products = productService.getProductsByTopLevelCategory(category,
                org.springframework.data.domain.PageRequest.of(0, 1000)).getContent();
        return ResponseEntity.ok(products);
    }

    /**
     * Create or Update product with images and variants
     */
    @PostMapping("/save")
    public String saveProduct(
            @RequestParam(required = false) Long id,
            @RequestParam String sku,
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam BigDecimal basePrice,
            @RequestParam(defaultValue = "true") Boolean active,
            @RequestParam(required = false) String imageUrls, // Comma-separated URLs
            @RequestParam(required = false) String variantsJson, // JSON array of variants
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            User currentUser = getCurrentUser();

            // Parse image URLs
            List<String> imageUrlList = null;
            if (imageUrls != null && !imageUrls.trim().isEmpty()) {
                imageUrlList = Arrays.stream(imageUrls.split(","))
                        .map(String::trim)
                        .filter(url -> !url.isEmpty())
                        .collect(Collectors.toList());
            }

            // Parse variants JSON
            List<ProductVariant> variantList = null;
            if (variantsJson != null && !variantsJson.trim().isEmpty()) {
                variantList = parseVariantsJson(variantsJson);
            }

            // Build product object
            Product product = new Product();
            product.setSku(sku);
            product.setTitle(title);
            product.setDescription(description);
            product.setBasePrice(basePrice);
            product.setActive(active);

            // Set category
            Category category = new Category();
            category.setId(categoryId);
            product.setCategory(category);

            // Set brand if provided
            if (brandId != null) {
                Brand brand = new Brand();
                brand.setId(brandId);
                product.setBrand(brand);
            }

            if (id != null) {
                // Update existing product
                Product oldProduct = productService.getProductById(id).orElse(null);
                Product updated = productService.updateProduct(id, product, imageUrlList, variantList);

                // Manual audit log
                if (currentUser != null && oldProduct != null) {
                    auditLogService.createAuditLog(
                            currentUser,
                            "Product",
                            updated.getId().toString(),
                            "UPDATE",
                            String.format("Updated product '%s'", updated.getTitle()),
                            request
                    );
                }

                redirectAttributes.addFlashAttribute("successMessage", "Product updated successfully!");
            } else {
                // Create new product
                Product created = productService.createProduct(product, imageUrlList, variantList);

                // Manual audit log
                if (currentUser != null) {
                    auditLogService.createAuditLog(
                            currentUser,
                            "Product",
                            created.getId().toString(),
                            "CREATE",
                            String.format("Created product '%s'", created.getTitle()),
                            request
                    );
                }

                redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin#products";
    }

    /**
     * Delete product
     */
    @PostMapping("/{id}/delete")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            User currentUser = getCurrentUser();
            Product product = productService.getProductById(id).orElse(null);

            productService.deleteProduct(id);

            // Manual audit log
            if (currentUser != null && product != null) {
                auditLogService.createAuditLog(
                        currentUser,
                        "Product",
                        id.toString(),
                        "DELETE",
                        String.format("Deleted product '%s'", product.getTitle()),
                        request
                );
            }

            redirectAttributes.addFlashAttribute("successMessage", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting product: " + e.getMessage());
        }
        return "redirect:/admin#products";
    }

    /**
     * Toggle product active status
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleProductActive(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request
    ) {
        try {
            User currentUser = getCurrentUser();
            Product product = productService.toggleProductActive(id);

            // Manual audit log
            if (currentUser != null) {
                auditLogService.createAuditLog(
                        currentUser,
                        "Product",
                        id.toString(),
                        "UPDATE",
                        String.format("Toggled product '%s' active status to %s",
                                product.getTitle(), product.getActive()),
                        request
                );
            }

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product " + (product.getActive() ? "activated" : "deactivated") + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error: " + e.getMessage());
        }
        return "redirect:/admin#products";
    }

    /**
     * Get variants for a product
     */
    @GetMapping("/{id}/variants")
    @ResponseBody
    public ResponseEntity<List<ProductVariant>> getProductVariants(@PathVariable Long id) {
        List<ProductVariant> variants = productService.getVariantsByProductId(id);
        return ResponseEntity.ok(variants);
    }

    /**
     * Add variant to product
     */
    @PostMapping("/{id}/variants")
    @ResponseBody
    public ResponseEntity<ProductVariant> addVariant(
            @PathVariable Long id,
            @RequestBody ProductVariant variant
    ) {
        try {
            ProductVariant created = productService.addVariant(id, variant);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update variant
     */
    @PutMapping("/variants/{variantId}")
    @ResponseBody
    public ResponseEntity<ProductVariant> updateVariant(
            @PathVariable Long variantId,
            @RequestBody ProductVariant variant
    ) {
        try {
            ProductVariant updated = productService.updateVariant(variantId, variant);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete variant
     */
    @DeleteMapping("/variants/{variantId}")
    @ResponseBody
    public ResponseEntity<Void> deleteVariant(@PathVariable Long variantId) {
        try {
            productService.deleteVariant(variantId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Get images for a product
     */
    @GetMapping("/{id}/images")
    @ResponseBody
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable Long id) {
        List<ProductImage> images = productService.getImagesByProductId(id);
        return ResponseEntity.ok(images);
    }

    /**
     * Add image to product
     */
    @PostMapping("/{id}/images")
    @ResponseBody
    public ResponseEntity<ProductImage> addImage(
            @PathVariable Long id,
            @RequestParam String imageUrl
    ) {
        try {
            ProductImage created = productService.addImage(id, imageUrl);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete image
     */
    @DeleteMapping("/images/{imageId}")
    @ResponseBody
    public ResponseEntity<Void> deleteImage(@PathVariable Long imageId) {
        try {
            productService.deleteImage(imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Set primary image
     */
    @PostMapping("/images/{imageId}/set-primary")
    @ResponseBody
    public ResponseEntity<Void> setPrimaryImage(@PathVariable Long imageId) {
        try {
            productService.setPrimaryImage(imageId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Helper method to parse variants JSON
     */
    private List<ProductVariant> parseVariantsJson(String variantsJson) {
        try {
            // Expected format: [{"sku":"TS-L-R","size":"L","color":"Red","price":29.99,"stock":50}]
            List<ProductVariant> variants = new ArrayList<>();

            // Simple JSON parsing - you can use ObjectMapper for complex cases
            variantsJson = variantsJson.trim();
            if (variantsJson.startsWith("[") && variantsJson.endsWith("]")) {
                // Remove brackets and split by objects
                String content = variantsJson.substring(1, variantsJson.length() - 1);
                // This is simplified - use ObjectMapper in production
                // For now, return empty list - you can implement proper JSON parsing
            }

            return variants;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Helper method to get current authenticated user
     */
    private User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return null;
            }

            Object principal = authentication.getPrincipal();

            if (principal instanceof User) {
                return (User) principal;
            }

            if (principal instanceof UserDetails) {
                String email = ((UserDetails) principal).getUsername();
                return userRepository.findByEmail(email).orElse(null);
            }

            if (principal instanceof String) {
                String email = (String) principal;
                return userRepository.findByEmail(email).orElse(null);
            }

            return null;

        } catch (Exception e) {
            return null;
        }
    }
}