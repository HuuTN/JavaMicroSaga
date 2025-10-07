package com.demo.projection.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Category projection entity for category-level aggregations and statistics.
 */
@Entity
@Table(name = "category_projections")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CategoryProjection {

    @Id
    private String category;

    @Column(name = "product_count")
    private Integer productCount = 0;

    @Column(name = "avg_price", precision = 10, scale = 2)
    private BigDecimal avgPrice = BigDecimal.ZERO;

    @Column(name = "min_price", precision = 10, scale = 2)
    private BigDecimal minPrice = BigDecimal.ZERO;

    @Column(name = "max_price", precision = 10, scale = 2)
    private BigDecimal maxPrice = BigDecimal.ZERO;

    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Column(name = "total_orders")
    private Integer totalOrders = 0;

    @Column(name = "total_revenue", precision = 15, scale = 2)
    private BigDecimal totalRevenue = BigDecimal.ZERO;

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

    @Column(name = "total_views")
    private Long totalViews = 0L;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public CategoryProjection() {}

    public CategoryProjection(String category) {
        this.category = category;
        this.lastUpdated = LocalDateTime.now();
    }

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getProductCount() { return productCount; }
    public void setProductCount(Integer productCount) { this.productCount = productCount; }

    public BigDecimal getAvgPrice() { return avgPrice; }
    public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }

    public BigDecimal getMinPrice() { return minPrice; }
    public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }

    public BigDecimal getMaxPrice() { return maxPrice; }
    public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

    public Long getTotalViews() { return totalViews; }
    public void setTotalViews(Long totalViews) { this.totalViews = totalViews; }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public void updatePriceStatistics(BigDecimal newPrice) {
        if (this.productCount == 0) {
            this.minPrice = newPrice;
            this.maxPrice = newPrice;
            this.avgPrice = newPrice;
            this.totalValue = newPrice;
        } else {
            if (newPrice.compareTo(this.minPrice) < 0) {
                this.minPrice = newPrice;
            }
            if (newPrice.compareTo(this.maxPrice) > 0) {
                this.maxPrice = newPrice;
            }
            this.totalValue = this.totalValue.add(newPrice);
            this.avgPrice = this.totalValue.divide(BigDecimal.valueOf(this.productCount + 1), 2, BigDecimal.ROUND_HALF_UP);
        }
        this.productCount++;
        this.lastUpdated = LocalDateTime.now();
    }

    public void incrementOrders(BigDecimal orderValue) {
        this.totalOrders++;
        this.totalRevenue = this.totalRevenue.add(orderValue);
        this.lastUpdated = LocalDateTime.now();
    }

    public void incrementViews(Long views) {
        this.totalViews += views;
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
}