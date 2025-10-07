-- Event Store tables for Event Sourcing

-- Core event store table
CREATE TABLE event_store (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    aggregate_id VARCHAR(36) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSON NOT NULL,
    metadata JSON,
    version BIGINT NOT NULL,
    sequence_number BIGINT NOT NULL,
    timestamp TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    correlation_id VARCHAR(36),
    causation_id VARCHAR(36),
    user_id VARCHAR(100),
    
    -- Ensure version uniqueness per aggregate
    UNIQUE KEY uk_aggregate_version (aggregate_id, version),
    
    -- Indexes for performance
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_aggregate_type (aggregate_type),
    INDEX idx_event_type (event_type),
    INDEX idx_timestamp (timestamp),
    INDEX idx_correlation_id (correlation_id),
    INDEX idx_sequence_number (sequence_number)
);

-- Snapshots table for performance optimization
CREATE TABLE aggregate_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id VARCHAR(36) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    snapshot_data JSON NOT NULL,
    version BIGINT NOT NULL,
    timestamp TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Only one snapshot per aggregate version
    UNIQUE KEY uk_aggregate_snapshot (aggregate_id, version),
    
    -- Indexes
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_aggregate_type (aggregate_type),
    INDEX idx_version (version),
    INDEX idx_timestamp (timestamp)
);

-- Event stream positions for consumers
CREATE TABLE event_stream_positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consumer_group VARCHAR(100) NOT NULL,
    stream_name VARCHAR(100) NOT NULL,
    position BIGINT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- One position per consumer group per stream
    UNIQUE KEY uk_consumer_stream (consumer_group, stream_name),
    
    -- Indexes
    INDEX idx_consumer_group (consumer_group),
    INDEX idx_stream_name (stream_name)
);

-- Projection checkpoints for tracking processed events
CREATE TABLE projection_checkpoints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    projection_name VARCHAR(100) NOT NULL UNIQUE,
    last_processed_event_id VARCHAR(36),
    last_processed_sequence BIGINT NOT NULL DEFAULT 0,
    last_processed_timestamp TIMESTAMP(6),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    error_message TEXT,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_projection_name (projection_name),
    INDEX idx_status (status),
    INDEX idx_last_processed_sequence (last_processed_sequence)
);

-- Event replay log for tracking replay operations
CREATE TABLE event_replay_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    replay_id VARCHAR(36) NOT NULL UNIQUE,
    aggregate_id VARCHAR(36),
    aggregate_type VARCHAR(100),
    from_version BIGINT,
    to_version BIGINT,
    from_timestamp TIMESTAMP(6),
    to_timestamp TIMESTAMP(6),
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    events_processed INT DEFAULT 0,
    total_events INT DEFAULT 0,
    error_message TEXT,
    requested_by VARCHAR(100),
    started_at TIMESTAMP(6),
    completed_at TIMESTAMP(6),
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    
    -- Indexes
    INDEX idx_replay_id (replay_id),
    INDEX idx_aggregate_id (aggregate_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Sequence generator for global ordering
CREATE TABLE sequence_generator (
    id VARCHAR(50) PRIMARY KEY,
    next_value BIGINT NOT NULL DEFAULT 1
);

-- Initialize sequence generator
INSERT INTO sequence_generator (id, next_value) VALUES ('event_sequence', 1);

-- Create triggers and procedures for sequence generation
DELIMITER //

-- Function to get next sequence number
CREATE FUNCTION get_next_sequence() RETURNS BIGINT
READS SQL DATA
DETERMINISTIC
BEGIN
    DECLARE next_seq BIGINT;
    
    UPDATE sequence_generator 
    SET next_value = next_value + 1 
    WHERE id = 'event_sequence';
    
    SELECT next_value - 1 INTO next_seq 
    FROM sequence_generator 
    WHERE id = 'event_sequence';
    
    RETURN next_seq;
END//

-- Trigger to auto-assign sequence numbers
CREATE TRIGGER event_store_sequence_trigger 
BEFORE INSERT ON event_store
FOR EACH ROW
BEGIN
    IF NEW.sequence_number IS NULL OR NEW.sequence_number = 0 THEN
        SET NEW.sequence_number = get_next_sequence();
    END IF;
END//

DELIMITER ;

-- Create partitioning for better performance (optional for large datasets)
-- ALTER TABLE event_store PARTITION BY RANGE (YEAR(timestamp)) (
--     PARTITION p2024 VALUES LESS THAN (2025),
--     PARTITION p2025 VALUES LESS THAN (2026),
--     PARTITION p2026 VALUES LESS THAN (2027),
--     PARTITION p_future VALUES LESS THAN MAXVALUE
-- );