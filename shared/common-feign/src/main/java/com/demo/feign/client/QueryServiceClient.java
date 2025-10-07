package com.demo.feign.client;

import com.demo.feign.dto.ApiResponse;
import com.demo.feign.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.demo.feign.config.QueryServiceClientConfiguration;

import java.util.List;
import java.util.Map;

/**
 * Feign client for Query Service operations.
 * Provides type-safe HTTP client for query operations.
 */
@FeignClient(
    name = "query-service",
    url = "${services.query-service.url:http://localhost:8082}",
    configuration = QueryServiceClientConfiguration.class
)
public interface QueryServiceClient {

    /**
     * Get all products with pagination
     */
    @GetMapping("/api/v1/queries/products")
    ResponseEntity<ApiResponse.PagedResponse<ProductDto>> getProducts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String name
    );

    /**
     * Get product by ID
     */
    @GetMapping("/api/v1/queries/products/{productId}")
    ResponseEntity<ProductDto> getProductById(@PathVariable("productId") String productId);

    /**
     * Check product availability
     */
    @GetMapping("/api/v1/queries/products/{productId}/availability")
    ResponseEntity<Map<String, Object>> checkAvailability(
        @PathVariable("productId") String productId,
        @RequestParam int quantity
    );

    /**
     * Get products by category
     */
    @GetMapping("/api/v1/queries/products/category/{category}")
    ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable("category") String category);

    /**
     * Search products by name
     */
    @GetMapping("/api/v1/queries/products/search")
    ResponseEntity<List<ProductDto>> searchProducts(@RequestParam("q") String query);

    /**
     * Get available products (with stock > 0)
     */
    @GetMapping("/api/v1/queries/products/available")
    ResponseEntity<List<ProductDto>> getAvailableProducts();

    /**
     * Get low stock products
     */
    @GetMapping("/api/v1/queries/products/low-stock")
    ResponseEntity<List<ProductDto>> getLowStockProducts(@RequestParam(defaultValue = "10") int threshold);

    /**
     * Get product statistics
     */
    @GetMapping("/api/v1/queries/products/statistics")
    ResponseEntity<Map<String, Object>> getProductStatistics();

    /**
     * Health check endpoint
     */
    @GetMapping("/actuator/health")
    ResponseEntity<Map<String, Object>> healthCheck();
}