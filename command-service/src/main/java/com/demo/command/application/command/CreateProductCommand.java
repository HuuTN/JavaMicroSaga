package com.demo.command.application.command;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/**
 * Command to create a new product.
 * Represents the intention to create a product in the system.
 */
public record CreateProductCommand(
    @NotBlank(message = "Product name is required")
    String name,
    
    String description,
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,
    
    String category,
    
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    int stockQuantity,
    
    @NotBlank(message = "User ID is required")
    String userId
) {
    
    /**
     * Validate the command
     */
    public void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
}