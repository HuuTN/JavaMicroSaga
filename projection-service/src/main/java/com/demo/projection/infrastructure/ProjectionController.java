package com.demo.projection.infrastructure;

import com.demo.projection.application.ProjectionService;
import com.demo.projection.domain.ProductProjection;
import com.demo.projection.domain.CategoryProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for projection queries and statistics.
 */
@RestController
@RequestMapping("/api/projections")
@CrossOrigin(origins = "*")
public class ProjectionController {

    private final ProjectionService projectionService;

    @Autowired
    public ProjectionController(ProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    /**
     * Get all product projections
     */
    @GetMapping("/products")
    public ResponseEntity<List<ProductProjection>> getAllProductProjections() {
        List<ProductProjection> projections = projectionService.getAllProductProjections();
        return ResponseEntity.ok(projections);
    }

    /**
     * Get specific product projection
     */
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductProjection> getProductProjection(@PathVariable String productId) {
        Optional<ProductProjection> projection = projectionService.getProductProjection(productId);
        return projection.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get products by category
     */
    @GetMapping("/products/category/{category}")
    public ResponseEntity<List<ProductProjection>> getProductsByCategory(@PathVariable String category) {
        List<ProductProjection> projections = projectionService.getProductsByCategory(category);
        return ResponseEntity.ok(projections);
    }

    /**
     * Get popular products
     */
    @GetMapping("/products/popular")
    public ResponseEntity<List<ProductProjection>> getPopularProducts() {
        List<ProductProjection> projections = projectionService.getPopularProducts();
        return ResponseEntity.ok(projections);
    }

    /**
     * Get trending products
     */
    @GetMapping("/products/trending")
    public ResponseEntity<List<ProductProjection>> getTrendingProducts() {
        List<ProductProjection> projections = projectionService.getTrendingProducts();
        return ResponseEntity.ok(projections);
    }

    /**
     * Get top products by views
     */
    @GetMapping("/products/top-viewed")
    public ResponseEntity<List<ProductProjection>> getTopProductsByViews() {
        List<ProductProjection> projections = projectionService.getTopProductsByViews();
        return ResponseEntity.ok(projections);
    }

    /**
     * Get all category projections
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryProjection>> getAllCategoryProjections() {
        List<CategoryProjection> projections = projectionService.getAllCategoryProjections();
        return ResponseEntity.ok(projections);
    }

    /**
     * Get specific category projection
     */
    @GetMapping("/categories/{category}")
    public ResponseEntity<CategoryProjection> getCategoryProjection(@PathVariable String category) {
        Optional<CategoryProjection> projection = projectionService.getCategoryProjection(category);
        return projection.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get projection summary statistics
     */
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getProjectionSummary() {
        Map<String, Object> summary = projectionService.getProjectionSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Get projection statistics for monitoring
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getProjectionStats() {
        Map<String, Object> stats = projectionService.getProjectionSummary();
        
        // Add additional stats for monitoring
        stats.put("timestamp", System.currentTimeMillis());
        stats.put("service", "projection-service");
        stats.put("version", "1.0.0");
        
        return ResponseEntity.ok(stats);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "projection-service",
            "timestamp", String.valueOf(System.currentTimeMillis())
        ));
    }
}