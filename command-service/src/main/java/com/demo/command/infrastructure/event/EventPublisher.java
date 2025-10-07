package com.demo.command.infrastructure.event;

import com.demo.events.DomainEvent;

/**
 * Interface for publishing domain events.
 * This abstraction allows for different event publishing mechanisms.
 */
public interface EventPublisher {
    
    /**
     * Publish a domain event
     * @param event The domain event to publish
     */
    void publish(DomainEvent event);
    
    /**
     * Publish a domain event to a specific topic
     * @param event The domain event to publish
     * @param topic The specific topic to publish to
     */
    void publish(DomainEvent event, String topic);
}