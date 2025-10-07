package com.example.saga.orchestrator.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@FeignClient(name = "inventory-service", url = "${services.inventory-service.url:http://localhost:8093}")
public interface InventoryServiceClient {
    
    @PostMapping("/api/inventory/reserve")
    Map<String, Object> reserveInventory(@RequestBody Map<String, Object> reserveRequest);
    
    @PutMapping("/api/inventory/release")
    Map<String, Object> releaseInventory(@RequestBody Map<String, Object> releaseRequest);
    
    @GetMapping("/api/inventory/check")
    Map<String, Object> checkAvailability(@RequestParam String productId, 
                                         @RequestParam Integer quantity);
    
    @PutMapping("/api/inventory/confirm")
    Map<String, Object> confirmReservation(@RequestBody Map<String, Object> confirmRequest);
}