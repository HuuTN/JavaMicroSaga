package com.demo.query.infrastructure.event;

import com.demo.events.ProductCreatedEvent;
import com.demo.events.ProductUpdatedEvent;
import com.demo.query.domain.ProductView;
import com.demo.query.domain.ProductViewRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Kafka Event Handler for Query Service.
 * Consumes domain events from Kafka and updates the read model (query database).
 */
@Component
public class ProductEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProductEventHandler.class);

    private final ProductViewRepository productViewRepository;
    private final ObjectMapper objectMapper;

    public ProductEventHandler(ProductViewRepository productViewRepository, ObjectMapper objectMapper) {
        this.productViewRepository = productViewRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Universal event handler for all domain events
     */
    @KafkaListener(
        topics = "${app.kafka.topics.domain-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void handleDomainEvent(
            @Payload Map<String, Object> event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        try {
            logger.info("=== CONSUMING DOMAIN EVENT ===");
            logger.info("Event Map: {}", event);
            logger.info("Topic: {}, Partition: {}, Offset: {}", topic, partition, offset);

            String eventType = determineEventType(event);
            logger.info("Determined Event Type: {}", eventType);

            if ("ProductCreatedEvent".equals(eventType)) {
                ProductCreatedEvent createdEvent = objectMapper.convertValue(event, ProductCreatedEvent.class);
                handleProductCreated(createdEvent, acknowledgment);
            } else if ("ProductUpdatedEvent".equals(eventType)) {
                ProductUpdatedEvent updatedEvent = objectMapper.convertValue(event, ProductUpdatedEvent.class);
                handleProductUpdated(updatedEvent, acknowledgment);
            } else {
                logger.warn("Unknown event type: {}", eventType);
                acknowledgment.acknowledge(); // Skip unknown events
            }
            
        } catch (Exception e) {
            logger.error("❌ Error handling domain event: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handleProductCreated(ProductCreatedEvent event, Acknowledgment acknowledgment) {
        logger.info("=== PROCESSING PRODUCT CREATED EVENT ===");
        logger.info("Event ID: {}", event.getEventId());
        logger.info("Product ID: {}", event.getProductId());
        logger.info("Product Name: {}", event.getName());

        // Check if product view already exists (idempotency)
        if (productViewRepository.existsById(event.getProductId())) {
            logger.warn("Product view already exists for ID: {}", event.getProductId());
            acknowledgment.acknowledge();
            return;
        }

        // Create new ProductView from event
        LocalDateTime now = LocalDateTime.now();
        ProductView productView = new ProductView(
            event.getProductId(),
            event.getName(),
            event.getDescription(),
            event.getPrice(),
            event.getCategory(),
            event.getStockQuantity(),
            event.getVersion(),
            now,
            now,
            event.getEventId(),
            event.getVersion()
        );

        // Save to query database
        ProductView saved = productViewRepository.save(productView);
        logger.info("✅ Product view created successfully: {}", saved.getId());

        // Acknowledge the message
        acknowledgment.acknowledge();
    }

    private void handleProductUpdated(ProductUpdatedEvent event, Acknowledgment acknowledgment) {
        logger.info("=== PROCESSING PRODUCT UPDATED EVENT ===");
        logger.info("Event ID: {}", event.getEventId());
        logger.info("Product ID: {}", event.getProductId());

        // Find existing product view
        ProductView productView = productViewRepository.findById(event.getProductId())
            .orElse(null);

        if (productView == null) {
            logger.warn("Product view not found for ID: {}", event.getProductId());
            acknowledgment.acknowledge();
            return;
        }

        // Update product view from event
        productView.updateFromEvent(
            event.getName(),
            event.getDescription(),
            event.getPrice(),
            event.getCategory(),
            event.getStockQuantity(),
            event.getVersion(),
            event.getEventId(),
            event.getVersion()
        );

        // Save updated view
        ProductView saved = productViewRepository.save(productView);
        logger.info("✅ Product view updated successfully: {}", saved.getId());

        // Acknowledge the message
        acknowledgment.acknowledge();
    }

    /**
     * Determine event type from event data
     */
    private String determineEventType(Map<String, Object> eventData) {
        // Check if event has eventType field
        if (eventData.containsKey("eventType")) {
            String eventType = (String) eventData.get("eventType");
            if ("ProductCreated".equals(eventType)) {
                return "ProductCreatedEvent";
            } else if ("ProductUpdated".equals(eventType)) {
                return "ProductUpdatedEvent";
            }
        }
        
        // Fallback: Try to determine event type by checking properties
        if (eventData.containsKey("name") && eventData.containsKey("description") && 
            eventData.containsKey("price") && eventData.containsKey("category") && 
            eventData.containsKey("stockQuantity") && eventData.containsKey("eventId")) {
            
            // Check if it's an update (might have additional fields or specific patterns)
            // For simplicity, assume it's a create event if basic product data is present
            return "ProductCreatedEvent";
        }
        
        return "Unknown";
    }


}