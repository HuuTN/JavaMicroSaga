package com.demo.command.infrastructure.event;

import com.demo.events.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka-based implementation of EventPublisher.
 * Publishes domain events to Kafka topics for event-driven architecture.
 */
@Component
public class KafkaEventPublisher implements EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);

    @Value("${app.kafka.topics.domain-events:domain-events}")
    private String defaultTopic;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publish(DomainEvent event) {
        publish(event, defaultTopic);
    }

    @Override
    public void publish(DomainEvent event, String topic) {
        try {
            logger.debug("Publishing event {} to topic {}", event.getEventType(), topic);
            
            // Use aggregate ID as the partition key to ensure ordering
            String partitionKey = event.getAggregateId();
            
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(topic, partitionKey, event);
            
            future.whenComplete((result, throwable) -> {
                if (throwable == null) {
                    logger.info("Successfully published event {} with key {} to topic {} at offset {}",
                        event.getEventType(), 
                        partitionKey, 
                        topic, 
                        result.getRecordMetadata().offset());
                } else {
                    logger.error("Failed to publish event {} with key {} to topic {}: {}", 
                        event.getEventType(), 
                        partitionKey, 
                        topic, 
                        throwable.getMessage(), 
                        throwable);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing event {} to topic {}: {}", 
                event.getEventType(), topic, e.getMessage(), e);
            throw new EventPublishingException("Failed to publish event: " + event.getEventType(), e);
        }
    }

    /**
     * Publish event synchronously (for testing or critical operations)
     */
    public void publishSync(DomainEvent event, String topic) {
        try {
            logger.debug("Publishing event {} synchronously to topic {}", event.getEventType(), topic);
            
            String partitionKey = event.getAggregateId();
            SendResult<String, Object> result = kafkaTemplate.send(topic, partitionKey, event).get();
            
            logger.info("Successfully published event {} synchronously with key {} to topic {} at offset {}",
                event.getEventType(), 
                partitionKey, 
                topic, 
                result.getRecordMetadata().offset());
            
        } catch (Exception e) {
            logger.error("Error publishing event {} synchronously to topic {}: {}", 
                event.getEventType(), topic, e.getMessage(), e);
            throw new EventPublishingException("Failed to publish event synchronously: " + event.getEventType(), e);
        }
    }

    /**
     * Custom exception for event publishing failures
     */
    public static class EventPublishingException extends RuntimeException {
        public EventPublishingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}