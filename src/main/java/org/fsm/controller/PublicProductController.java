package org.fsm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.fsm.entity.Product;
import org.fsm.entity.ProductImage;
import org.fsm.entity.ProductOption;
import org.fsm.repository.ProductImageRepository;
import org.fsm.repository.ProductOptionRepository;
import org.fsm.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Public-facing product controller for customer views
 * Separate from staff ProductController which has /staff/products prefix
 */
@Controller
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ObjectMapper objectMapper;

    /**
     * Public product detail page
     * URL: /product/{id}
     */
    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        // ⭐ Fetch product
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        // ⭐ Fetch images directly from repository
        List<ProductImage> allImages = productImageRepository.findByProductIdOrderByOrdersAsc(id);

        System.out.println("===========================================");
        System.out.println("🔍 DEBUG: Product ID = " + id);
        System.out.println("🖼️ Total images found: " + allImages.size());

        // Debug each image
        for (ProductImage img : allImages) {
            System.out.println("  ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("  Image ID: " + img.getId());
            System.out.println("  URL: " + img.getUrl());
            System.out.println("  Color Variant: '" + img.getColorVariant() + "'");
            System.out.println("  Orders: " + img.getOrders());
            System.out.println("  Is Primary: " + img.getPrimary());
        }

        // ⭐ Fetch options
        List<ProductOption> allOptions = productOptionRepository.findActiveOptionsByProductId(id);

        List<ProductOption> sizeOptions = allOptions.stream()
                .filter(opt -> "SIZE".equalsIgnoreCase(opt.getOptionType()))
                .collect(Collectors.toList());

        List<ProductOption> colorOptions = allOptions.stream()
                .filter(opt -> "COLOR".equalsIgnoreCase(opt.getOptionType()))
                .collect(Collectors.toList());

        // ⭐ BUILD imagesByColor map
        Map<String, List<Map<String, Object>>> imagesByColor = new LinkedHashMap<>();

        System.out.println("🎨 Building imagesByColor map...");

        for (ProductImage image : allImages) {
            String color = image.getColorVariant();

            System.out.println("  → Processing image ID: " + image.getId());
            System.out.println("    Color value: '" + color + "'");

            // ⭐ Check for null or empty
            if (color == null || color.trim().isEmpty()) {
                System.out.println("    ⚠️ SKIPPED - No color variant");
                continue;
            }

            // Create image map
            Map<String, Object> imageMap = new HashMap<>();
            imageMap.put("id", image.getId());
            imageMap.put("url", image.getUrl());
            imageMap.put("orders", image.getOrders() != null ? image.getOrders() : 0);
            imageMap.put("primary", image.getPrimary() != null ? image.getPrimary() : false);

            // Add to color group
            imagesByColor.computeIfAbsent(color, k -> new ArrayList<>()).add(imageMap);
            System.out.println("    ✅ ADDED to group: '" + color + "'");
        }

        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        System.out.println("🎨 FINAL imagesByColor:");
        System.out.println("   Color groups: " + imagesByColor.keySet());
        System.out.println("   Total groups: " + imagesByColor.size());

        for (Map.Entry<String, List<Map<String, Object>>> entry : imagesByColor.entrySet()) {
            System.out.println("   - " + entry.getKey() + ": " + entry.getValue().size() + " images");
        }

        // Sort images by orders within each color
        imagesByColor.values().forEach(images ->
                images.sort((a, b) ->
                        Integer.compare((Integer)a.get("orders"), (Integer)b.get("orders"))
                )
        );

        // ⭐ Convert to JSON
        String imagesByColorJson = "{}";

        try {
            imagesByColorJson = objectMapper.writeValueAsString(imagesByColor);
            System.out.println("📦 JSON Output Length: " + imagesByColorJson.length() + " chars");
            System.out.println("📦 JSON Content: " + imagesByColorJson);
        } catch (Exception e) {
            System.err.println("❌ ERROR converting to JSON: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("===========================================");

        // ⭐ Add to model
        model.addAttribute("currentPath", "/product/" + id);
        model.addAttribute("product", product);
        model.addAttribute("sizeOptions", sizeOptions);
        model.addAttribute("colorOptions", colorOptions);
        model.addAttribute("imagesByColorJson", imagesByColorJson);

        // ⭐ Related products
        List<Product> relatedProducts = productRepository.findAll()
                .stream()
                .filter(p -> !p.getId().equals(id) && p.getActive())
                .limit(4)
                .collect(Collectors.toList());
        model.addAttribute("relatedProducts", relatedProducts);

        return "product-detail";
    }
}