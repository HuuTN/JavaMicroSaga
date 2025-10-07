package com.example.saga.orchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "payment-service", url = "${services.payment-service.url:http://localhost:8092}")
public interface PaymentServiceClient {
    
    @PostMapping("/api/payments")
    Map<String, Object> processPayment(@RequestBody Map<String, Object> paymentRequest);
    
    @PutMapping("/api/payments/{paymentId}/refund")
    Map<String, Object> refundPayment(@PathVariable String paymentId, 
                                     @RequestBody Map<String, Object> refundRequest);
    
    @GetMapping("/api/payments/{paymentId}")
    Map<String, Object> getPayment(@PathVariable String paymentId);
    
    @PutMapping("/api/payments/{paymentId}/authorize")
    Map<String, Object> authorizePayment(@PathVariable String paymentId);
    
    @PutMapping("/api/payments/{paymentId}/capture")
    Map<String, Object> capturePayment(@PathVariable String paymentId);
}