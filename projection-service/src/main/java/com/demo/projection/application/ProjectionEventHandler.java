package com.demo.projection.application;

import com.demo.projection.application.ProjectionService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Kafka event handler for projection updates.
 */
@Component
public class ProjectionEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProjectionEventHandler.class);
    
    private final ProjectionService projectionService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProjectionEventHandler(ProjectionService projectionService, ObjectMapper objectMapper) {
        this.projectionService = projectionService;
        this.objectMapper = objectMapper;
    }

    /**
     * Handle product events from command service
     */
    @KafkaListener(topics = "product-events", groupId = "projection-service-group")
    @Transactional
    public void handleProductEvent(String eventMessage) {
        try {
            logger.info("Received product event: {}", eventMessage);
            
            JsonNode eventNode = objectMapper.readTree(eventMessage);
            String eventType = eventNode.get("eventType").asText();
            JsonNode eventData = eventNode.get("eventData");
            
            switch (eventType) {
                case "ProductCreated":
                case "ProductUpdated":
                    handleProductCreatedOrUpdated(eventData);
                    break;
                case "ProductViewed":
                    handleProductViewed(eventData);
                    break;
                case "ProductOrdered":
                    handleProductOrdered(eventData);
                    break;
                case "ProductRated":
                    handleProductRated(eventData);
                    break;
                default:
                    logger.warn("Unknown event type: {}", eventType);
            }
            
        } catch (Exception e) {
            logger.error("Error processing product event: {}", eventMessage, e);
        }
    }

    /**
     * Handle order events from other services
     */
    @KafkaListener(topics = "order-events", groupId = "projection-service-group")
    @Transactional
    public void handleOrderEvent(String eventMessage) {
        try {
            logger.info("Received order event: {}", eventMessage);
            
            JsonNode eventNode = objectMapper.readTree(eventMessage);
            String eventType = eventNode.get("eventType").asText();
            JsonNode eventData = eventNode.get("eventData");
            
            if ("OrderCreated".equals(eventType) || "OrderCompleted".equals(eventType)) {
                handleOrderCompleted(eventData);
            }
            
        } catch (Exception e) {
            logger.error("Error processing order event: {}", eventMessage, e);
        }
    }

    private void handleProductCreatedOrUpdated(JsonNode eventData) {
        try {
            String productId = eventData.get("id").asText();
            String name = eventData.get("name").asText();
            String description = eventData.get("description").asText();
            BigDecimal price = new BigDecimal(eventData.get("price").asText());
            String category = eventData.get("category").asText();
            
            projectionService.createOrUpdateProductProjection(productId, name, description, price, category);
            logger.info("Updated projection for product: {}", productId);
            
        } catch (Exception e) {
            logger.error("Error handling product created/updated event", e);
        }
    }

    private void handleProductViewed(JsonNode eventData) {
        try {
            String productId = eventData.get("productId").asText();
            projectionService.handleProductView(productId);
            logger.info("Updated view count for product: {}", productId);
            
        } catch (Exception e) {
            logger.error("Error handling product viewed event", e);
        }
    }

    private void handleProductOrdered(JsonNode eventData) {
        try {
            String productId = eventData.get("productId").asText();
            Integer quantity = eventData.get("quantity").asInt();
            BigDecimal orderTotal = new BigDecimal(eventData.get("totalPrice").asText());
            
            projectionService.handleProductOrder(productId, quantity, orderTotal);
            logger.info("Updated order statistics for product: {}", productId);
            
        } catch (Exception e) {
            logger.error("Error handling product ordered event", e);
        }
    }

    private void handleProductRated(JsonNode eventData) {
        try {
            String productId = eventData.get("productId").asText();
            Double rating = eventData.get("rating").asDouble();
            
            projectionService.handleProductRating(productId, rating);
            logger.info("Updated rating for product: {}", productId);
            
        } catch (Exception e) {
            logger.error("Error handling product rated event", e);
        }
    }

    private void handleOrderCompleted(JsonNode eventData) {
        try {
            JsonNode orderItems = eventData.get("orderItems");
            if (orderItems != null && orderItems.isArray()) {
                for (JsonNode item : orderItems) {
                    String productId = item.get("productId").asText();
                    Integer quantity = item.get("quantity").asInt();
                    BigDecimal totalPrice = new BigDecimal(item.get("totalPrice").asText());
                    
                    projectionService.handleProductOrder(productId, quantity, totalPrice);
                }
            }
            logger.info("Updated projections for order completion");
            
        } catch (Exception e) {
            logger.error("Error handling order completed event", e);
        }
    }
}