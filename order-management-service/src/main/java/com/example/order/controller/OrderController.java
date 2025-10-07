package com.example.order.controller;

import com.example.order.entity.Order;
import com.example.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> orderRequest) {
        try {
            Order order = orderService.createOrder(orderRequest);
            Map<String, Object> response = Map.of(
                "orderId", order.getOrderId(),
                "status", order.getStatus().toString(),
                "totalAmount", order.getTotalAmount(),
                "createdAt", order.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to create order: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.getOrder(orderId);
            Map<String, Object> response = Map.of(
                "orderId", order.getOrderId(),
                "customerId", order.getCustomerId(),
                "status", order.getStatus().toString(),
                "totalAmount", order.getTotalAmount(),
                "shippingAddress", order.getShippingAddress(),
                "createdAt", order.getCreatedAt(),
                "updatedAt", order.getUpdatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Order not found: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Map<String, Object>> confirmOrder(@PathVariable String orderId) {
        try {
            Order order = orderService.confirmOrder(orderId);
            Map<String, Object> response = Map.of(
                "orderId", order.getOrderId(),
                "status", order.getStatus().toString(),
                "updatedAt", order.getUpdatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to confirm order: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(@PathVariable String orderId, 
                                                          @RequestBody Map<String, Object> cancelRequest) {
        try {
            Order order = orderService.cancelOrder(orderId, cancelRequest);
            Map<String, Object> response = Map.of(
                "orderId", order.getOrderId(),
                "status", order.getStatus().toString(),
                "updatedAt", order.getUpdatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to cancel order: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomer(@PathVariable String customerId) {
        try {
            List<Order> orders = orderService.getOrdersByCustomer(customerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "order-management-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}