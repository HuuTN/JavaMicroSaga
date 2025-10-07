package com.example.saga.orchestrator.service;

import com.example.saga.orchestrator.client.*;
import com.example.saga.orchestrator.entity.SagaStep;
import com.example.saga.orchestrator.entity.SagaTransaction;
import com.example.saga.orchestrator.model.OrderRequest;
import com.example.saga.orchestrator.repository.SagaTransactionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class OrderSagaOrchestrator {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderSagaOrchestrator.class);
    
    @Autowired
    private SagaTransactionRepository sagaRepository;
    
    @Autowired
    private OrderServiceClient orderServiceClient;
    
    @Autowired
    private InventoryServiceClient inventoryServiceClient;
    
    @Autowired
    private PaymentServiceClient paymentServiceClient;
    
    @Autowired
    private NotificationServiceClient notificationServiceClient;
    
    @Autowired
    private ObjectMapper objectMapper;

    public String startOrderSaga(OrderRequest orderRequest) {
        try {
            String sagaId = UUID.randomUUID().toString();
            String payload = objectMapper.writeValueAsString(orderRequest);
            
            SagaTransaction saga = new SagaTransaction(sagaId, "ORDER_SAGA", payload);
            saga.setTotalSteps(4); // Order -> Inventory -> Payment -> Notification
            
            // Initialize saga steps
            initializeSagaSteps(saga, orderRequest);
            
            saga = sagaRepository.save(saga);
            
            logger.info("Started Order Saga with ID: {}", sagaId);
            
            // Start executing the saga
            executeNextStep(saga);
            
            return sagaId;
            
        } catch (Exception e) {
            logger.error("Failed to start Order Saga", e);
            throw new RuntimeException("Failed to start saga", e);
        }
    }

    private void initializeSagaSteps(SagaTransaction saga, OrderRequest orderRequest) {
        try {
            // Step 1: Create Order
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("customerId", orderRequest.getCustomerId());
            orderData.put("items", orderRequest.getItems());
            orderData.put("totalAmount", orderRequest.getTotalAmount());
            orderData.put("shippingAddress", orderRequest.getShippingAddress());
            
            SagaStep orderStep = new SagaStep(1, "CREATE_ORDER", "order-service",
                    objectMapper.writeValueAsString(orderData), saga);
            saga.getSteps().add(orderStep);
            
            // Step 2: Reserve Inventory
            Map<String, Object> inventoryData = new HashMap<>();
            inventoryData.put("items", orderRequest.getItems());
            
            SagaStep inventoryStep = new SagaStep(2, "RESERVE_INVENTORY", "inventory-service",
                    objectMapper.writeValueAsString(inventoryData), saga);
            saga.getSteps().add(inventoryStep);
            
            // Step 3: Process Payment
            Map<String, Object> paymentData = new HashMap<>();
            paymentData.put("customerId", orderRequest.getCustomerId());
            paymentData.put("amount", orderRequest.getTotalAmount());
            paymentData.put("paymentMethod", orderRequest.getPaymentMethod());
            
            SagaStep paymentStep = new SagaStep(3, "PROCESS_PAYMENT", "payment-service",
                    objectMapper.writeValueAsString(paymentData), saga);
            saga.getSteps().add(paymentStep);
            
            // Step 4: Send Notification
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("customerId", orderRequest.getCustomerId());
            notificationData.put("type", "ORDER_CONFIRMATION");
            
            SagaStep notificationStep = new SagaStep(4, "SEND_NOTIFICATION", "notification-service",
                    objectMapper.writeValueAsString(notificationData), saga);
            saga.getSteps().add(notificationStep);
            
        } catch (Exception e) {
            logger.error("Failed to initialize saga steps", e);
            throw new RuntimeException("Failed to initialize saga steps", e);
        }
    }

    public void executeNextStep(SagaTransaction saga) {
        try {
            if (saga.isCompleted() || saga.isFailed()) {
                return;
            }

            int currentStep = saga.getCurrentStep();
            if (currentStep >= saga.getTotalSteps()) {
                // All steps completed successfully
                saga.complete();
                sagaRepository.save(saga);
                logger.info("Saga {} completed successfully", saga.getSagaId());
                return;
            }

            SagaStep step = saga.getSteps().get(currentStep);
            executeStep(saga, step);
            
        } catch (Exception e) {
            logger.error("Failed to execute next step for saga {}", saga.getSagaId(), e);
            handleSagaFailure(saga, e.getMessage());
        }
    }

    private void executeStep(SagaTransaction saga, SagaStep step) {
        try {
            step.execute();
            sagaRepository.save(saga);
            
            logger.info("Executing step {} for saga {}", step.getStepName(), saga.getSagaId());
            
            Map<String, Object> request = objectMapper.readValue(step.getRequestPayload(), Map.class);
            Map<String, Object> response;
            
            switch (step.getStepName()) {
                case "CREATE_ORDER":
                    response = orderServiceClient.createOrder(request);
                    break;
                case "RESERVE_INVENTORY":
                    response = inventoryServiceClient.reserveInventory(request);
                    break;
                case "PROCESS_PAYMENT":
                    response = paymentServiceClient.processPayment(request);
                    break;
                case "SEND_NOTIFICATION":
                    response = notificationServiceClient.sendOrderConfirmation(request);
                    break;
                default:
                    throw new RuntimeException("Unknown step: " + step.getStepName());
            }
            
            // Handle successful response
            String responsePayload = objectMapper.writeValueAsString(response);
            step.complete(responsePayload);
            saga.nextStep();
            
            sagaRepository.save(saga);
            
            logger.info("Step {} completed for saga {}", step.getStepName(), saga.getSagaId());
            
            // Continue to next step
            executeNextStep(saga);
            
        } catch (Exception e) {
            logger.error("Step {} failed for saga {}", step.getStepName(), saga.getSagaId(), e);
            step.fail(e.getMessage());
            sagaRepository.save(saga);
            
            // Start compensation
            compensateSaga(saga);
        }
    }

    public void compensateSaga(SagaTransaction saga) {
        try {
            logger.info("Starting compensation for saga {}", saga.getSagaId());
            saga.setStatus(SagaTransaction.SagaStatus.COMPENSATING);
            sagaRepository.save(saga);
            
            // Compensate completed steps in reverse order
            List<SagaStep> completedSteps = saga.getSteps().stream()
                    .filter(SagaStep::needsCompensation)
                    .sorted((s1, s2) -> s2.getStepNumber().compareTo(s1.getStepNumber()))
                    .toList();
            
            for (SagaStep step : completedSteps) {
                compensateStep(saga, step);
            }
            
            saga.setStatus(SagaTransaction.SagaStatus.COMPENSATED);
            sagaRepository.save(saga);
            
            logger.info("Compensation completed for saga {}", saga.getSagaId());
            
        } catch (Exception e) {
            logger.error("Failed to compensate saga {}", saga.getSagaId(), e);
            handleSagaFailure(saga, "Compensation failed: " + e.getMessage());
        }
    }

    private void compensateStep(SagaTransaction saga, SagaStep step) {
        try {
            logger.info("Compensating step {} for saga {}", step.getStepName(), saga.getSagaId());
            
            Map<String, Object> compensationData = new HashMap<>();
            if (step.getResponsePayload() != null) {
                Map<String, Object> response = objectMapper.readValue(step.getResponsePayload(), Map.class);
                compensationData.putAll(response);
            }
            
            step.compensate(objectMapper.writeValueAsString(compensationData));
            
            switch (step.getStepName()) {
                case "CREATE_ORDER":
                    String orderId = (String) compensationData.get("orderId");
                    if (orderId != null) {
                        orderServiceClient.cancelOrder(orderId, compensationData);
                    }
                    break;
                case "RESERVE_INVENTORY":
                    inventoryServiceClient.releaseInventory(compensationData);
                    break;
                case "PROCESS_PAYMENT":
                    String paymentId = (String) compensationData.get("paymentId");
                    if (paymentId != null) {
                        paymentServiceClient.refundPayment(paymentId, compensationData);
                    }
                    break;
                case "SEND_NOTIFICATION":
                    // Send cancellation notification
                    compensationData.put("type", "ORDER_CANCELLATION");
                    notificationServiceClient.sendOrderCancellation(compensationData);
                    break;
            }
            
            step.compensated();
            sagaRepository.save(saga);
            
            logger.info("Step {} compensated for saga {}", step.getStepName(), saga.getSagaId());
            
    } catch (Exception e) {
        logger.error("Failed to compensate step {} for saga {}", 
            step.getStepName(), saga.getSagaId(), e);
        throw new RuntimeException(e);
    }
    }

    private void handleSagaFailure(SagaTransaction saga, String errorMessage) {
        saga.fail(errorMessage);
        sagaRepository.save(saga);
        logger.error("Saga {} failed: {}", saga.getSagaId(), errorMessage);
    }

    public SagaTransaction getSagaStatus(String sagaId) {
        return sagaRepository.findBySagaId(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found: " + sagaId));
    }

    public List<SagaTransaction> getAllSagas() {
        return sagaRepository.findAll();
    }

    public void retrySaga(String sagaId) {
        SagaTransaction saga = getSagaStatus(sagaId);
        if (saga.isFailed()) {
            logger.info("Retrying saga {}", sagaId);
            saga.setStatus(SagaTransaction.SagaStatus.IN_PROGRESS);
            saga.setErrorMessage(null);
            executeNextStep(saga);
        } else {
            throw new RuntimeException("Cannot retry saga in status: " + saga.getStatus());
        }
    }
}