package com.demo.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

/**
 * Event fired when a new order is created.
 */
public class OrderCreatedEvent extends DomainEvent {

    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final String status;

    @JsonCreator
    public OrderCreatedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("userId") String userId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("customerId") String customerId,
            @JsonProperty("items") List<OrderItem> items,
            @JsonProperty("totalAmount") BigDecimal totalAmount,
            @JsonProperty("status") String status) {
        
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
        this.items = items;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    private OrderCreatedEvent(Builder builder) {
        super(builder);
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.items = builder.items;
        this.totalAmount = builder.totalAmount;
        this.status = builder.status;
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }

    @Override
    public String getEventType() {
        return "OrderCreated";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends DomainEvent.Builder<Builder> {
        private String orderId;
        private String customerId;
        private List<OrderItem> items;
        private BigDecimal totalAmount;
        private String status;

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder items(List<OrderItem> items) {
            this.items = items;
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        @Override
        public OrderCreatedEvent build() {
            return new OrderCreatedEvent(this);
        }
    }

    /**
     * Represents an item in an order
     */
    public static class OrderItem {
        private final String productId;
        private final String productName;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal totalPrice;

        @JsonCreator
        public OrderItem(
                @JsonProperty("productId") String productId,
                @JsonProperty("productName") String productName,
                @JsonProperty("quantity") int quantity,
                @JsonProperty("unitPrice") BigDecimal unitPrice,
                @JsonProperty("totalPrice") BigDecimal totalPrice) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public BigDecimal getTotalPrice() { return totalPrice; }
    }
}