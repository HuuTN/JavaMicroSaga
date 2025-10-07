package com.demo.projection.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Product projection entity for aggregated product data and statistics.
 */
@Entity
@Table(name = "product_projections")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ProductProjection {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private String category;

    @Column(name = "total_events")
    private Integer totalEvents = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "order_count")
    private Integer orderCount = 0;

    @Column(name = "total_ordered_quantity")
    private Integer totalOrderedQuantity = 0;

    @Column(name = "revenue")
    private BigDecimal revenue = BigDecimal.ZERO;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "price_history", columnDefinition = "JSON")
    private String priceHistory;

    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    @Column(name = "is_popular")
    private Boolean isPopular = false;

    @Column(name = "is_trending")
    private Boolean isTrending = false;

    public ProductProjection() {}

    public ProductProjection(String id, String name, String description, BigDecimal price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getTotalEvents() { return totalEvents; }
    public void setTotalEvents(Integer totalEvents) { this.totalEvents = totalEvents; }

    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }

    public Integer getTotalOrderedQuantity() { return totalOrderedQuantity; }
    public void setTotalOrderedQuantity(Integer totalOrderedQuantity) { this.totalOrderedQuantity = totalOrderedQuantity; }

    public BigDecimal getRevenue() { return revenue; }
    public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

    public Double getAverageRating() { return averageRating; }
    public void setAverageRating(Double averageRating) { this.averageRating = averageRating; }

    public Integer getRatingCount() { return ratingCount; }
    public void setRatingCount(Integer ratingCount) { this.ratingCount = ratingCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getPriceHistory() { return priceHistory; }
    public void setPriceHistory(String priceHistory) { this.priceHistory = priceHistory; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Boolean getIsPopular() { return isPopular; }
    public void setIsPopular(Boolean isPopular) { this.isPopular = isPopular; }

    public Boolean getIsTrending() { return isTrending; }
    public void setIsTrending(Boolean isTrending) { this.isTrending = isTrending; }

    public void incrementTotalEvents() {
        this.totalEvents++;
        this.lastUpdated = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
        this.lastUpdated = LocalDateTime.now();
    }

    public void addOrder(Integer quantity, BigDecimal orderTotal) {
        this.orderCount++;
        this.totalOrderedQuantity += quantity;
        this.revenue = this.revenue.add(orderTotal);
        this.lastUpdated = LocalDateTime.now();
        updatePopularityFlags();
    }

    public void addRating(Double rating) {
        double totalRating = this.averageRating * this.ratingCount + rating;
        this.ratingCount++;
        this.averageRating = totalRating / this.ratingCount;
        this.lastUpdated = LocalDateTime.now();
    }

    private void updatePopularityFlags() {
        // Simple popularity logic - can be enhanced
        this.isPopular = this.orderCount > 10 || this.viewCount > 100;
        this.isTrending = this.orderCount > 5; // Orders in recent period
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}