package com.demo.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event fired when an order is completed.
 */
public class OrderCompletedEvent extends DomainEvent {

    private final String orderId;
    private final String customerId;
    private final BigDecimal totalAmount;
    private final Instant completedAt;

    @JsonCreator
    public OrderCompletedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("userId") String userId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("completedAt") Instant completedAt) {
        
        super(new Builder()
                .eventId(eventId)
                .aggregateId(aggregateId)
                .aggregateType("Order")
                .version(version)
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(userId));
        
        this.orderId = orderId;
        this.customerId = customerId;
        this.totalAmount = totalAmount;
        this.completedAt = completedAt;
    }

    private OrderCompletedEvent(Builder builder) {
        super(builder);
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.totalAmount = builder.totalAmount;
        this.completedAt = builder.completedAt;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCompletedAt() { return completedAt; }

    @Override
    public String getEventType() {
        return "OrderCompleted";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends DomainEvent.Builder<Builder> {
        private String orderId;
        private String customerId;
        private BigDecimal totalAmount;
        private Instant completedAt;

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder completedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        @Override
        public OrderCompletedEvent build() {
            return new OrderCompletedEvent(this);
        }
    }
}