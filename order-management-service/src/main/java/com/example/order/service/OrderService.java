package com.example.order.service;

import com.example.order.entity.Order;
import com.example.order.entity.OrderItem;
import com.example.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.order.outbox.OutboxEvent;
import com.example.order.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Order createOrder(Map<String, Object> request) {
        try {
            String orderId = UUID.randomUUID().toString();
            String customerId = (String) request.get("customerId");
            BigDecimal totalAmount = new BigDecimal(request.get("totalAmount").toString());
            String sagaId = (String) request.get("sagaId");
            
            Order order = new Order(orderId, customerId, totalAmount, sagaId);
            order.setShippingAddress((String) request.get("shippingAddress"));
            order.setBillingAddress((String) request.get("billingAddress"));
            
            // Add order items
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            if (items != null) {
                for (Map<String, Object> itemData : items) {
                    String productId = (String) itemData.get("productId");
                    Integer quantity = (Integer) itemData.get("quantity");
                    BigDecimal unitPrice = new BigDecimal(itemData.get("unitPrice").toString());
                    
                    OrderItem orderItem = new OrderItem(productId, quantity, unitPrice, order);
                    order.getItems().add(orderItem);
                }
            }
            
            order = orderRepository.save(order);
            
            // Publish order created event
            publishOrderEvent("ORDER_CREATED", order);
            
            logger.info("Order created with ID: {}", orderId);
            return order;
            
        } catch (Exception e) {
            logger.error("Failed to create order", e);
            throw new RuntimeException("Failed to create order", e);
        }
    }

    public Order getOrder(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public Order confirmOrder(String orderId) {
        try {
            Order order = getOrder(orderId);
            order.confirm();
            order = orderRepository.save(order);
            
            // Publish order confirmed event
            publishOrderEvent("ORDER_CONFIRMED", order);
            
            logger.info("Order confirmed: {}", orderId);
            return order;
            
        } catch (Exception e) {
            logger.error("Failed to confirm order: {}", orderId, e);
            throw new RuntimeException("Failed to confirm order", e);
        }
    }

    public Order cancelOrder(String orderId, Map<String, Object> cancelRequest) {
        try {
            Order order = getOrder(orderId);
            order.cancel();
            order = orderRepository.save(order);
            
            // Publish order cancelled event
            publishOrderEvent("ORDER_CANCELLED", order);
            
            logger.info("Order cancelled: {}", orderId);
            return order;
            
        } catch (Exception e) {
            logger.error("Failed to cancel order: {}", orderId, e);
            throw new RuntimeException("Failed to cancel order", e);
        }
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
    }

    public List<Order> getOrdersBySaga(String sagaId) {
        return orderRepository.findBySagaId(sagaId);
    }

    private void publishOrderEvent(String eventType, Order order) {
        try {
            Map<String, Object> event = Map.of(
                "eventType", eventType,
                "orderId", order.getOrderId(),
                "customerId", order.getCustomerId(),
                "totalAmount", order.getTotalAmount(),
                "status", order.getStatus().toString(),
                "sagaId", order.getSagaId(),
                "timestamp", System.currentTimeMillis()
            );
            
            // Persist to outbox table; Debezium will capture and publish to Kafka
            try {
                String payload = objectMapper.writeValueAsString(event);
                OutboxEvent outbox = new OutboxEvent();
                outbox.setAggregateType("Order");
                outbox.setEventType(eventType);
                outbox.setAggregateId(order.getOrderId());
                outbox.setPayload(payload);
                outboxRepository.save(outbox);
                logger.info("Wrote outbox event {} for order {}", eventType, order.getOrderId());
            } catch (Exception e) {
                logger.error("Failed to write outbox event", e);
            }
            
        } catch (Exception e) {
            logger.error("Failed to publish order event", e);
        }
    }
}