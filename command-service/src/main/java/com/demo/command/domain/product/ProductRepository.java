package com.demo.command.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity.
 * Provides data access operations for the command side.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

    /**
     * Find products by category
     */
    List<Product> findByCategory(String category);

    /**
     * Find products by name containing the given text (case insensitive)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products with stock quantity greater than or equal to specified amount
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity >= :minQuantity")
    List<Product> findByStockQuantityGreaterThanEqual(@Param("minQuantity") int minQuantity);

    /**
     * Find products that are available (have stock)
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findAvailableProducts();

    /**
     * Check if product exists by name
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Find product by ID with optimistic locking
     */
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") String id);

    /**
     * Find products by category with available stock
     */
    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.stockQuantity > 0")
    List<Product> findByCategoryAndAvailable(@Param("category") String category);
}