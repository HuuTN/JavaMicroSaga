package com.example.inventory.repository;

import com.example.inventory.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    Optional<Reservation> findByReservationId(String reservationId);
    
    List<Reservation> findBySagaId(String sagaId);
    
    List<Reservation> findByOrderId(String orderId);
}