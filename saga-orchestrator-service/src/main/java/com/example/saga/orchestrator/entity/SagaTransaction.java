package com.example.saga.orchestrator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "saga_transactions")
public class SagaTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "saga_id", unique = true, nullable = false)
    private String sagaId;
    
    @Column(name = "saga_type", nullable = false)
    private String sagaType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SagaStatus status;
    
    @Column(name = "current_step")
    private Integer currentStep;
    
    @Column(name = "total_steps")
    private Integer totalSteps;
    
    @Column(name = "payload", columnDefinition = "JSON")
    private String payload;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @OneToMany(mappedBy = "sagaTransaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SagaStep> steps = new ArrayList<>();

    // Constructors
    public SagaTransaction() {
        this.createdAt = LocalDateTime.now();
    }

    public SagaTransaction(String sagaId, String sagaType, String payload) {
        this();
        this.sagaId = sagaId;
        this.sagaType = sagaType;
        this.payload = payload;
        this.status = SagaStatus.STARTED;
        this.currentStep = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getSagaType() {
        return sagaType;
    }

    public void setSagaType(String sagaType) {
        this.sagaType = sagaType;
    }

    public SagaStatus getStatus() {
        return status;
    }

    public void setStatus(SagaStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == SagaStatus.COMPLETED || status == SagaStatus.FAILED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public Integer getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(Integer currentStep) {
        this.currentStep = currentStep;
        this.updatedAt = LocalDateTime.now();
    }

    public Integer getTotalSteps() {
        return totalSteps;
    }

    public void setTotalSteps(Integer totalSteps) {
        this.totalSteps = totalSteps;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
        this.updatedAt = LocalDateTime.now();
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public List<SagaStep> getSteps() {
        return steps;
    }

    public void setSteps(List<SagaStep> steps) {
        this.steps = steps;
    }

    // Business Methods
    public void nextStep() {
        this.currentStep++;
        this.updatedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = SagaStatus.FAILED;
        this.errorMessage = errorMessage;
        this.updatedAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
    }

    public void complete() {
        this.status = SagaStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
        this.completedAt = LocalDateTime.now();
    }

    public boolean isCompleted() {
        return status == SagaStatus.COMPLETED;
    }

    public boolean isFailed() {
        return status == SagaStatus.FAILED;
    }

    public boolean isCompensating() {
        return status == SagaStatus.COMPENSATING;
    }

    public enum SagaStatus {
        STARTED,
        IN_PROGRESS,
        COMPENSATING,
        COMPLETED,
        FAILED,
        COMPENSATED
    }
}