package com.demo.command.application.integration;

import com.demo.feign.client.QueryServiceClient;
import com.demo.feign.config.FeignErrorDecoder;
import com.demo.feign.dto.ProductDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Integration service using Feign clients to communicate with other services.
 * Demonstrates cross-service communication in CQRS architecture.
 */
//@Service  // Temporarily disabled due to Feign client issues
public class ProductIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(ProductIntegrationService.class);

    private final QueryServiceClient queryServiceClient;

    @Autowired
    public ProductIntegrationService(QueryServiceClient queryServiceClient) {
        this.queryServiceClient = queryServiceClient;
    }

    /**
     * Check if product exists and has sufficient stock before processing command
     */
    public boolean validateProductAvailability(String productId, int requiredQuantity) {
        logger.debug("Validating product availability: productId={}, quantity={}", productId, requiredQuantity);
        
        try {
            ResponseEntity<Map<String, Object>> response = queryServiceClient.checkAvailability(productId, requiredQuantity);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Boolean available = (Boolean) response.getBody().get("available");
                logger.info("Product {} availability check: {}", productId, available);
                return Boolean.TRUE.equals(available);
            }
            
            logger.warn("Invalid response from query service for product {}", productId);
            return false;
            
        } catch (FeignErrorDecoder.FeignNotFoundException e) {
            logger.warn("Product not found: {}", productId);
            return false;
        } catch (FeignErrorDecoder.FeignClientException e) {
            logger.error("Error checking product availability: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error checking product availability: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get product information from query service
     */
    public Optional<ProductDto> getProductInfo(String productId) {
        logger.debug("Getting product info for: {}", productId);
        
        try {
            ResponseEntity<ProductDto> response = queryServiceClient.getProductById(productId);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                ProductDto product = response.getBody();
                logger.debug("Retrieved product info: {}", product.name());
                return Optional.of(product);
            }
            
            return Optional.empty();
            
        } catch (FeignErrorDecoder.FeignNotFoundException e) {
            logger.debug("Product not found: {}", productId);
            return Optional.empty();
        } catch (FeignErrorDecoder.FeignClientException e) {
            logger.error("Error getting product info: {}", e.getMessage());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Unexpected error getting product info: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Check if a product name already exists (to prevent duplicates)
     */
    public boolean isProductNameExists(String productName) {
        logger.debug("Checking if product name exists: {}", productName);
        
        try {
            ResponseEntity<java.util.List<ProductDto>> response = queryServiceClient.searchProducts(productName);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                boolean exists = response.getBody().stream()
                        .anyMatch(product -> product.name().equalsIgnoreCase(productName));
                
                logger.debug("Product name '{}' exists: {}", productName, exists);
                return exists;
            }
            
            return false;
            
        } catch (FeignErrorDecoder.FeignClientException e) {
            logger.error("Error checking product name existence: {}", e.getMessage());
            return false; // Assume doesn't exist on error to avoid blocking creation
        } catch (Exception e) {
            logger.error("Unexpected error checking product name existence: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Health check for query service
     */
    public boolean isQueryServiceHealthy() {
        try {
            ResponseEntity<Map<String, Object>> response = queryServiceClient.healthCheck();
            boolean healthy = response.getStatusCode().is2xxSuccessful();
            
            logger.debug("Query service health check: {}", healthy ? "UP" : "DOWN");
            return healthy;
            
        } catch (Exception e) {
            logger.warn("Query service health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Get current stock level from query service
     */
    public int getCurrentStockLevel(String productId) {
        logger.debug("Getting current stock level for product: {}", productId);
        
        try {
            Optional<ProductDto> productOpt = getProductInfo(productId);
            if (productOpt.isPresent()) {
                int stockLevel = productOpt.get().stockQuantity();
                logger.debug("Current stock level for product {}: {}", productId, stockLevel);
                return stockLevel;
            }
            
            logger.warn("Product not found when checking stock level: {}", productId);
            return 0;
            
        } catch (Exception e) {
            logger.error("Error getting current stock level: {}", e.getMessage(), e);
            return 0;
        }
    }
}