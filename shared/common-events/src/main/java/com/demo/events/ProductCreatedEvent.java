package com.demo.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Event fired when a new product is created in the system.
 */
public class ProductCreatedEvent extends DomainEvent {

    private final String productId;
    private final String name;
    private final String description;
    private final BigDecimal price;
    private final String category;
    private final int stockQuantity;

    @JsonCreator
    public ProductCreatedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("userId") String userId,
            @JsonProperty("productId") String productId,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("category") String category,
            @JsonProperty("stockQuantity") int stockQuantity) {
        
        super(new Builder()
                .eventId(eventId)
                .aggregateId(aggregateId)
                .aggregateType("Product")
                .version(version)
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(userId));
        
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
    }

    private ProductCreatedEvent(Builder builder) {
        super(builder);
        this.productId = builder.productId;
        this.name = builder.name;
        this.description = builder.description;
        this.price = builder.price;
        this.category = builder.category;
        this.stockQuantity = builder.stockQuantity;
    }

    // Getters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }
    public int getStockQuantity() { return stockQuantity; }

    @Override
    public String getEventType() {
        return "ProductCreated";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends DomainEvent.Builder<Builder> {
        private String productId;
        private String name;
        private String description;
        private BigDecimal price;
        private String category;
        private int stockQuantity;

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder stockQuantity(int stockQuantity) {
            this.stockQuantity = stockQuantity;
            return this;
        }

        @Override
        public ProductCreatedEvent build() {
            return new ProductCreatedEvent(this);
        }
    }
}