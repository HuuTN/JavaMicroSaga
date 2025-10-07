package com.example.payment.controller;

import com.example.payment.entity.Payment;
import com.example.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> processPayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            Payment payment = paymentService.processPayment(paymentRequest);
            Map<String, Object> response = Map.of(
                "paymentId", payment.getPaymentId(),
                "status", payment.getStatus().toString(),
                "amount", payment.getAmount(),
                "transactionId", payment.getTransactionId(),
                "processedAt", payment.getProcessedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to process payment: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable String paymentId) {
        try {
            Payment payment = paymentService.getPayment(paymentId);
            Map<String, Object> response = Map.of(
                "paymentId", payment.getPaymentId(),
                "customerId", payment.getCustomerId(),
                "orderId", payment.getOrderId(),
                "amount", payment.getAmount(),
                "status", payment.getStatus().toString(),
                "paymentMethod", payment.getPaymentMethod(),
                "transactionId", payment.getTransactionId(),
                "sagaId", payment.getSagaId(),
                "createdAt", payment.getCreatedAt(),
                "processedAt", payment.getProcessedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Payment not found: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{paymentId}/refund")
    public ResponseEntity<Map<String, Object>> refundPayment(@PathVariable String paymentId, 
                                                            @RequestBody Map<String, Object> refundRequest) {
        try {
            Payment payment = paymentService.refundPayment(paymentId, refundRequest);
            Map<String, Object> response = Map.of(
                "paymentId", payment.getPaymentId(),
                "status", payment.getStatus().toString(),
                "refundedAt", payment.getUpdatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to refund payment: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/saga/{sagaId}")
    public ResponseEntity<Map<String, Object>> getPaymentBySagaId(@PathVariable String sagaId) {
        try {
            Payment payment = paymentService.getPaymentBySagaId(sagaId);
            Map<String, Object> response = Map.of(
                "paymentId", payment.getPaymentId(),
                "status", payment.getStatus().toString(),
                "amount", payment.getAmount()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Payment not found for saga: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "payment-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}