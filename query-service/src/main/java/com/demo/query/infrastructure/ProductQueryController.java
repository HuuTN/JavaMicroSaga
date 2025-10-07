package com.demo.query.infrastructure;

import com.demo.feign.dto.ApiResponse;
import com.demo.feign.dto.ProductDto;
import com.demo.query.application.ProductQueryService;
import io.micrometer.core.annotation.Timed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for product queries.
 * Handles HTTP requests for read operations on products.
 */
@RestController
@RequestMapping("/api/v1/queries/products")
public class ProductQueryController {

    private static final Logger logger = LoggerFactory.getLogger(ProductQueryController.class);

    private final ProductQueryService productQueryService;

    @Autowired
    public ProductQueryController(ProductQueryService productQueryService) {
        this.productQueryService = productQueryService;
    }

    /**
     * Get all products with pagination and filtering
     */
    @GetMapping
    @Timed(value = "query.products.get")
    public ResponseEntity<ApiResponse.PagedResponse<ProductDto>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String name) {
        
        logger.info("Getting products: page={}, size={}, category={}, name={}", page, size, category, name);
        
        try {
            ApiResponse.PagedResponse<ProductDto> response = productQueryService.getProducts(page, size, category, name);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get product by ID
     */
    @GetMapping("/{productId}")
    @Timed(value = "query.products.getById")
    public ResponseEntity<ProductDto> getProductById(@PathVariable String productId) {
        logger.info("Getting product by ID: {}", productId);
        
        try {
            return productQueryService.getProductById(productId)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Check product availability
     */
    @GetMapping("/{productId}/availability")
    @Timed(value = "query.products.checkAvailability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable String productId,
            @RequestParam int quantity) {
        
        logger.info("Checking availability for product {} with quantity {}", productId, quantity);
        
        try {
            Map<String, Object> availability = productQueryService.checkAvailability(productId, quantity);
            return ResponseEntity.ok(availability);
        } catch (Exception e) {
            logger.error("Error checking availability for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get products by category
     */
    @GetMapping("/category/{category}")
    @Timed(value = "query.products.byCategory")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        logger.info("Getting products by category: {}", category);
        
        try {
            List<ProductDto> products = productQueryService.getProductsByCategory(category);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error getting products by category {}: {}", category, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search products by name
     */
    @GetMapping("/search")
    @Timed(value = "query.products.search")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam("q") String query) {
        logger.info("Searching products with query: {}", query);
        
        try {
            List<ProductDto> products = productQueryService.searchProducts(query);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error searching products with query {}: {}", query, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get available products (with stock > 0)
     */
    @GetMapping("/available")
    @Timed(value = "query.products.available")
    public ResponseEntity<List<ProductDto>> getAvailableProducts() {
        logger.info("Getting available products");
        
        try {
            List<ProductDto> products = productQueryService.getAvailableProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error getting available products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get low stock products
     */
    @GetMapping("/low-stock")
    @Timed(value = "query.products.lowStock")
    public ResponseEntity<List<ProductDto>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        
        logger.info("Getting low stock products with threshold: {}", threshold);
        
        try {
            List<ProductDto> products = productQueryService.getLowStockProducts(threshold);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            logger.error("Error getting low stock products: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get product statistics
     */
    @GetMapping("/statistics")
    @Timed(value = "query.products.statistics")
    public ResponseEntity<Map<String, Object>> getProductStatistics() {
        logger.info("Getting product statistics");
        
        try {
            Map<String, Object> statistics = productQueryService.getProductStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error getting product statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "query-service",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}