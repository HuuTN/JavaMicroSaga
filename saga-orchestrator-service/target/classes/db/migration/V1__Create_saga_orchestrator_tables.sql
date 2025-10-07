CREATE DATABASE IF NOT EXISTS saga_orchestrator_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE saga_orchestrator_db;

-- Saga Transactions Table
CREATE TABLE IF NOT EXISTS saga_transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    saga_id VARCHAR(255) NOT NULL UNIQUE,
    saga_type VARCHAR(100) NOT NULL,
    status ENUM('STARTED', 'IN_PROGRESS', 'COMPENSATING', 'COMPLETED', 'FAILED', 'COMPENSATED') NOT NULL DEFAULT 'STARTED',
    current_step INT DEFAULT 0,
    total_steps INT DEFAULT 0,
    payload JSON,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    
    INDEX idx_saga_id (saga_id),
    INDEX idx_status (status),
    INDEX idx_saga_type (saga_type),
    INDEX idx_created_at (created_at),
    INDEX idx_status_updated (status, updated_at)
) ENGINE=InnoDB;

-- Saga Steps Table
CREATE TABLE IF NOT EXISTS saga_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    saga_transaction_id BIGINT NOT NULL,
    step_number INT NOT NULL,
    step_name VARCHAR(100) NOT NULL,
    service_name VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'EXECUTING', 'COMPLETED', 'FAILED', 'COMPENSATING', 'COMPENSATED') NOT NULL DEFAULT 'PENDING',
    request_payload JSON,
    response_payload JSON,
    error_message TEXT,
    compensation_data JSON,
    retry_count INT DEFAULT 0,
    max_retries INT DEFAULT 3,
    started_at TIMESTAMP NULL,
    completed_at TIMESTAMP NULL,
    
    FOREIGN KEY (saga_transaction_id) REFERENCES saga_transactions(id) ON DELETE CASCADE,
    INDEX idx_saga_transaction_id (saga_transaction_id),
    INDEX idx_step_number (step_number),
    INDEX idx_status (status),
    INDEX idx_service_name (service_name),
    UNIQUE KEY unique_saga_step (saga_transaction_id, step_number)
) ENGINE=InnoDB;

-- Saga Events Table (for event sourcing)
CREATE TABLE IF NOT EXISTS saga_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    saga_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    event_data JSON NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_saga_id (saga_id),
    INDEX idx_event_type (event_type),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Saga Metrics Table (for monitoring)
CREATE TABLE IF NOT EXISTS saga_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    saga_type VARCHAR(100) NOT NULL,
    date_hour TIMESTAMP NOT NULL,
    total_count INT DEFAULT 0,
    completed_count INT DEFAULT 0,
    failed_count INT DEFAULT 0,
    compensated_count INT DEFAULT 0,
    avg_duration_seconds DECIMAL(10,2) DEFAULT 0,
    
    UNIQUE KEY unique_saga_type_hour (saga_type, date_hour),
    INDEX idx_saga_type (saga_type),
    INDEX idx_date_hour (date_hour)
) ENGINE=InnoDB;