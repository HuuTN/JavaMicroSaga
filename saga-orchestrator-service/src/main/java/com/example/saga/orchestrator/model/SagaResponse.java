package com.example.saga.orchestrator.model;

public class SagaResponse {
    
    private String sagaId;
    private String status;
    private String message;
    private Object data;
    
    // Constructors
    public SagaResponse() {}
    
    public SagaResponse(String sagaId, String status, String message) {
        this.sagaId = sagaId;
        this.status = status;
        this.message = message;
    }
    
    public SagaResponse(String sagaId, String status, String message, Object data) {
        this.sagaId = sagaId;
        this.status = status;
        this.message = message;
        this.data = data;
    }
    
    // Static factory methods
    public static SagaResponse success(String sagaId, String message) {
        return new SagaResponse(sagaId, "SUCCESS", message);
    }
    
    public static SagaResponse success(String sagaId, String message, Object data) {
        return new SagaResponse(sagaId, "SUCCESS", message, data);
    }
    
    public static SagaResponse failed(String sagaId, String message) {
        return new SagaResponse(sagaId, "FAILED", message);
    }
    
    public static SagaResponse inProgress(String sagaId, String message) {
        return new SagaResponse(sagaId, "IN_PROGRESS", message);
    }

    // Getters and Setters
    public String getSagaId() {
        return sagaId;
    }

    public void setSagaId(String sagaId) {
        this.sagaId = sagaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}