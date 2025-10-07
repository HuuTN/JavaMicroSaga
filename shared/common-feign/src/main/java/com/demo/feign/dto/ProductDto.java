package com.demo.feign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product DTO for Feign client communication
 */
public record ProductDto(
    @NotBlank(message = "Product ID is required")
    String id,
    
    @NotBlank(message = "Product name is required")
    String name,
    
    String description,
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    BigDecimal price,
    
    String category,
    
    @PositiveOrZero(message = "Stock quantity cannot be negative")
    int stockQuantity,
    
    Long version,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,
    
    String createdBy,
    String updatedBy
) {
    
    /**
     * Create a minimal product DTO for availability check
     */
    public static ProductDto forAvailabilityCheck(String id, String name, int stockQuantity) {
        return new ProductDto(
            id, name, null, null, null, stockQuantity,
            null, null, null, null, null
        );
    }
    
    /**
     * Check if product is available in required quantity
     */
    public boolean isAvailable(int requiredQuantity) {
        return this.stockQuantity >= requiredQuantity;
    }
}