package com.demo.command.application.service;

import com.demo.command.application.command.CreateProductCommand;
import com.demo.command.application.command.UpdateProductCommand;
import com.demo.command.domain.product.Product;
import com.demo.command.domain.product.ProductRepository;
import com.demo.command.infrastructure.event.EventPublisher;
import com.demo.events.ProductCreatedEvent;
import com.demo.events.ProductUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application service for handling product commands.
 * Implements the command side of CQRS pattern.
 */
@Service
@Transactional
public class ProductCommandService {

    private static final Logger logger = LoggerFactory.getLogger(ProductCommandService.class);

    private final ProductRepository productRepository;
    private final EventPublisher eventPublisher;

    @Autowired
    public ProductCommandService(ProductRepository productRepository, EventPublisher eventPublisher) {
        this.productRepository = productRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Handle create product command
     */
    public String createProduct(CreateProductCommand command) {
        logger.info("Creating product with command: {}", command);
        
        // Validate command
        command.validate();
        
        // Check if product with same name already exists
        if (productRepository.existsByNameIgnoreCase(command.name())) {
            throw new IllegalArgumentException("Product with name '" + command.name() + "' already exists");
        }

        // Create new product
        Product product = new Product(
            command.name(),
            command.description(),
            command.price(),
            command.category(),
            command.stockQuantity(),
            command.userId()
        );

        // Save product
        Product savedProduct = productRepository.save(product);
        
        // Create and publish event
        ProductCreatedEvent event = ProductCreatedEvent.builder()
            .aggregateId(savedProduct.getId())
            .version(savedProduct.getVersion())
            .correlationId(UUID.randomUUID().toString())
            .userId(command.userId())
            .productId(savedProduct.getId())
            .name(savedProduct.getName())
            .description(savedProduct.getDescription())
            .price(savedProduct.getPrice())
            .category(savedProduct.getCategory())
            .stockQuantity(savedProduct.getStockQuantity())
            .build();

        eventPublisher.publish(event);
        
        logger.info("Product created successfully with ID: {}", savedProduct.getId());
        return savedProduct.getId();
    }

    /**
     * Handle update product command
     */
    public void updateProduct(UpdateProductCommand command) {
        logger.info("Updating product with command: {}", command);
        
        // Validate command
        command.validate();
        
        if (!command.hasUpdates()) {
            logger.warn("No updates provided for product ID: {}", command.productId());
            return;
        }

        // Find existing product
        Product product = productRepository.findById(command.productId())
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + command.productId()));

        // Store original values for comparison
        long originalVersion = product.getVersion();

        // Update product
        product.updateProduct(
            command.name(),
            command.description(),
            command.price(),
            command.category(),
            command.userId()
        );

        if (command.stockQuantity() != null) {
            product.updateStock(command.stockQuantity(), command.userId());
        }

        // Save updated product
        Product updatedProduct = productRepository.save(product);
        
        // Create and publish event
        ProductUpdatedEvent event = ProductUpdatedEvent.builder()
            .aggregateId(updatedProduct.getId())
            .version(updatedProduct.getVersion())
            .correlationId(UUID.randomUUID().toString())
            .causationId("ProductUpdateCommand")
            .userId(command.userId())
            .productId(updatedProduct.getId())
            .name(updatedProduct.getName())
            .description(updatedProduct.getDescription())
            .price(updatedProduct.getPrice())
            .category(updatedProduct.getCategory())
            .stockQuantity(updatedProduct.getStockQuantity())
            .build();

        eventPublisher.publish(event);
        
        logger.info("Product updated successfully with ID: {}, version: {} -> {}", 
                   updatedProduct.getId(), originalVersion, updatedProduct.getVersion());
    }

    /**
     * Reserve stock for a product
     */
    public void reserveStock(String productId, int quantity, String userId) {
        logger.info("Reserving {} units of product ID: {} for user: {}", quantity, productId, userId);
        
        Product product = productRepository.findByIdForUpdate(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        // Reserve stock (this will throw exception if insufficient stock)
        product.reserveStock(quantity);
        
        // Save updated product
        productRepository.save(product);
        
        logger.info("Stock reserved successfully for product ID: {}, remaining stock: {}", 
                   productId, product.getStockQuantity());
    }

    /**
     * Release reserved stock
     */
    public void releaseStock(String productId, int quantity, String userId) {
        logger.info("Releasing {} units of product ID: {} for user: {}", quantity, productId, userId);
        
        Product product = productRepository.findByIdForUpdate(productId)
            .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));

        // Release stock
        product.releaseStock(quantity);
        
        // Save updated product
        productRepository.save(product);
        
        logger.info("Stock released successfully for product ID: {}, current stock: {}", 
                   productId, product.getStockQuantity());
    }
}