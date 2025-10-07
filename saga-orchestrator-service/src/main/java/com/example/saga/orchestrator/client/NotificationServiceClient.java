package com.example.saga.orchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "notification-service", url = "${services.notification-service.url:http://localhost:8094}")
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications/order-confirmation")
    Map<String, Object> sendOrderConfirmation(@RequestBody Map<String, Object> notificationRequest);
    
    @PostMapping("/api/notifications/payment-confirmation")
    Map<String, Object> sendPaymentConfirmation(@RequestBody Map<String, Object> notificationRequest);
    
    @PostMapping("/api/notifications/order-cancellation")
    Map<String, Object> sendOrderCancellation(@RequestBody Map<String, Object> notificationRequest);
    
    @PostMapping("/api/notifications/order-failure")
    Map<String, Object> sendOrderFailure(@RequestBody Map<String, Object> notificationRequest);
}