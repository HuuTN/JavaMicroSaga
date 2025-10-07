package com.demo.feign.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request DTOs for Command Service operations
 */
public class CommandRequest {

    /**
     * Create product command request
     */
    public record CreateProductRequest(
        @NotBlank(message = "Product name is required")
        String name,
        
        String description,
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,
        
        String category,
        
        @PositiveOrZero(message = "Stock quantity cannot be negative")
        Integer stockQuantity
    ) {}

    /**
     * Update product command request
     */
    public record UpdateProductRequest(
        String name,
        String description,
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal price,
        
        String category,
        
        @PositiveOrZero(message = "Stock quantity cannot be negative")
        Integer stockQuantity
    ) {}

    /**
     * Reserve stock request
     */
    public record ReserveStockRequest(
        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be positive")
        Integer quantity
    ) {}

    /**
     * Release stock request
     */
    public record ReleaseStockRequest(
        @NotNull(message = "Quantity is required")
        @PositiveOrZero(message = "Quantity must be positive")
        Integer quantity
    ) {}
}