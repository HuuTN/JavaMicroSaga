package com.demo.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Event fired when inventory is reserved for an order.
 */
public class InventoryReservedEvent extends DomainEvent {

    private final String productId;
    private final String orderId;
    private final int quantity;
    private final int remainingStock;

    @JsonCreator
    public InventoryReservedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("userId") String userId,
            @JsonProperty("productId") String productId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("quantity") int quantity,
            @JsonProperty("remainingStock") int remainingStock) {
        
        super(new Builder()
                .eventId(eventId)
                .aggregateId(aggregateId)
                .aggregateType("Inventory")
                .version(version)
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(userId));
        
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
        this.remainingStock = remainingStock;
    }

    private InventoryReservedEvent(Builder builder) {
        super(builder);
        this.productId = builder.productId;
        this.orderId = builder.orderId;
        this.quantity = builder.quantity;
        this.remainingStock = builder.remainingStock;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getOrderId() { return orderId; }
    public int getQuantity() { return quantity; }
    public int getRemainingStock() { return remainingStock; }

    @Override
    public String getEventType() {
        return "InventoryReserved";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends DomainEvent.Builder<Builder> {
        private String productId;
        private String orderId;
        private int quantity;
        private int remainingStock;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder quantity(int quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder remainingStock(int remainingStock) {
            this.remainingStock = remainingStock;
            return this;
        }

        @Override
        public InventoryReservedEvent build() {
            return new InventoryReservedEvent(this);
        }
    }
}