package com.example.notification.repository;

import com.example.notification.entity.Notification;
import com.example.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    
    Optional<NotificationTemplate> findByTemplateId(String templateId);
    
    Optional<NotificationTemplate> findByTypeAndChannel(Notification.NotificationType type, 
                                                       Notification.NotificationChannel channel);
}