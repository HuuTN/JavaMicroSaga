package com.demo.projection.application;

import com.demo.projection.domain.ProductProjection;
import com.demo.projection.domain.ProductProjectionRepository;
import com.demo.projection.domain.CategoryProjection;
import com.demo.projection.domain.CategoryProjectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Service for managing product and category projections.
 */
@Service
@Transactional
public class ProjectionService {

    private final ProductProjectionRepository productProjectionRepository;
    private final CategoryProjectionRepository categoryProjectionRepository;

    @Autowired
    public ProjectionService(ProductProjectionRepository productProjectionRepository,
                           CategoryProjectionRepository categoryProjectionRepository) {
        this.productProjectionRepository = productProjectionRepository;
        this.categoryProjectionRepository = categoryProjectionRepository;
    }

    /**
     * Create or update product projection
     */
    public ProductProjection createOrUpdateProductProjection(String productId, String name, 
                                                           String description, BigDecimal price, 
                                                           String category) {
        Optional<ProductProjection> existingProjection = productProjectionRepository.findById(productId);
        
        ProductProjection projection;
        if (existingProjection.isPresent()) {
            projection = existingProjection.get();
            projection.setName(name);
            projection.setDescription(description);
            projection.setPrice(price);
            projection.setCategory(category);
            projection.incrementTotalEvents();
        } else {
            projection = new ProductProjection(productId, name, description, price, category);
            projection.setTotalEvents(1);
        }

        ProductProjection savedProjection = productProjectionRepository.save(projection);
        updateCategoryProjection(category, price);
        
        return savedProjection;
    }

    /**
     * Handle product view event
     */
    public void handleProductView(String productId) {
        Optional<ProductProjection> projection = productProjectionRepository.findById(productId);
        if (projection.isPresent()) {
            ProductProjection productProjection = projection.get();
            productProjection.incrementViewCount();
            productProjectionRepository.save(productProjection);
            
            // Update category views
            updateCategoryViews(productProjection.getCategory(), 1L);
        }
    }

    /**
     * Handle product order event
     */
    public void handleProductOrder(String productId, Integer quantity, BigDecimal orderTotal) {
        Optional<ProductProjection> projection = productProjectionRepository.findById(productId);
        if (projection.isPresent()) {
            ProductProjection productProjection = projection.get();
            productProjection.addOrder(quantity, orderTotal);
            productProjectionRepository.save(productProjection);
            
            // Update category orders
            updateCategoryOrders(productProjection.getCategory(), orderTotal);
        }
    }

    /**
     * Handle product rating event
     */
    public void handleProductRating(String productId, Double rating) {
        Optional<ProductProjection> projection = productProjectionRepository.findById(productId);
        if (projection.isPresent()) {
            ProductProjection productProjection = projection.get();
            productProjection.addRating(rating);
            productProjectionRepository.save(productProjection);
        }
    }

    /**
     * Get all product projections
     */
    @Transactional(readOnly = true)
    public List<ProductProjection> getAllProductProjections() {
        return productProjectionRepository.findAll();
    }

    /**
     * Get product projection by ID
     */
    @Transactional(readOnly = true)
    public Optional<ProductProjection> getProductProjection(String productId) {
        return productProjectionRepository.findById(productId);
    }

    /**
     * Get products by category
     */
    @Transactional(readOnly = true)
    public List<ProductProjection> getProductsByCategory(String category) {
        return productProjectionRepository.findByCategory(category);
    }

    /**
     * Get popular products
     */
    @Transactional(readOnly = true)
    public List<ProductProjection> getPopularProducts() {
        return productProjectionRepository.findByIsPopularTrue();
    }

    /**
     * Get trending products
     */
    @Transactional(readOnly = true)
    public List<ProductProjection> getTrendingProducts() {
        return productProjectionRepository.findByIsTrendingTrue();
    }

    /**
     * Get top products by views
     */
    @Transactional(readOnly = true)
    public List<ProductProjection> getTopProductsByViews() {
        return productProjectionRepository.findTop10ByOrderByViewCountDesc();
    }

    /**
     * Get all category projections
     */
    @Transactional(readOnly = true)
    public List<CategoryProjection> getAllCategoryProjections() {
        return categoryProjectionRepository.findAll();
    }

    /**
     * Get category projection by name
     */
    @Transactional(readOnly = true)
    public Optional<CategoryProjection> getCategoryProjection(String category) {
        return categoryProjectionRepository.findById(category);
    }

    /**
     * Get projection summary statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getProjectionSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        Long totalProducts = productProjectionRepository.getTotalProductCount();
        BigDecimal totalRevenue = productProjectionRepository.getTotalRevenue();
        Double avgRating = productProjectionRepository.getOverallAverageRating();
        
        summary.put("totalProducts", totalProducts);
        summary.put("totalRevenue", totalRevenue);
        summary.put("averageRating", avgRating != null ? avgRating : 0.0);
        summary.put("totalCategories", categoryProjectionRepository.count());
        summary.put("popularProducts", productProjectionRepository.findByIsPopularTrue().size());
        summary.put("trendingProducts", productProjectionRepository.findByIsTrendingTrue().size());
        
        return summary;
    }

    /**
     * Update category projection with new product price
     */
    private void updateCategoryProjection(String category, BigDecimal price) {
        Optional<CategoryProjection> existingCategory = categoryProjectionRepository.findById(category);
        
        CategoryProjection categoryProjection;
        if (existingCategory.isPresent()) {
            categoryProjection = existingCategory.get();
        } else {
            categoryProjection = new CategoryProjection(category);
        }
        
        categoryProjection.updatePriceStatistics(price);
        categoryProjectionRepository.save(categoryProjection);
    }

    /**
     * Update category views
     */
    private void updateCategoryViews(String category, Long views) {
        Optional<CategoryProjection> categoryProjection = categoryProjectionRepository.findById(category);
        if (categoryProjection.isPresent()) {
            CategoryProjection projection = categoryProjection.get();
            projection.incrementViews(views);
            categoryProjectionRepository.save(projection);
        }
    }

    /**
     * Update category orders
     */
    private void updateCategoryOrders(String category, BigDecimal orderValue) {
        Optional<CategoryProjection> categoryProjection = categoryProjectionRepository.findById(category);
        if (categoryProjection.isPresent()) {
            CategoryProjection projection = categoryProjection.get();
            projection.incrementOrders(orderValue);
            categoryProjectionRepository.save(projection);
        }
    }
}