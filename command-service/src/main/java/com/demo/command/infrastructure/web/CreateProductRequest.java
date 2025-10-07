package com.demo.command.infrastructure.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Request DTO for creating a new product
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
 * Request DTO for updating an existing product
 */
record UpdateProductRequest(
    String name,
    String description,
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,
    
    String category,
    
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    Integer stockQuantity
) {}

/**
 * Request DTO for reserving stock
 */
record ReserveStockRequest(
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be positive")
    Integer quantity
) {}

/**
 * Request DTO for releasing stock
 */
record ReleaseStockRequest(
    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity must be positive")
    Integer quantity
) {}