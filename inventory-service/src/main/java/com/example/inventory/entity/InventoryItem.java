package com.example.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", unique = true, nullable = false)
    private String productId;
    
    @Column(name = "product_name", nullable = false)
    private String productName;
    
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;
    
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity = 0;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "location")
    private String location;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public InventoryItem() {
        this.createdAt = LocalDateTime.now();
    }

    public InventoryItem(String productId, String productName, Integer availableQuantity, BigDecimal unitPrice) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.availableQuantity = availableQuantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
        this.updatedAt = LocalDateTime.now();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business Methods
    public boolean canReserve(Integer quantity) {
        return (availableQuantity - reservedQuantity) >= quantity;
    }

    public void reserve(Integer quantity) {
        if (!canReserve(quantity)) {
            throw new RuntimeException("Insufficient inventory for product: " + productId);
        }
        this.reservedQuantity += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void release(Integer quantity) {
        if (reservedQuantity < quantity) {
            throw new RuntimeException("Cannot release more than reserved quantity for product: " + productId);
        }
        this.reservedQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirmReservation(Integer quantity) {
        if (reservedQuantity < quantity) {
            throw new RuntimeException("Cannot confirm more than reserved quantity for product: " + productId);
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity -= quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getEffectiveAvailableQuantity() {
        return availableQuantity - reservedQuantity;
    }
}