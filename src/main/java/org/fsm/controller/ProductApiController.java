package org.fsm.controller;

import lombok.RequiredArgsConstructor;
import org.fsm.entity.Product;
import org.fsm.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API Controller for Products
 * Sử dụng cho AJAX calls từ shop.js (Quick View feature)
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApiController {

  private final ProductRepository productRepository;

  /**
   * Get product by ID for Quick View modal
   * 
   * @param id Product ID
   * @return Product details with variants
   */
  @GetMapping("/{id}")
  public ResponseEntity<Product> getProductById(@PathVariable Long id) {
    return productRepository.findById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }
}