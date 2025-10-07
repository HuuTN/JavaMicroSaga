package com.example.notification.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "notification_templates")
public class NotificationTemplate {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_id", unique = true, nullable = false)
    private String templateId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Notification.NotificationType type;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "subject_template", nullable = false)
    private String subjectTemplate;
    
    @Column(name = "message_template", columnDefinition = "TEXT", nullable = false)
    private String messageTemplate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false)
    private Notification.NotificationChannel channel;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructors
    public NotificationTemplate() {
        this.createdAt = LocalDateTime.now();
    }

    public NotificationTemplate(String templateId, Notification.NotificationType type, 
                               String name, String subjectTemplate, String messageTemplate,
                               Notification.NotificationChannel channel) {
        this();
        this.templateId = templateId;
        this.type = type;
        this.name = name;
        this.subjectTemplate = subjectTemplate;
        this.messageTemplate = messageTemplate;
        this.channel = channel;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public Notification.NotificationType getType() {
        return type;
    }

    public void setType(Notification.NotificationType type) {
        this.type = type;
        this.updatedAt = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = LocalDateTime.now();
    }

    public String getSubjectTemplate() {
        return subjectTemplate;
    }

    public void setSubjectTemplate(String subjectTemplate) {
        this.subjectTemplate = subjectTemplate;
        this.updatedAt = LocalDateTime.now();
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
        this.updatedAt = LocalDateTime.now();
    }

    public Notification.NotificationChannel getChannel() {
        return channel;
    }

    public void setChannel(Notification.NotificationChannel channel) {
        this.channel = channel;
        this.updatedAt = LocalDateTime.now();
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Business Methods
    public String processSubject(Map<String, Object> variables) {
        return replaceVariables(subjectTemplate, variables);
    }

    public String processMessage(Map<String, Object> variables) {
        return replaceVariables(messageTemplate, variables);
    }

    private String replaceVariables(String template, Map<String, Object> variables) {
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            result = result.replace(placeholder, entry.getValue().toString());
        }
        return result;
    }
}