package com.example.inventory.controller;

import com.example.inventory.entity.InventoryItem;
import com.example.inventory.entity.Reservation;
import com.example.inventory.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {
    
    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Object>> reserveInventory(@RequestBody Map<String, Object> request) {
        try {
            Reservation reservation = inventoryService.reserveInventory(request);
            Map<String, Object> response = Map.of(
                "reservationId", reservation.getReservationId(),
                "sagaId", reservation.getSagaId(),
                "status", "RESERVED",
                "createdAt", reservation.getCreatedAt()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to reserve inventory: " + e.getMessage()
            ));
        }
    }

    @PutMapping("/release")
    public ResponseEntity<Map<String, Object>> releaseInventory(@RequestBody Map<String, Object> request) {
        try {
            inventoryService.releaseInventory(request);
            return ResponseEntity.ok(Map.of(
                "status", "RELEASED",
                "message", "Inventory released successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to release inventory: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAvailability(@RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> result = inventoryService.checkAvailability(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Failed to check availability: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getInventoryItem(@PathVariable String productId) {
        try {
            InventoryItem item = inventoryService.getInventoryItem(productId);
            Map<String, Object> response = Map.of(
                "productId", item.getProductId(),
                "productName", item.getProductName(),
                "availableQuantity", item.getAvailableQuantity(),
                "reservedQuantity", item.getReservedQuantity(),
                "effectiveAvailable", item.getEffectiveAvailableQuantity(),
                "unitPrice", item.getUnitPrice(),
                "location", item.getLocation()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Product not found: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/reservations/saga/{sagaId}")
    public ResponseEntity<List<Reservation>> getReservationsBySagaId(@PathVariable String sagaId) {
        try {
            List<Reservation> reservations = inventoryService.getReservationsBySagaId(sagaId);
            return ResponseEntity.ok(reservations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "inventory-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}