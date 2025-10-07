package com.demo.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Event fired when a payment is processed successfully.
 */
public class PaymentProcessedEvent extends DomainEvent {

    private final String paymentId;
    private final String orderId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final String transactionId;

    @JsonCreator
    public PaymentProcessedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("version") long version,
            @JsonProperty("correlationId") String correlationId,
            @JsonProperty("causationId") String causationId,
            @JsonProperty("userId") String userId,
            @JsonProperty("paymentId") String paymentId,
            @JsonProperty("orderId") String orderId,
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("paymentMethod") String paymentMethod,
            @JsonProperty("transactionId") String transactionId) {
        
        super(new Builder()
                .eventId(eventId)
                .aggregateId(aggregateId)
                .aggregateType("Payment")
                .version(version)
                .correlationId(correlationId)
                .causationId(causationId)
                .userId(userId));
        
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
    }

    private PaymentProcessedEvent(Builder builder) {
        super(builder);
        this.paymentId = builder.paymentId;
        this.orderId = builder.orderId;
        this.amount = builder.amount;
        this.paymentMethod = builder.paymentMethod;
        this.transactionId = builder.transactionId;
    }

    // Getters
    public String getPaymentId() { return paymentId; }
    public String getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionId() { return transactionId; }

    @Override
    public String getEventType() {
        return "PaymentProcessed";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends DomainEvent.Builder<Builder> {
        private String paymentId;
        private String orderId;
        private BigDecimal amount;
        private String paymentMethod;
        private String transactionId;

        public Builder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        @Override
        public PaymentProcessedEvent build() {
            return new PaymentProcessedEvent(this);
        }
    }
}