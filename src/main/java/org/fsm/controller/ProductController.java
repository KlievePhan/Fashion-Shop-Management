package org.fsm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.fsm.dto.ProductDTO;
import org.fsm.entity.*;
import org.fsm.repository.ProductOptionRepository;
import org.fsm.repository.ProductRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/staff/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;

    @GetMapping("/product/{id}")
    public String getProductDetail(@PathVariable Long id, Model model) {
        // Load product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Load all active options
        List<ProductOption> allOptions = productOptionRepository.findActiveOptionsByProductId(id);

        // Tách SIZE và COLOR options
        List<ProductOption> sizeOptions = allOptions.stream()
                .filter(opt -> "SIZE".equalsIgnoreCase(opt.getOptionType()))
                .collect(Collectors.toList());

        List<ProductOption> colorOptions = allOptions.stream()
                .filter(opt -> "COLOR".equalsIgnoreCase(opt.getOptionType()))
                .collect(Collectors.toList());

        // Add to model
        model.addAttribute("product", product);
        model.addAttribute("sizeOptions", sizeOptions);
        model.addAttribute("colorOptions", colorOptions);

        return "product-detail";
    }

    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        return "staff";
    }

    /**
     * Get single product by ID (for AJAX edit) - Returns DTO instead of Entity
     */
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get products by category filter
     */
    @GetMapping("/filter")
    @ResponseBody
    public ResponseEntity<List<ProductDTO>> filterProductsByCategory(@RequestParam String category) {
        List<Product> products;

        if (category.equals("all")) {
            products = productService.getAllProducts();
        } else {
            products = productService
                    .getProductsByTopLevelCategory(category,
                            org.springframework.data.domain.PageRequest.of(0, 1000))
                    .getContent();
        }

        List<ProductDTO> dtos = products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    // Add this method to your ProductController.java

    /**
     * Search products by SKU or Title
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            // Return all products if search is empty
            List<Product> products = productService.getAllProducts();
            List<ProductDTO> dtos = products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        }

        // Search by title first
        List<Product> products = productService
                .searchProducts(keyword,
                        org.springframework.data.domain.PageRequest.of(0, 1000))
                .getContent();

        // Also search by SKU
        productService.getProductBySku(keyword).ifPresent(products::add);

        // Remove duplicates and convert to DTO
        List<ProductDTO> dtos = products.stream()
                .distinct()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
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
            @RequestParam(required = false) String imageUrls,
            @RequestParam(required = false) String variantsJson,
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
        return "redirect:/staff#products";
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
        return "redirect:/staff#products";
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
        return "redirect:/staff#products";
    }

    @GetMapping("/{id}/variants")
    @ResponseBody
    public ResponseEntity<List<ProductVariant>> getProductVariants(@PathVariable Long id) {
        List<ProductVariant> variants = productService.getVariantsByProductId(id);
        return ResponseEntity.ok(variants);
    }

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

    @GetMapping("/{id}/images")
    @ResponseBody
    public ResponseEntity<List<ProductImage>> getProductImages(@PathVariable Long id) {
        List<ProductImage> images = productService.getImagesByProductId(id);
        return ResponseEntity.ok(images);
    }

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

    // ===== helper methods =====

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setTitle(product.getTitle());
        dto.setDescription(product.getDescription());
        dto.setBasePrice(product.getBasePrice());
        dto.setActive(product.getActive());

        if (product.getCategory() != null) {
            dto.setCategory(new ProductDTO.CategoryInfo(
                    product.getCategory().getId(),
                    product.getCategory().getName()
            ));
        }

        if (product.getBrand() != null) {
            dto.setBrand(new ProductDTO.BrandInfo(
                    product.getBrand().getId(),
                    product.getBrand().getName()
            ));
        }

        if (product.getImages() != null) {
            List<ProductDTO.ImageInfo> imageInfos = product.getImages().stream()
                    .map(img -> new ProductDTO.ImageInfo(
                            img.getId(),
                            img.getUrl(),
                            img.getPrimary(),
                            img.getOrders()
                    ))
                    .collect(Collectors.toList());
            dto.setImages(imageInfos);
        }

        dto.setPrimaryImageUrl(product.getPrimaryImageUrl());
        return dto;
    }

    private List<ProductVariant> parseVariantsJson(String variantsJson) {
        try {
            List<ProductVariant> variants = new ArrayList<>();
            if (variantsJson == null || variantsJson.trim().isEmpty()) {
                return variants;
            }

            List<Map<String, Object>> variantMaps = objectMapper.readValue(
                    variantsJson,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> variantMap : variantMaps) {
                ProductVariant variant = new ProductVariant();

                if (variantMap.containsKey("sku")) {
                    variant.setSku((String) variantMap.get("sku"));
                }

                if (variantMap.containsKey("attributeJson")) {
                    variant.setAttributeJson((String) variantMap.get("attributeJson"));
                }

                if (variantMap.containsKey("price")) {
                    String priceStr = (String) variantMap.get("price");
                    if (priceStr != null && !priceStr.isEmpty()) {
                        variant.setPrice(new BigDecimal(priceStr));
                    }
                }

                if (variantMap.containsKey("stock")) {
                    String stockStr = (String) variantMap.get("stock");
                    if (stockStr != null && !stockStr.isEmpty()) {
                        variant.setStock(Integer.parseInt(stockStr));
                    } else {
                        variant.setStock(0);
                    }
                }

                variants.add(variant);
            }

            return variants;
        } catch (Exception e) {
            System.err.println("Error parsing variants JSON: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

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
