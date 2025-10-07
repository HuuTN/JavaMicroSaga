package com.demo.command.application.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Command to update an existing product.
 */
public record UpdateProductCommand(
    @NotBlank(message = "Product ID is required")
    String productId,
    
    String name,
    
    String description,
    
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,
    
    String category,
    
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    Integer stockQuantity,
    
    @NotBlank(message = "User ID is required")
    String userId
) {
    
    /**
     * Validate the command
     */
    public void validate() {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID is required");
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (stockQuantity != null && stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
    
    /**
     * Check if there are any updates to apply
     */
    public boolean hasUpdates() {
        return name != null || description != null || price != null || 
               category != null || stockQuantity != null;
    }
}