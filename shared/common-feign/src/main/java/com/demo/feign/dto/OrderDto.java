package com.demo.feign.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order DTO for Feign client communication
 */
public record OrderDto(
    @NotBlank(message = "Order ID is required")
    String id,
    
    @NotBlank(message = "Customer ID is required") 
    String customerId,
    
    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    BigDecimal totalAmount,
    
    @NotBlank(message = "Status is required")
    String status,
    
    List<OrderItemDto> items,
    
    int itemCount,
    
    Long version,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime createdAt,
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime updatedAt,
    
    String lastEventId,
    Long lastEventVersion
) {
    
    /**
     * Order item DTO
     */
    public record OrderItemDto(
        String id,
        String orderId,
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice
    ) {}
}