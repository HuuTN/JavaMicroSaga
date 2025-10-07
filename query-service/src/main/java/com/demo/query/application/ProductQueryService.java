package com.demo.query.application;

import com.demo.feign.dto.ProductDto;
import com.demo.feign.dto.ApiResponse;
import com.demo.query.domain.ProductView;
import com.demo.query.domain.ProductViewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Query service for product read operations.
 * Handles all query operations in the CQRS pattern.
 */
@Service
@Transactional(readOnly = true)
public class ProductQueryService {

    private static final Logger logger = LoggerFactory.getLogger(ProductQueryService.class);

    private final ProductViewRepository productViewRepository;

    @Autowired
    public ProductQueryService(ProductViewRepository productViewRepository) {
        this.productViewRepository = productViewRepository;
    }

    /**
     * Get products with pagination and filtering
     */
    public ApiResponse.PagedResponse<ProductDto> getProducts(int page, int size, String category, String name) {
        logger.debug("Getting products: page={}, size={}, category={}, name={}", page, size, category, name);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<ProductView> productPage;

        if (category != null && name != null) {
            productPage = productViewRepository.findByCategoryAndNameContainingIgnoreCase(category, name, pageable);
        } else if (category != null) {
            productPage = productViewRepository.findByCategory(category, pageable);
        } else if (name != null) {
            productPage = productViewRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            productPage = productViewRepository.findAll(pageable);
        }

        List<ProductDto> products = productPage.getContent().stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());

        return ApiResponse.PagedResponse.of(
                products,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements()
        );
    }

    /**
     * Get product by ID
     */
    public Optional<ProductDto> getProductById(String productId) {
        logger.debug("Getting product by ID: {}", productId);
        
        return productViewRepository.findById(productId)
                .map(this::toProductDto);
    }

    /**
     * Check product availability
     */
    public Map<String, Object> checkAvailability(String productId, int quantity) {
        logger.debug("Checking availability for product {} with quantity {}", productId, quantity);
        
        Map<String, Object> response = new HashMap<>();
        
        Optional<ProductView> productOpt = productViewRepository.findById(productId);
        if (productOpt.isEmpty()) {
            response.put("available", false);
            response.put("reason", "Product not found");
            response.put("currentStock", 0);
            return response;
        }

        ProductView product = productOpt.get();
        boolean available = product.hasStock(quantity);
        
        response.put("productId", productId);
        response.put("productName", product.getName());
        response.put("requestedQuantity", quantity);
        response.put("currentStock", product.getStockQuantity());
        response.put("available", available);
        
        if (!available) {
            response.put("reason", "Insufficient stock");
            response.put("maxAvailable", product.getStockQuantity());
        }
        
        return response;
    }

    /**
     * Get products by category
     */
    public List<ProductDto> getProductsByCategory(String category) {
        logger.debug("Getting products by category: {}", category);
        
        return productViewRepository.findByCategory(category).stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }

    /**
     * Search products by name
     */
    public List<ProductDto> searchProducts(String query) {
        logger.debug("Searching products with query: {}", query);
        
        return productViewRepository.findByNameContainingIgnoreCase(query).stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }

    /**
     * Get available products (with stock > 0)
     */
    public List<ProductDto> getAvailableProducts() {
        logger.debug("Getting available products");
        
        return productViewRepository.findByStockQuantityGreaterThan(0).stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }

    /**
     * Get low stock products
     */
    public List<ProductDto> getLowStockProducts(int threshold) {
        logger.debug("Getting low stock products with threshold: {}", threshold);
        
        return productViewRepository.findByStockQuantityLessThanEqual(threshold).stream()
                .map(this::toProductDto)
                .collect(Collectors.toList());
    }

    /**
     * Get product statistics
     */
    public Map<String, Object> getProductStatistics() {
        logger.debug("Getting product statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalProducts", productViewRepository.countAllProducts());
        stats.put("availableProducts", productViewRepository.countAvailableProducts());
        stats.put("averagePrice", productViewRepository.getAveragePrice());
        stats.put("totalStockValue", productViewRepository.getTotalStockValue());
        
        // Category statistics
        List<Object[]> categoryStats = productViewRepository.getCategoryStatistics();
        List<Map<String, Object>> categoryData = categoryStats.stream()
                .map(row -> {
                    Map<String, Object> categoryMap = new HashMap<>();
                    categoryMap.put("category", row[0]);
                    categoryMap.put("productCount", row[1]);
                    categoryMap.put("averagePrice", row[2]);
                    categoryMap.put("totalStock", row[3]);
                    return categoryMap;
                })
                .collect(Collectors.toList());
        
        stats.put("categoryStatistics", categoryData);
        
        return stats;
    }

    /**
     * Convert ProductView to ProductDto
     */
    private ProductDto toProductDto(ProductView productView) {
        return new ProductDto(
                productView.getId(),
                productView.getName(),
                productView.getDescription(),
                productView.getPrice(),
                productView.getCategory(),
                productView.getStockQuantity(),
                productView.getVersion(),
                productView.getCreatedAt(),
                productView.getUpdatedAt(),
                null, // createdBy not available in view
                null  // updatedBy not available in view
        );
    }
}