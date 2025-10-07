package com.demo.query.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product view entity for the read model.
 * Optimized for query operations with denormalized data.
 */
@Entity
@Table(name = "product_view")
public class ProductView {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 100)
    private String category;

    @Column(name = "stock_quantity", nullable = false)
    private int stockQuantity;

    private Long version;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_event_id")
    private String lastEventId;

    @Column(name = "last_event_version")
    private Long lastEventVersion;

    // Default constructor for JPA
    protected ProductView() {}

    // Constructor for creating from events
    public ProductView(String id, String name, String description, BigDecimal price, 
                      String category, int stockQuantity, Long version,
                      LocalDateTime createdAt, LocalDateTime updatedAt,
                      String lastEventId, Long lastEventVersion) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastEventId = lastEventId;
        this.lastEventVersion = lastEventVersion;
    }

    /**
     * Update from event data
     */
    public void updateFromEvent(String name, String description, BigDecimal price, 
                               String category, int stockQuantity, Long version,
                               String eventId, Long eventVersion) {
        if (name != null) this.name = name;
        if (description != null) this.description = description;
        if (price != null) this.price = price;
        if (category != null) this.category = category;
        this.stockQuantity = stockQuantity;
        this.version = version;
        this.updatedAt = LocalDateTime.now();
        this.lastEventId = eventId;
        this.lastEventVersion = eventVersion;
    }

    /**
     * Check if product is available
     */
    public boolean isAvailable() {
        return this.stockQuantity > 0;
    }

    /**
     * Check if product has sufficient stock
     */
    public boolean hasStock(int requiredQuantity) {
        return this.stockQuantity >= requiredQuantity;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStockQuantity() { return stockQuantity; }
    public Long getVersion() { return version; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getLastEventId() { return lastEventId; }
    public Long getLastEventVersion() { return lastEventVersion; }

    @Override
    public String toString() {
        return String.format("ProductView{id='%s', name='%s', price=%s, stockQuantity=%d, version=%d}",
                id, name, price, stockQuantity, version);
    }
}