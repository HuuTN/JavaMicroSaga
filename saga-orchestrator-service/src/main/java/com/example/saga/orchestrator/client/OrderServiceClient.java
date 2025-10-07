package com.example.saga.orchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "order-service", url = "${services.order-service.url:http://localhost:8091}")
public interface OrderServiceClient {
    
    @PostMapping("/api/orders")
    Map<String, Object> createOrder(@RequestBody Map<String, Object> orderRequest);
    
    @PutMapping("/api/orders/{orderId}/cancel")
    Map<String, Object> cancelOrder(@PathVariable String orderId, 
                                   @RequestBody Map<String, Object> cancelRequest);
    
    @GetMapping("/api/orders/{orderId}")
    Map<String, Object> getOrder(@PathVariable String orderId);
    
    @PutMapping("/api/orders/{orderId}/confirm")
    Map<String, Object> confirmOrder(@PathVariable String orderId);
}