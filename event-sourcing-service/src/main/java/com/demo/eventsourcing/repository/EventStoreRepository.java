package com.demo.eventsourcing.repository;

import com.demo.eventsourcing.domain.StoredEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Event Store operations.
 * Provides specialized queries for event sourcing scenarios.
 */
@Repository
public interface EventStoreRepository extends JpaRepository<StoredEvent, Long> {

    /**
     * Find events by aggregate ID ordered by version
     */
    List<StoredEvent> findByAggregateIdOrderByVersionAsc(String aggregateId);

    /**
     * Find events by aggregate ID from a specific version
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.aggregateId = :aggregateId AND e.version >= :fromVersion ORDER BY e.version ASC")
    List<StoredEvent> findByAggregateIdFromVersion(@Param("aggregateId") String aggregateId, 
                                                   @Param("fromVersion") Long fromVersion);

    /**
     * Find events by aggregate ID within version range
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.aggregateId = :aggregateId AND e.version BETWEEN :fromVersion AND :toVersion ORDER BY e.version ASC")
    List<StoredEvent> findByAggregateIdVersionRange(@Param("aggregateId") String aggregateId,
                                                    @Param("fromVersion") Long fromVersion,
                                                    @Param("toVersion") Long toVersion);

    /**
     * Find events by aggregate ID up to a specific timestamp (temporal query)
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.aggregateId = :aggregateId AND e.timestamp <= :timestamp ORDER BY e.version ASC")
    List<StoredEvent> findByAggregateIdUpToTimestamp(@Param("aggregateId") String aggregateId,
                                                     @Param("timestamp") LocalDateTime timestamp);

    /**
     * Get the latest version for an aggregate
     */
    @Query("SELECT MAX(e.version) FROM StoredEvent e WHERE e.aggregateId = :aggregateId")
    Optional<Long> findLatestVersionByAggregateId(@Param("aggregateId") String aggregateId);

    /**
     * Find events by aggregate type
     */
    List<StoredEvent> findByAggregateTypeOrderBySequenceNumberAsc(String aggregateType);

    /**
     * Find events by event type
     */
    List<StoredEvent> findByEventTypeOrderBySequenceNumberAsc(String eventType);

    /**
     * Find events by correlation ID
     */
    List<StoredEvent> findByCorrelationIdOrderBySequenceNumberAsc(String correlationId);

    /**
     * Find events within a sequence range
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.sequenceNumber BETWEEN :fromSequence AND :toSequence ORDER BY e.sequenceNumber ASC")
    List<StoredEvent> findBySequenceNumberRange(@Param("fromSequence") Long fromSequence,
                                                @Param("toSequence") Long toSequence);

    /**
     * Find events from a specific sequence number
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.sequenceNumber >= :fromSequence ORDER BY e.sequenceNumber ASC")
    List<StoredEvent> findFromSequenceNumber(@Param("fromSequence") Long fromSequence);

    /**
     * Find events within timestamp range
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.timestamp BETWEEN :fromTime AND :toTime ORDER BY e.sequenceNumber ASC")
    List<StoredEvent> findByTimestampRange(@Param("fromTime") LocalDateTime fromTime,
                                          @Param("toTime") LocalDateTime toTime);

    /**
     * Get the latest sequence number
     */
    @Query("SELECT MAX(e.sequenceNumber) FROM StoredEvent e")
    Optional<Long> findLatestSequenceNumber();

    /**
     * Count events for an aggregate
     */
    long countByAggregateId(String aggregateId);

    /**
     * Check if event exists by event ID
     */
    boolean existsByEventId(String eventId);

    /**
     * Find event by event ID
     */
    Optional<StoredEvent> findByEventId(String eventId);

    /**
     * Find events for multiple aggregates
     */
    @Query("SELECT e FROM StoredEvent e WHERE e.aggregateId IN :aggregateIds ORDER BY e.sequenceNumber ASC")
    List<StoredEvent> findByAggregateIds(@Param("aggregateIds") List<String> aggregateIds);

    /**
     * Find events by user ID
     */
    List<StoredEvent> findByUserIdOrderBySequenceNumberAsc(String userId);

    /**
     * Find recent events (last N events)
     */
    @Query("SELECT e FROM StoredEvent e ORDER BY e.sequenceNumber DESC LIMIT :limit")
    List<StoredEvent> findRecentEvents(@Param("limit") int limit);
}