package com.example.saga.orchestrator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saga_steps")
public class SagaStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;
    
    @Column(name = "step_name", nullable = false)
    private String stepName;
    
    @Column(name = "service_name", nullable = false)
    private String serviceName;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StepStatus status;
    
    @Column(name = "request_payload", columnDefinition = "JSON")
    private String requestPayload;
    
    @Column(name = "response_payload", columnDefinition = "JSON")
    private String responsePayload;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "compensation_data", columnDefinition = "JSON")
    private String compensationData;
    
    @Column(name = "retry_count", columnDefinition = "INT DEFAULT 0")
    private Integer retryCount = 0;
    
    @Column(name = "max_retries", columnDefinition = "INT DEFAULT 3")
    private Integer maxRetries = 3;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "saga_transaction_id", nullable = false)
    private SagaTransaction sagaTransaction;

    // Constructors
    public SagaStep() {}

    public SagaStep(Integer stepNumber, String stepName, String serviceName, 
                   String requestPayload, SagaTransaction sagaTransaction) {
        this.stepNumber = stepNumber;
        this.stepName = stepName;
        this.serviceName = serviceName;
        this.requestPayload = requestPayload;
        this.sagaTransaction = sagaTransaction;
        this.status = StepStatus.PENDING;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getStepName() {
        return stepName;
    }

    public void setStepName(String stepName) {
        this.stepName = stepName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public StepStatus getStatus() {
        return status;
    }

    public void setStatus(StepStatus status) {
        this.status = status;
        if (status == StepStatus.EXECUTING) {
            this.startedAt = LocalDateTime.now();
        } else if (status == StepStatus.COMPLETED || status == StepStatus.FAILED || 
                   status == StepStatus.COMPENSATED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCompensationData() {
        return compensationData;
    }

    public void setCompensationData(String compensationData) {
        this.compensationData = compensationData;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public SagaTransaction getSagaTransaction() {
        return sagaTransaction;
    }

    public void setSagaTransaction(SagaTransaction sagaTransaction) {
        this.sagaTransaction = sagaTransaction;
    }

    // Business Methods
    public void execute() {
        this.status = StepStatus.EXECUTING;
        this.startedAt = LocalDateTime.now();
    }

    public void complete(String responsePayload) {
        this.status = StepStatus.COMPLETED;
        this.responsePayload = responsePayload;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String errorMessage) {
        this.status = StepStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = LocalDateTime.now();
    }

    public void compensate(String compensationData) {
        this.status = StepStatus.COMPENSATING;
        this.compensationData = compensationData;
        this.startedAt = LocalDateTime.now();
    }

    public void compensated() {
        this.status = StepStatus.COMPENSATED;
        this.completedAt = LocalDateTime.now();
    }

    public void retry() {
        this.retryCount++;
        this.status = StepStatus.PENDING;
    }

    public boolean canRetry() {
        return this.retryCount < this.maxRetries;
    }

    public boolean needsCompensation() {
        return status == StepStatus.COMPLETED;
    }

    public enum StepStatus {
        PENDING,
        EXECUTING,
        COMPLETED,
        FAILED,
        COMPENSATING,
        COMPENSATED
    }
}