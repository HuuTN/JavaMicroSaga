package com.example.saga.orchestrator.repository;

import com.example.saga.orchestrator.entity.SagaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SagaTransactionRepository extends JpaRepository<SagaTransaction, Long> {
    
    Optional<SagaTransaction> findBySagaId(String sagaId);
    
    List<SagaTransaction> findByStatus(SagaTransaction.SagaStatus status);
    
    List<SagaTransaction> findBySagaType(String sagaType);
    
    @Query("SELECT s FROM SagaTransaction s WHERE s.status = :status AND s.updatedAt < :timeout")
    List<SagaTransaction> findTimedOutSagas(SagaTransaction.SagaStatus status, LocalDateTime timeout);
    
    @Query("SELECT s FROM SagaTransaction s WHERE s.createdAt BETWEEN :startDate AND :endDate")
    List<SagaTransaction> findSagasByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM SagaTransaction s WHERE s.status = :status")
    Long countByStatus(SagaTransaction.SagaStatus status);
    
    @Query("SELECT s FROM SagaTransaction s WHERE s.status IN :statuses ORDER BY s.createdAt DESC")
    List<SagaTransaction> findByStatusInOrderByCreatedAtDesc(List<SagaTransaction.SagaStatus> statuses);
}