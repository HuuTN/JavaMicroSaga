package com.example.inventory.service;

import com.example.inventory.entity.InventoryItem;
import com.example.inventory.entity.Reservation;
import com.example.inventory.repository.InventoryRepository;
import com.example.inventory.repository.ReservationRepository;
import com.example.inventory.outbox.OutboxEvent;
import com.example.inventory.outbox.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public Reservation reserveInventory(Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
            String sagaId = (String) request.get("sagaId");

            Reservation mainReservation = null;

            for (Map<String, Object> itemData : items) {
                String productId = (String) itemData.get("productId");
                Integer quantity = (Integer) itemData.get("quantity");

                InventoryItem inventoryItem = inventoryRepository.findByProductId(productId)
                        .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

                if (!inventoryItem.canReserve(quantity)) {
                    throw new RuntimeException("Insufficient inventory for product: " + productId);
                }

                // Reserve the inventory
                inventoryItem.reserve(quantity);
                inventoryRepository.save(inventoryItem);

                // Create reservation record
                String reservationId = UUID.randomUUID().toString();
                Reservation reservation = new Reservation(reservationId, inventoryItem, quantity, sagaId);
                reservation = reservationRepository.save(reservation);

                if (mainReservation == null) {
                    mainReservation = reservation;
                }

                logger.info("Reserved {} units of product {} for saga {}", quantity, productId, sagaId);
            }

            // Write to outbox (Debezium will publish to Kafka)
            writeOutboxEvent("InventoryReservedEvent", sagaId, items);

            return mainReservation;

        } catch (Exception e) {
            logger.error("Failed to reserve inventory", e);
            throw new RuntimeException("Failed to reserve inventory", e);
        }
    }

    public void releaseInventory(Map<String, Object> request) {
        try {
            String sagaId = (String) request.get("sagaId");

            List<Reservation> reservations = reservationRepository.findBySagaId(sagaId);

            for (Reservation reservation : reservations) {
                if (reservation.isActive()) {
                    InventoryItem inventoryItem = reservation.getInventoryItem();
                    inventoryItem.release(reservation.getQuantity());
                    inventoryRepository.save(inventoryItem);

                    reservation.release();
                    reservationRepository.save(reservation);

                    logger.info("Released {} units of product {} for saga {}",
                            reservation.getQuantity(), inventoryItem.getProductId(), sagaId);
                }
            }

            writeOutboxEvent("InventoryReleasedEvent", sagaId, null);

        } catch (Exception e) {
            logger.error("Failed to release inventory for saga", e);
            throw new RuntimeException("Failed to release inventory", e);
        }
    }

    public Map<String, Object> checkAvailability(Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");

            boolean allAvailable = true;
            for (Map<String, Object> itemData : items) {
                String productId = (String) itemData.get("productId");
                Integer quantity = (Integer) itemData.get("quantity");

                InventoryItem inventoryItem = inventoryRepository.findByProductId(productId)
                        .orElse(null);

                if (inventoryItem == null || !inventoryItem.canReserve(quantity)) {
                    allAvailable = false;
                    break;
                }
            }

            return Map.of(
                    "available", allAvailable,
                    "items", items
            );

        } catch (Exception e) {
            logger.error("Failed to check inventory availability", e);
            return Map.of("available", false, "error", e.getMessage());
        }
    }

    public InventoryItem getInventoryItem(String productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));
    }

    public List<Reservation> getReservationsBySagaId(String sagaId) {
        return reservationRepository.findBySagaId(sagaId);
    }

    private void writeOutboxEvent(String eventType, String sagaId, List<Map<String, Object>> items) {
        try {
            Map<String, Object> event = Map.of(
                    "eventType", eventType,
                    "sagaId", sagaId,
                    "items", items != null ? items : List.of(),
                    "timestamp", System.currentTimeMillis()
            );

            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outbox = new OutboxEvent();
            outbox.setAggregateType("Inventory");
            outbox.setEventType(eventType);
            outbox.setAggregateId(sagaId);
            outbox.setPayload(payload);
            outboxRepository.save(outbox);
            logger.info("Wrote outbox inventory event for saga {}", sagaId);

        } catch (Exception ex) {
            logger.error("Failed to write inventory outbox event", ex);
        }
    }
}