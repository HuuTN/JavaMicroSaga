package com.demo.projection.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for ProductProjection entities.
 */
@Repository
public interface ProductProjectionRepository extends JpaRepository<ProductProjection, String> {

    /**
     * Find projections by category
     */
    List<ProductProjection> findByCategory(String category);

    /**
     * Find projections by price range
     */
    List<ProductProjection> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find popular products
     */
    List<ProductProjection> findByIsPopularTrue();

    /**
     * Find trending products
     */
    List<ProductProjection> findByIsTrendingTrue();

    /**
     * Find top products by view count
     */
    List<ProductProjection> findTop10ByOrderByViewCountDesc();

    /**
     * Find top products by order count
     */
    List<ProductProjection> findTop10ByOrderByOrderCountDesc();

    /**
     * Find top products by revenue
     */
    List<ProductProjection> findTop10ByOrderByRevenueDesc();

    /**
     * Find products updated after a specific time
     */
    List<ProductProjection> findByLastUpdatedAfter(LocalDateTime dateTime);

    /**
     * Custom query to get product statistics
     */
    @Query("SELECT COUNT(p), AVG(p.price), MIN(p.price), MAX(p.price), AVG(p.averageRating), SUM(p.viewCount) FROM ProductProjection p WHERE p.category = :category")
    Object[] getCategoryStatistics(@Param("category") String category);

    /**
     * Find products with ratings above threshold
     */
    List<ProductProjection> findByAverageRatingGreaterThanEqual(Double rating);

    /**
     * Find products ordered in last period
     */
    @Query("SELECT p FROM ProductProjection p WHERE p.orderCount > 0 AND p.lastUpdated >= :since")
    List<ProductProjection> findProductsOrderedSince(@Param("since") LocalDateTime since);

    /**
     * Get total product count
     */
    @Query("SELECT COUNT(p) FROM ProductProjection p")
    Long getTotalProductCount();

    /**
     * Get total revenue across all products
     */
    @Query("SELECT COALESCE(SUM(p.revenue), 0) FROM ProductProjection p")
    BigDecimal getTotalRevenue();

    /**
     * Get average rating across all products
     */
    @Query("SELECT AVG(p.averageRating) FROM ProductProjection p WHERE p.ratingCount > 0")
    Double getOverallAverageRating();
}