package com.example.notification.service;

import com.example.notification.entity.Notification;
import com.example.notification.entity.NotificationTemplate;
import com.example.notification.repository.NotificationRepository;
import com.example.notification.repository.NotificationTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.notification.outbox.OutboxEvent;
import com.example.notification.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private NotificationTemplateRepository templateRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Notification sendOrderConfirmation(Map<String, Object> request) {
        try {
            String customerId = (String) request.get("customerId");
            String sagaId = (String) request.get("sagaId");
            
            // Get template
            NotificationTemplate template = templateRepository
                    .findByTypeAndChannel(Notification.NotificationType.ORDER_CONFIRMATION, 
                                        Notification.NotificationChannel.EMAIL)
                    .orElseThrow(() -> new RuntimeException("Order confirmation template not found"));
            
            // Prepare variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerId", customerId);
            variables.put("sagaId", sagaId);
            
            // Process template
            String subject = template.processSubject(variables);
            String message = template.processMessage(variables);
            
            // Create notification
            String notificationId = UUID.randomUUID().toString();
            Notification notification = new Notification(notificationId, customerId, 
                    Notification.NotificationType.ORDER_CONFIRMATION, subject, message,
                    Notification.NotificationChannel.EMAIL);
            notification.setSagaId(sagaId);
            notification.setRecipientEmail(customerId + "@example.com"); // Mock email
            
            notification = notificationRepository.save(notification);
            
            // Send notification (simulate)
            sendNotification(notification);
            
            // Publish event
            publishNotificationEvent("NOTIFICATION_SENT", notification);
            
            logger.info("Order confirmation sent to customer {}", customerId);
            return notification;
            
        } catch (Exception e) {
            logger.error("Failed to send order confirmation", e);
            throw new RuntimeException("Failed to send order confirmation", e);
        }
    }

    public Notification sendPaymentConfirmation(Map<String, Object> request) {
        try {
            String customerId = (String) request.get("customerId");
            String sagaId = (String) request.get("sagaId");
            
            // Get template
            NotificationTemplate template = templateRepository
                    .findByTypeAndChannel(Notification.NotificationType.PAYMENT_CONFIRMATION, 
                                        Notification.NotificationChannel.EMAIL)
                    .orElseThrow(() -> new RuntimeException("Payment confirmation template not found"));
            
            // Prepare variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerId", customerId);
            variables.put("sagaId", sagaId);
            
            // Process template
            String subject = template.processSubject(variables);
            String message = template.processMessage(variables);
            
            // Create notification
            String notificationId = UUID.randomUUID().toString();
            Notification notification = new Notification(notificationId, customerId, 
                    Notification.NotificationType.PAYMENT_CONFIRMATION, subject, message,
                    Notification.NotificationChannel.EMAIL);
            notification.setSagaId(sagaId);
            notification.setRecipientEmail(customerId + "@example.com"); // Mock email
            
            notification = notificationRepository.save(notification);
            
            // Send notification (simulate)
            sendNotification(notification);
            
            // Publish event
            publishNotificationEvent("NOTIFICATION_SENT", notification);
            
            logger.info("Payment confirmation sent to customer {}", customerId);
            return notification;
            
        } catch (Exception e) {
            logger.error("Failed to send payment confirmation", e);
            throw new RuntimeException("Failed to send payment confirmation", e);
        }
    }

    public Notification sendOrderCancellation(Map<String, Object> request) {
        try {
            String customerId = (String) request.get("customerId");
            String sagaId = (String) request.get("sagaId");
            
            // Get template
            NotificationTemplate template = templateRepository
                    .findByTypeAndChannel(Notification.NotificationType.ORDER_CANCELLATION, 
                                        Notification.NotificationChannel.EMAIL)
                    .orElseThrow(() -> new RuntimeException("Order cancellation template not found"));
            
            // Prepare variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("customerId", customerId);
            variables.put("sagaId", sagaId);
            
            // Process template
            String subject = template.processSubject(variables);
            String message = template.processMessage(variables);
            
            // Create notification
            String notificationId = UUID.randomUUID().toString();
            Notification notification = new Notification(notificationId, customerId, 
                    Notification.NotificationType.ORDER_CANCELLATION, subject, message,
                    Notification.NotificationChannel.EMAIL);
            notification.setSagaId(sagaId);
            notification.setRecipientEmail(customerId + "@example.com"); // Mock email
            
            notification = notificationRepository.save(notification);
            
            // Send notification (simulate)
            sendNotification(notification);
            
            // Publish event
            publishNotificationEvent("NOTIFICATION_SENT", notification);
            
            logger.info("Order cancellation notification sent to customer {}", customerId);
            return notification;
            
        } catch (Exception e) {
            logger.error("Failed to send order cancellation notification", e);
            throw new RuntimeException("Failed to send order cancellation notification", e);
        }
    }

    private void sendNotification(Notification notification) {
        try {
            // Simulate sending notification
            Thread.sleep(100); // Simulate processing time
            
            notification.markSent();
            notificationRepository.save(notification);
            
            logger.info("Notification {} sent successfully", notification.getNotificationId());
            
        } catch (Exception e) {
            logger.error("Failed to send notification {}", notification.getNotificationId(), e);
            notification.markFailed(e.getMessage());
            notificationRepository.save(notification);
        }
    }

    private void publishNotificationEvent(String eventType, Notification notification) {
        try {
            Map<String, Object> event = Map.of(
                "eventType", eventType,
                "notificationId", notification.getNotificationId(),
                "customerId", notification.getCustomerId(),
                "type", notification.getType().toString(),
                "channel", notification.getChannel().toString(),
                "status", notification.getStatus().toString(),
                "sagaId", notification.getSagaId(),
                "timestamp", System.currentTimeMillis()
            );
            
            try {
                String payload = objectMapper.writeValueAsString(event);
                OutboxEvent outbox = new OutboxEvent();
                outbox.setAggregateType("Notification");
                outbox.setEventType(eventType);
                outbox.setAggregateId(notification.getNotificationId());
                outbox.setPayload(payload);
                outboxRepository.save(outbox);
                logger.info("Wrote outbox notification event for {}", notification.getNotificationId());
            } catch (Exception e) {
                logger.error("Failed to write notification outbox event", e);
            }
            
        } catch (Exception e) {
            logger.error("Failed to publish notification event", e);
        }
    }

    public Notification getNotification(String notificationId) {
        return notificationRepository.findByNotificationId(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
    }
}