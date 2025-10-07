package com.demo.query.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductView entity.
 * Provides optimized queries for the read model.
 */
@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, String> {

    /**
     * Find products by category with pagination
     */
    Page<ProductView> findByCategory(String category, Pageable pageable);

    /**
     * Find products by name containing text (case insensitive) with pagination
     */
    Page<ProductView> findByNameContainingIgnoreCase(String name, Pageable pageable);

    /**
     * Find products by category and name with pagination
     */
    Page<ProductView> findByCategoryAndNameContainingIgnoreCase(String category, String name, Pageable pageable);

    /**
     * Find available products (stock > 0)
     */
    List<ProductView> findByStockQuantityGreaterThan(int minStock);

    /**
     * Find products with low stock
     */
    List<ProductView> findByStockQuantityLessThanEqual(int threshold);

    /**
     * Find products by category
     */
    List<ProductView> findByCategory(String category);

    /**
     * Search products by name
     */
    List<ProductView> findByNameContainingIgnoreCase(String name);

    /**
     * Find products in price range
     */
    @Query("SELECT p FROM ProductView p WHERE p.price BETWEEN :minPrice AND :maxPrice")
    List<ProductView> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                      @Param("maxPrice") BigDecimal maxPrice);

    /**
     * Get product statistics
     */
    @Query("SELECT COUNT(p) FROM ProductView p")
    long countAllProducts();

    @Query("SELECT COUNT(p) FROM ProductView p WHERE p.stockQuantity > 0")
    long countAvailableProducts();

    @Query("SELECT AVG(p.price) FROM ProductView p")
    BigDecimal getAveragePrice();

    @Query("SELECT SUM(p.stockQuantity * p.price) FROM ProductView p")
    BigDecimal getTotalStockValue();

    /**
     * Get products by category with statistics
     */
    @Query("SELECT p.category, COUNT(p), AVG(p.price), SUM(p.stockQuantity) " +
           "FROM ProductView p GROUP BY p.category")
    List<Object[]> getCategoryStatistics();

    /**
     * Check if product exists and has sufficient stock
     */
    @Query("SELECT CASE WHEN p.stockQuantity >= :quantity THEN true ELSE false END " +
           "FROM ProductView p WHERE p.id = :productId")
    Optional<Boolean> hasInStock(@Param("productId") String productId, @Param("quantity") int quantity);

    /**
     * Get top products by stock quantity
     */
    @Query("SELECT p FROM ProductView p ORDER BY p.stockQuantity DESC")
    List<ProductView> findTopByStockQuantity(Pageable pageable);

    /**
     * Get recently updated products
     */
    @Query("SELECT p FROM ProductView p ORDER BY p.updatedAt DESC")
    List<ProductView> findRecentlyUpdated(Pageable pageable);
}