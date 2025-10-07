package com.demo.eventsourcing.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event Store entity representing a stored domain event.
 * This is the core entity for event sourcing implementation.
 */
@Entity
@Table(name = "event_store")
public class StoredEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true, length = 36)
    private String eventId;

    @Column(name = "aggregate_id", nullable = false, length = 36)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "event_data", nullable = false, columnDefinition = "JSON")
    private JsonNode eventData;

    @Column(name = "metadata", columnDefinition = "JSON")
    private JsonNode metadata;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "sequence_number", nullable = false)
    private Long sequenceNumber;

    @Column(name = "timestamp", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    @Column(name = "correlation_id", length = 36)
    private String correlationId;

    @Column(name = "causation_id", length = 36)
    private String causationId;

    @Column(name = "user_id", length = 100)
    private String userId;

    // Default constructor for JPA
    protected StoredEvent() {}

    // Constructor for creating new stored events
    public StoredEvent(String eventId, String aggregateId, String aggregateType, 
                      String eventType, JsonNode eventData, JsonNode metadata,
                      Long version, String correlationId, String causationId, 
                      String userId) {
        this.eventId = eventId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.eventData = eventData;
        this.metadata = metadata;
        this.version = version;
        this.correlationId = correlationId;
        this.causationId = causationId;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public JsonNode getEventData() { return eventData; }
    public void setEventData(JsonNode eventData) { this.eventData = eventData; }

    public JsonNode getMetadata() { return metadata; }
    public void setMetadata(JsonNode metadata) { this.metadata = metadata; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public Long getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Long sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }

    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoredEvent that)) return false;
        return Objects.equals(eventId, that.eventId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventId);
    }

    @Override
    public String toString() {
        return String.format("StoredEvent{eventId='%s', aggregateId='%s', eventType='%s', version=%d, timestamp=%s}",
                eventId, aggregateId, eventType, version, timestamp);
    }
}