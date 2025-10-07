package com.demo.eventsourcing.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Aggregate Snapshot entity for performance optimization.
 * Snapshots store the state of an aggregate at a specific version.
 */
@Entity
@Table(name = "aggregate_snapshots")
public class AggregateSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_id", nullable = false, length = 36)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "snapshot_data", nullable = false, columnDefinition = "JSON")
    private JsonNode snapshotData;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "timestamp", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    private LocalDateTime timestamp;

    // Default constructor for JPA
    protected AggregateSnapshot() {}

    // Constructor for creating new snapshots
    public AggregateSnapshot(String aggregateId, String aggregateType, 
                           JsonNode snapshotData, Long version) {
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.snapshotData = snapshotData;
        this.version = version;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }

    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }

    public JsonNode getSnapshotData() { return snapshotData; }
    public void setSnapshotData(JsonNode snapshotData) { this.snapshotData = snapshotData; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AggregateSnapshot that)) return false;
        return Objects.equals(aggregateId, that.aggregateId) && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(aggregateId, version);
    }

    @Override
    public String toString() {
        return String.format("AggregateSnapshot{aggregateId='%s', aggregateType='%s', version=%d, timestamp=%s}",
                aggregateId, aggregateType, version, timestamp);
    }
}