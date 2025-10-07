package com.demo.projection.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CategoryProjection entities.
 */
@Repository
public interface CategoryProjectionRepository extends JpaRepository<CategoryProjection, String> {

    /**
     * Find categories ordered by product count
     */
    List<CategoryProjection> findAllByOrderByProductCountDesc();

    /**
     * Find categories ordered by total revenue
     */
    List<CategoryProjection> findAllByOrderByTotalRevenueDesc();

    /**
     * Find categories ordered by average rating
     */
    List<CategoryProjection> findAllByOrderByAvgRatingDesc();

    /**
     * Find categories with product count greater than threshold
     */
    List<CategoryProjection> findByProductCountGreaterThan(Integer count);

    /**
     * Find categories with revenue greater than threshold
     */
    List<CategoryProjection> findByTotalRevenueGreaterThan(BigDecimal revenue);

    /**
     * Custom query to get overall statistics
     */
    @Query("SELECT COUNT(c), SUM(c.productCount), AVG(c.avgPrice), SUM(c.totalRevenue), AVG(c.avgRating) FROM CategoryProjection c")
    Object[] getOverallStatistics();

    /**
     * Find top categories by total orders
     */
    List<CategoryProjection> findTop5ByOrderByTotalOrdersDesc();

    /**
     * Find categories with ratings above threshold
     */
    List<CategoryProjection> findByAvgRatingGreaterThanEqual(Double rating);
}