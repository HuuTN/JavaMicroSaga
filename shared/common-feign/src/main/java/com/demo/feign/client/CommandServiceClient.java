package com.demo.feign.client;

import com.demo.feign.dto.ApiResponse;
import com.demo.feign.dto.CommandRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.demo.feign.config.CommandServiceClientConfiguration;

import java.util.Map;

/**
 * Feign client for Command Service operations.
 * Provides type-safe HTTP client for command operations.
 */
@FeignClient(
    name = "command-service",
    url = "${services.command-service.url:http://localhost:8081}",
    configuration = CommandServiceClientConfiguration.class
)
public interface CommandServiceClient {

    /**
     * Create a new product
     */
    @PostMapping("/api/v1/commands/products")
    ResponseEntity<Map<String, String>> createProduct(
        @RequestBody CommandRequest.CreateProductRequest request,
        @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId
    );

    /**
     * Update an existing product
     */
    @PutMapping("/api/v1/commands/products/{productId}")
    ResponseEntity<Map<String, String>> updateProduct(
        @PathVariable("productId") String productId,
        @RequestBody CommandRequest.UpdateProductRequest request,
        @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId
    );

    /**
     * Reserve stock for a product
     */
    @PostMapping("/api/v1/commands/products/{productId}/reserve-stock")
    ResponseEntity<Map<String, String>> reserveStock(
        @PathVariable("productId") String productId,
        @RequestBody CommandRequest.ReserveStockRequest request,
        @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId
    );

    /**
     * Release reserved stock
     */
    @PostMapping("/api/v1/commands/products/{productId}/release-stock")
    ResponseEntity<Map<String, String>> releaseStock(
        @PathVariable("productId") String productId,
        @RequestBody CommandRequest.ReleaseStockRequest request,
        @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId
    );

    /**
     * Health check endpoint
     */
    @GetMapping("/actuator/health")
    ResponseEntity<Map<String, Object>> healthCheck();
}