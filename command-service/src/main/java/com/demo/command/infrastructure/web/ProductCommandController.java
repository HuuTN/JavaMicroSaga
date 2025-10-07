package com.demo.command.infrastructure.web;

import com.demo.command.application.command.CreateProductCommand;
import com.demo.command.application.command.UpdateProductCommand;
import com.demo.command.application.service.ProductCommandService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;

/**
 * REST controller for product commands.
 * Handles HTTP requests for write operations on products.
 */
@RestController
@RequestMapping("/api/v1/commands/products")
public class ProductCommandController {

    private static final Logger logger = LoggerFactory.getLogger(ProductCommandController.class);

    private final ProductCommandService productCommandService;

    @Autowired
    public ProductCommandController(ProductCommandService productCommandService) {
        this.productCommandService = productCommandService;
    }

    /**
     * Create a new product
     */
    @PostMapping
    @Timed(value = "command.products.create")
    public ResponseEntity<Map<String, String>> createProduct(
            @Valid @RequestBody CreateProductRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {
        
        logger.info("Received create product request: {}", request);
        
        try {
            CreateProductCommand command = new CreateProductCommand(
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                request.stockQuantity() != null ? request.stockQuantity() : 0,
                userId
            );

            String productId = productCommandService.createProduct(command);
            
            Map<String, String> response = Map.of(
                "productId", productId,
                "message", "Product created successfully"
            );
            
            return ResponseEntity
                .created(URI.create("/api/v1/commands/products/" + productId))
                .body(response);
                
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid product creation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error creating product: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Update an existing product
     */
    @PutMapping("/{productId}")
    @Timed(value = "command.products.update")
    public ResponseEntity<Map<String, String>> updateProduct(
            @PathVariable String productId,
            @Valid @RequestBody UpdateProductRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {
        
        logger.info("Received update product request for ID {}: {}", productId, request);
        
        try {
            UpdateProductCommand command = new UpdateProductCommand(
                productId,
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                request.stockQuantity(),
                userId
            );

            productCommandService.updateProduct(command);
            
            Map<String, String> response = Map.of(
                "productId", productId,
                "message", "Product updated successfully"
            );
            
            return ResponseEntity.ok(response);
                
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid product update request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Reserve stock for a product
     */
    @PostMapping("/{productId}/reserve-stock")
    @Timed(value = "command.products.reserveStock")
    public ResponseEntity<Map<String, String>> reserveStock(
            @PathVariable String productId,
            @RequestBody ReserveStockRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {
        
        logger.info("Received reserve stock request for product {}: {}", productId, request);
        
        try {
            if (request.quantity() <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Quantity must be greater than 0"));
            }

            productCommandService.reserveStock(productId, request.quantity(), userId);
            
            Map<String, String> response = Map.of(
                "productId", productId,
                "quantity", String.valueOf(request.quantity()),
                "message", "Stock reserved successfully"
            );
            
            return ResponseEntity.ok(response);
                
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.warn("Invalid stock reservation request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error reserving stock for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Release reserved stock
     */
    @PostMapping("/{productId}/release-stock")
    @Timed(value = "command.products.releaseStock")
    public ResponseEntity<Map<String, String>> releaseStock(
            @PathVariable String productId,
            @RequestBody ReleaseStockRequest request,
            @RequestHeader(value = "X-User-ID", defaultValue = "system") String userId) {
        
        logger.info("Received release stock request for product {}: {}", productId, request);
        
        try {
            if (request.quantity() <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Quantity must be greater than 0"));
            }

            productCommandService.releaseStock(productId, request.quantity(), userId);
            
            Map<String, String> response = Map.of(
                "productId", productId,
                "quantity", String.valueOf(request.quantity()),
                "message", "Stock released successfully"
            );
            
            return ResponseEntity.ok(response);
                
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid stock release request: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error releasing stock for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "command-service",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}