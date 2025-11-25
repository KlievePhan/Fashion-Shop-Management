package org.fsm.service;

import lombok.RequiredArgsConstructor;
import org.fsm.annotation.Audited;
import org.fsm.entity.*;
import org.fsm.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get products with pagination
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    /**
     * Get product by ID with images and variants
     */
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Get product by SKU
     */
    public Optional<Product> getProductBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    /**
     * Get products by category (top-level only: man, woman, unisex)
     */
    public Page<Product> getProductsByTopLevelCategory(String categorySlug, Pageable pageable) {
        Optional<Category> category = categoryRepository.findBySlug(categorySlug.toLowerCase());
        if (category.isPresent()) {
            return productRepository.findByCategoryId(category.get().getId(), pageable);
        }
        return Page.empty();
    }

    /**
     * Get products by brand
     */
    public Page<Product> getProductsByBrand(Integer brandId, Pageable pageable) {
        return productRepository.findByBrandId(brandId, pageable);
    }

    /**
     * Get active products
     */
    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    /**
     * Search products by title
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByTitle(keyword, pageable);
    }

    /**
     * Get recent products
     */
    public List<Product> getRecentProducts() {
        return productRepository.findTop10ByOrderByCreatedAtDesc();
    }

    /**
     * Create new product with images and variants
     */
    @Audited(entity = "Product", action = "CREATE")
    @Transactional
    public Product createProduct(Product product, List<String> imageUrls, List<ProductVariant> variants) {
        // Validate SKU uniqueness
        if (product.getSku() != null && productRepository.findBySku(product.getSku()).isPresent()) {
            throw new RuntimeException("SKU already exists: " + product.getSku());
        }

        // Set timestamps
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        // Save product first
        Product savedProduct = productRepository.save(product);

        // Save images if provided
        if (imageUrls != null && !imageUrls.isEmpty()) {
            saveProductImages(savedProduct, imageUrls);
        }

        // Save variants if provided
        if (variants != null && !variants.isEmpty()) {
            saveProductVariants(savedProduct, variants);
        }

        return savedProduct;
    }

    /**
     * Update existing product with images and variants
     */
    @Audited(entity = "Product", action = "UPDATE")
    @Transactional
    public Product updateProduct(Long id, Product updatedProduct, List<String> imageUrls, List<ProductVariant> variants) {
        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // Check SKU uniqueness if changed
        if (updatedProduct.getSku() != null &&
                !existingProduct.getSku().equals(updatedProduct.getSku()) &&
                productRepository.findBySku(updatedProduct.getSku()).isPresent()) {
            throw new RuntimeException("SKU already exists: " + updatedProduct.getSku());
        }

        // Update fields
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setTitle(updatedProduct.getTitle());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setBrand(updatedProduct.getBrand());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setBasePrice(updatedProduct.getBasePrice());
        existingProduct.setActive(updatedProduct.getActive());
        existingProduct.setUpdatedAt(LocalDateTime.now());

        // Save product
        Product savedProduct = productRepository.save(existingProduct);

        // Update images if provided
        if (imageUrls != null && !imageUrls.isEmpty()) {
            // Delete old images
            List<ProductImage> oldImages = productImageRepository.findByProductId(id);
            productImageRepository.deleteAll(oldImages);

            // Save new images
            saveProductImages(savedProduct, imageUrls);
        }

        // Update variants if provided
        if (variants != null && !variants.isEmpty()) {
            // Delete old variants
            List<ProductVariant> oldVariants = productVariantRepository.findByProductId(id);
            productVariantRepository.deleteAll(oldVariants);

            // Save new variants
            saveProductVariants(savedProduct, variants);
        }

        return savedProduct;
    }

    /**
     * Delete product (cascades to images and variants)
     */
    @Audited(entity = "Product", action = "DELETE")
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    /**
     * Toggle product active status
     */
    @Transactional
    public Product toggleProductActive(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setActive(!product.getActive());
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    /**
     * Helper method to save product images
     */
    private void saveProductImages(Product product, List<String> imageUrls) {
        List<ProductImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url(imageUrls.get(i))
                    .orders(i)
                    .primary(i == 0) // First image is primary
                    .build();
            images.add(image);
        }
        productImageRepository.saveAll(images);
    }

    /**
     * Helper method to save product variants
     */
    private void saveProductVariants(Product product, List<ProductVariant> variants) {
        for (ProductVariant variant : variants) {
            // Check SKU uniqueness for variants
            if (variant.getSku() != null && productVariantRepository.existsBySku(variant.getSku())) {
                throw new RuntimeException("Variant SKU already exists: " + variant.getSku());
            }

            variant.setProduct(product);
            variant.setCreatedAt(LocalDateTime.now());
            variant.setUpdatedAt(LocalDateTime.now());
        }
        productVariantRepository.saveAll(variants);
    }

    /**
     * Get variants by product ID
     */
    public List<ProductVariant> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductId(productId);
    }

    /**
     * Get images by product ID
     */
    public List<ProductImage> getImagesByProductId(Long productId) {
        return productImageRepository.findByProductIdOrderByOrdersAsc(productId);
    }
    
    /**
     * Search products by SKU or title
     */
    public Page<Product> searchProductsBySkuOrTitle(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.searchBySkuOrTitle(keyword.trim(), pageable);
    }

    /**
     * Search products by SKU or title (returns List for simpler usage)
     */
    public List<Product> searchProductsBySkuOrTitle(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll();
        }
        return productRepository.searchBySkuOrTitle(
                keyword.trim(),
                org.springframework.data.domain.PageRequest.of(0, 1000)
        ).getContent();
    }

    /**
     * Add variant to existing product
     */
    @Transactional
    public ProductVariant addVariant(Long productId, ProductVariant variant) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Check SKU uniqueness
        if (variant.getSku() != null && productVariantRepository.existsBySku(variant.getSku())) {
            throw new RuntimeException("Variant SKU already exists: " + variant.getSku());
        }

        variant.setProduct(product);
        variant.setCreatedAt(LocalDateTime.now());
        variant.setUpdatedAt(LocalDateTime.now());

        return productVariantRepository.save(variant);
    }

    /**
     * Update variant
     */
    @Transactional
    public ProductVariant updateVariant(Long variantId, ProductVariant updatedVariant) {
        ProductVariant existingVariant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new RuntimeException("Variant not found with id: " + variantId));

        // Check SKU uniqueness if changed
        if (updatedVariant.getSku() != null &&
                !existingVariant.getSku().equals(updatedVariant.getSku()) &&
                productVariantRepository.existsBySku(updatedVariant.getSku())) {
            throw new RuntimeException("Variant SKU already exists: " + updatedVariant.getSku());
        }

        existingVariant.setSku(updatedVariant.getSku());
        existingVariant.setAttributeJson(updatedVariant.getAttributeJson());
        existingVariant.setPrice(updatedVariant.getPrice());
        existingVariant.setStock(updatedVariant.getStock());
        existingVariant.setUpdatedAt(LocalDateTime.now());

        return productVariantRepository.save(existingVariant);
    }

    /**
     * Delete variant
     */
    @Transactional
    public void deleteVariant(Long variantId) {
        if (!productVariantRepository.existsById(variantId)) {
            throw new RuntimeException("Variant not found with id: " + variantId);
        }
        productVariantRepository.deleteById(variantId);
    }

    /**
     * Add image to existing product
     */
    @Transactional
    public ProductImage addImage(Long productId, String imageUrl) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Get current max order
        List<ProductImage> existingImages = productImageRepository.findByProductId(productId);
        int maxOrder = existingImages.stream()
                .mapToInt(ProductImage::getOrders)
                .max()
                .orElse(-1);

        ProductImage image = ProductImage.builder()
                .product(product)
                .url(imageUrl)
                .orders(maxOrder + 1)
                .primary(existingImages.isEmpty()) // Primary if first image
                .build();

        return productImageRepository.save(image);
    }

    /**
     * Delete image
     */
    @Transactional
    public void deleteImage(Long imageId) {
        if (!productImageRepository.existsById(imageId)) {
            throw new RuntimeException("Image not found with id: " + imageId);
        }
        productImageRepository.deleteById(imageId);
    }

    /**
     * Set primary image
     */
    @Transactional
    public void setPrimaryImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found with id: " + imageId));

        // Remove primary from all images of this product
        List<ProductImage> productImages = productImageRepository.findByProductId(image.getProduct().getId());
        productImages.forEach(img -> img.setPrimary(false));
        productImageRepository.saveAll(productImages);

        // Set this image as primary
        image.setPrimary(true);
        productImageRepository.save(image);
    }

    /**
     * Get top-level categories only (man, woman, unisex)
     */
    public List<Category> getTopLevelCategories() {
        return categoryRepository.findByParentIsNull();
    }

    /**
     * Get all brands
     */
    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }
}