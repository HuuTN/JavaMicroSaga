package com.example.saga.orchestrator.controller;

import com.example.saga.orchestrator.entity.SagaTransaction;
import com.example.saga.orchestrator.model.OrderRequest;
import com.example.saga.orchestrator.model.SagaResponse;
import com.example.saga.orchestrator.service.OrderSagaOrchestrator;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/saga")
public class SagaOrchestratorController {
    
    @Autowired
    private OrderSagaOrchestrator sagaOrchestrator;

    @PostMapping("/orders")
    public ResponseEntity<SagaResponse> startOrderSaga(@Valid @RequestBody OrderRequest orderRequest) {
        try {
            String sagaId = sagaOrchestrator.startOrderSaga(orderRequest);
            return ResponseEntity.ok(SagaResponse.inProgress(sagaId, 
                    "Order saga started successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    SagaResponse.failed(null, "Failed to start order saga: " + e.getMessage()));
        }
    }

    @GetMapping("/status/{sagaId}")
    public ResponseEntity<SagaResponse> getSagaStatus(@PathVariable String sagaId) {
        try {
            SagaTransaction saga = sagaOrchestrator.getSagaStatus(sagaId);
            return ResponseEntity.ok(SagaResponse.success(sagaId, 
                    "Saga status retrieved", saga));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    SagaResponse.failed(sagaId, "Failed to get saga status: " + e.getMessage()));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<SagaTransaction>> getAllSagas() {
        try {
            List<SagaTransaction> sagas = sagaOrchestrator.getAllSagas();
            return ResponseEntity.ok(sagas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/retry/{sagaId}")
    public ResponseEntity<SagaResponse> retrySaga(@PathVariable String sagaId) {
        try {
            sagaOrchestrator.retrySaga(sagaId);
            return ResponseEntity.ok(SagaResponse.inProgress(sagaId, 
                    "Saga retry initiated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    SagaResponse.failed(sagaId, "Failed to retry saga: " + e.getMessage()));
        }
    }

    @PostMapping("/compensate/{sagaId}")
    public ResponseEntity<SagaResponse> compensateSaga(@PathVariable String sagaId) {
        try {
            SagaTransaction saga = sagaOrchestrator.getSagaStatus(sagaId);
            sagaOrchestrator.compensateSaga(saga);
            return ResponseEntity.ok(SagaResponse.success(sagaId, 
                    "Saga compensation initiated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    SagaResponse.failed(sagaId, "Failed to compensate saga: " + e.getMessage()));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "saga-orchestrator-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}