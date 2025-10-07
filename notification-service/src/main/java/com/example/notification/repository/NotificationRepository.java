package com.example.notification.repository;

import com.example.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    Optional<Notification> findByNotificationId(String notificationId);
    
    List<Notification> findByCustomerIdOrderByCreatedAtDesc(String customerId);
    
    List<Notification> findByOrderId(String orderId);
    
    List<Notification> findBySagaId(String sagaId);
    
    List<Notification> findByStatus(Notification.NotificationStatus status);
}