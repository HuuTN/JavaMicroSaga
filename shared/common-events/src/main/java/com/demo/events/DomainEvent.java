package com.demo.events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * Provides common event metadata and serialization support.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "eventType"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = ProductCreatedEvent.class, name = "ProductCreated"),
    @JsonSubTypes.Type(value = ProductUpdatedEvent.class, name = "ProductUpdated"),
    @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "OrderCreated"),
    @JsonSubTypes.Type(value = OrderCompletedEvent.class, name = "OrderCompleted"),
    @JsonSubTypes.Type(value = InventoryReservedEvent.class, name = "InventoryReserved"),
    @JsonSubTypes.Type(value = PaymentProcessedEvent.class, name = "PaymentProcessed")
})
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DomainEvent {
    
    private final String eventId;
    private final String aggregateId;
    private final String aggregateType;
    private final long version;
    private final Instant timestamp;
    private final String correlationId;
    private final String causationId;
    private final String userId;

    protected DomainEvent(Builder<?> builder) {
        this.eventId = builder.eventId != null ? builder.eventId : UUID.randomUUID().toString();
        this.aggregateId = builder.aggregateId;
        this.aggregateType = builder.aggregateType;
        this.version = builder.version;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.correlationId = builder.correlationId;
        this.causationId = builder.causationId;
        this.userId = builder.userId;
    }

    // Getters
    public String getEventId() { return eventId; }
    public String getAggregateId() { return aggregateId; }
    public String getAggregateType() { return aggregateType; }
    public long getVersion() { return version; }
    public Instant getTimestamp() { return timestamp; }
    public String getCorrelationId() { return correlationId; }
    public String getCausationId() { return causationId; }
    public String getUserId() { return userId; }

    /**
     * Get the event type name for serialization
     */
    public abstract String getEventType();

    /**
     * Base builder for domain events
     */
    public abstract static class Builder<T extends Builder<T>> {
        private String eventId;
        private String aggregateId;
        private String aggregateType;
        private long version;
        private Instant timestamp;
        private String correlationId;
        private String causationId;
        private String userId;

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T eventId(String eventId) {
            this.eventId = eventId;
            return self();
        }

        public T aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return self();
        }

        public T aggregateType(String aggregateType) {
            this.aggregateType = aggregateType;
            return self();
        }

        public T version(long version) {
            this.version = version;
            return self();
        }

        public T timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return self();
        }

        public T correlationId(String correlationId) {
            this.correlationId = correlationId;
            return self();
        }

        public T causationId(String causationId) {
            this.causationId = causationId;
            return self();
        }

        public T userId(String userId) {
            this.userId = userId;
            return self();
        }

        public abstract DomainEvent build();
    }

    @Override
    public String toString() {
        return String.format("%s{eventId='%s', aggregateId='%s', version=%d, timestamp=%s}",
                getClass().getSimpleName(), eventId, aggregateId, version, timestamp);
    }
}