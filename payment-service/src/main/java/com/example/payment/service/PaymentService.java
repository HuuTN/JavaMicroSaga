package com.example.payment.service;

import com.example.payment.entity.Payment;
import com.example.payment.entity.PaymentTransaction;
import com.example.payment.repository.PaymentRepository;
import com.example.payment.repository.PaymentTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.payment.outbox.OutboxEvent;
import com.example.payment.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private PaymentTransactionRepository transactionRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    
    @Autowired
    private OutboxRepository outboxRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    public Payment processPayment(Map<String, Object> request) {
        try {
            String paymentId = UUID.randomUUID().toString();
            String customerId = (String) request.get("customerId");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            String sagaId = (String) request.get("sagaId");
            
            Payment payment = new Payment(paymentId, customerId, amount, paymentMethod, sagaId);
            payment = paymentRepository.save(payment);
            
            // Create payment transaction
            String transactionId = UUID.randomUUID().toString();
            PaymentTransaction transaction = new PaymentTransaction(
                transactionId, payment, PaymentTransaction.TransactionType.PAYMENT, amount);
            transactionRepository.save(transaction);
            
            // Simulate payment processing
            payment.process();
            payment = paymentRepository.save(payment);
            
            // Simulate successful payment (in real scenario, this would call payment gateway)
            Thread.sleep(100); // Simulate processing time
            
            payment.complete(transactionId);
            payment = paymentRepository.save(payment);
            
            transaction.complete("Payment processed successfully");
            transactionRepository.save(transaction);
            
            // Publish payment event
            try {
                Map<String, Object> event = Map.of(
                    "eventType", "PAYMENT_PROCESSED",
                    "paymentId", payment.getPaymentId(),
                    "customerId", payment.getCustomerId(),
                    "orderId", payment.getOrderId(),
                    "amount", payment.getAmount(),
                    "status", payment.getStatus().toString(),
                    "paymentMethod", payment.getPaymentMethod(),
                    "sagaId", payment.getSagaId(),
                    "timestamp", System.currentTimeMillis()
                );
                
                String payload = objectMapper.writeValueAsString(event);
                OutboxEvent outbox = new OutboxEvent();
                outbox.setAggregateType("Payment");
                outbox.setEventType("PaymentProcessedEvent");
                outbox.setAggregateId(payment.getPaymentId());
                outbox.setPayload(payload);
                outboxRepository.save(outbox);
                logger.info("Wrote outbox payment event for {}", payment.getPaymentId());
            } catch (Exception ex) {
                logger.error("Failed to write payment outbox event", ex);
            }
            
            logger.info("Payment processed successfully: {}", paymentId);
            return payment;
            
        } catch (Exception e) {
            logger.error("Failed to process payment", e);
            throw new RuntimeException("Failed to process payment", e);
        }
    }

    public Payment refundPayment(String paymentId, Map<String, Object> refundRequest) {
        try {
            Payment payment = getPayment(paymentId);
            
            if (!payment.isCompleted()) {
                throw new RuntimeException("Cannot refund payment that is not completed");
            }
            
            // Create refund transaction
            String refundTransactionId = UUID.randomUUID().toString();
            PaymentTransaction refundTransaction = new PaymentTransaction(
                refundTransactionId, payment, PaymentTransaction.TransactionType.REFUND, payment.getAmount());
            transactionRepository.save(refundTransaction);
            
            // Simulate refund processing
            Thread.sleep(100); // Simulate processing time
            
            payment.refund();
            payment = paymentRepository.save(payment);
            
            refundTransaction.complete("Refund processed successfully");
            transactionRepository.save(refundTransaction);
            
            // Publish refund event
            publishPaymentEvent("PAYMENT_REFUNDED", payment);
            
            logger.info("Payment refunded successfully: {}", paymentId);
            return payment;
            
        } catch (Exception e) {
            logger.error("Failed to refund payment: {}", paymentId, e);
            throw new RuntimeException("Failed to refund payment", e);
        }
    }

    public Payment getPayment(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
    }

    public Payment getPaymentBySagaId(String sagaId) {
        return paymentRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Payment not found for saga: " + sagaId));
    }

    private void publishPaymentEvent(String eventType, Payment payment) {
        try {
            Map<String, Object> event = Map.of(
                "eventType", eventType,
                "paymentId", payment.getPaymentId(),
                "customerId", payment.getCustomerId(),
                "orderId", payment.getOrderId(),
                "amount", payment.getAmount(),
                "status", payment.getStatus().toString(),
                "paymentMethod", payment.getPaymentMethod(),
                "sagaId", payment.getSagaId(),
                "timestamp", System.currentTimeMillis()
            );
            
            try {
                String payload = objectMapper.writeValueAsString(event);
                OutboxEvent outbox = new OutboxEvent();
                outbox.setAggregateType("Payment");
                outbox.setEventType(eventType);
                outbox.setAggregateId(payment.getPaymentId());
                outbox.setPayload(payload);
                outboxRepository.save(outbox);
                logger.info("Wrote outbox payment event for {}", payment.getPaymentId());
            } catch (Exception ex) {
                logger.error("Failed to write payment outbox event", ex);
            }
            
        } catch (Exception e) {
            logger.error("Failed to publish payment event", e);
        }
    }
}