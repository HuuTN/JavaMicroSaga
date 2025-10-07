package com.example.notification.controller;

import com.example.notification.entity.Notification;
import com.example.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;

    @PostMapping("/order-confirmation")
    public ResponseEntity<Map<String, Object>> sendOrderConfirmation(@RequestBody Map<String, Object> request) {
        try {
            Notification notification = notificationService.sendOrderConfirmation(request);
            Map<String, Object> response = Map.of(
                "notificationId", notification.getNotificationId(),
                "type", notification.getType().toString(),
                "status", notification.getStatus().toString(),
                "channel", notification.getChannel().toString(),
                "sentAt", notification.getSentAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to send order confirmation: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/payment-confirmation")
    public ResponseEntity<Map<String, Object>> sendPaymentConfirmation(@RequestBody Map<String, Object> request) {
        try {
            Notification notification = notificationService.sendPaymentConfirmation(request);
            Map<String, Object> response = Map.of(
                "notificationId", notification.getNotificationId(),
                "type", notification.getType().toString(),
                "status", notification.getStatus().toString(),
                "channel", notification.getChannel().toString(),
                "sentAt", notification.getSentAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to send payment confirmation: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/order-cancellation")
    public ResponseEntity<Map<String, Object>> sendOrderCancellation(@RequestBody Map<String, Object> request) {
        try {
            Notification notification = notificationService.sendOrderCancellation(request);
            Map<String, Object> response = Map.of(
                "notificationId", notification.getNotificationId(),
                "type", notification.getType().toString(),
                "status", notification.getStatus().toString(),
                "channel", notification.getChannel().toString(),
                "sentAt", notification.getSentAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to send order cancellation: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{notificationId}")
    public ResponseEntity<Map<String, Object>> getNotification(@PathVariable String notificationId) {
        try {
            Notification notification = notificationService.getNotification(notificationId);
            Map<String, Object> response = Map.of(
                "notificationId", notification.getNotificationId(),
                "customerId", notification.getCustomerId(),
                "type", notification.getType().toString(),
                "subject", notification.getSubject(),
                "message", notification.getMessage(),
                "channel", notification.getChannel().toString(),
                "status", notification.getStatus().toString(),
                "sentAt", notification.getSentAt(),
                "createdAt", notification.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Notification not found: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "notification-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}