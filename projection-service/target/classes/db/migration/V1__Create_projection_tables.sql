-- Projection tables for materialized views and analytics

-- Product projections table
CREATE TABLE product_projections (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    total_events INT DEFAULT 0,
    view_count BIGINT DEFAULT 0,
    order_count INT DEFAULT 0,
    total_ordered_quantity INT DEFAULT 0,
    revenue DECIMAL(15,2) DEFAULT 0.00,
    average_rating DOUBLE DEFAULT 0.0,
    rating_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    price_history JSON,
    tags JSON,
    is_popular BOOLEAN DEFAULT FALSE,
    is_trending BOOLEAN DEFAULT FALSE
);

-- Create indexes for product_projections
CREATE INDEX idx_category ON product_projections (category);
CREATE INDEX idx_last_updated ON product_projections (last_updated);
CREATE INDEX idx_popular ON product_projections (is_popular);
CREATE INDEX idx_trending ON product_projections (is_trending);
CREATE INDEX idx_view_count ON product_projections (view_count);
CREATE INDEX idx_order_count ON product_projections (order_count);
CREATE INDEX idx_revenue ON product_projections (revenue);

-- Category projections table
CREATE TABLE category_projections (
    category VARCHAR(100) NOT NULL PRIMARY KEY,
    product_count INT DEFAULT 0,
    avg_price DECIMAL(10,2) DEFAULT 0.00,
    min_price DECIMAL(10,2) DEFAULT 0.00,
    max_price DECIMAL(10,2) DEFAULT 0.00,
    total_value DECIMAL(15,2) DEFAULT 0.00,
    total_orders INT DEFAULT 0,
    total_revenue DECIMAL(15,2) DEFAULT 0.00,
    avg_rating DOUBLE DEFAULT 0.0,
    total_views BIGINT DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for category_projections
CREATE INDEX idx_category_projections_category ON category_projections (category);
CREATE INDEX idx_category_projections_product_count ON category_projections (product_count);
CREATE INDEX idx_category_projections_total_revenue ON category_projections (total_revenue);

-- Customer order analytics projection
CREATE TABLE customer_analytics_view (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL UNIQUE,
    total_orders INT DEFAULT 0,
    completed_orders INT DEFAULT 0,
    cancelled_orders INT DEFAULT 0,
    total_amount_spent DECIMAL(15, 2) DEFAULT 0,
    average_order_value DECIMAL(10, 2) DEFAULT 0,
    total_items_purchased INT DEFAULT 0,
    favorite_category VARCHAR(100),
    first_order_date TIMESTAMP NULL,
    last_order_date TIMESTAMP NULL,
    days_since_last_order INT DEFAULT 0,
    customer_lifetime_value DECIMAL(15, 2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_event_id VARCHAR(36)
);

-- Create indexes for customer_analytics_view
CREATE INDEX idx_customer_analytics_customer_id ON customer_analytics_view (customer_id);
CREATE INDEX idx_customer_analytics_total_orders ON customer_analytics_view (total_orders);
CREATE INDEX idx_customer_analytics_total_spent ON customer_analytics_view (total_amount_spent);
CREATE INDEX idx_customer_analytics_last_order_date ON customer_analytics_view (last_order_date);
CREATE INDEX idx_customer_analytics_lifetime_value ON customer_analytics_view (customer_lifetime_value);

-- Daily sales summary projection
CREATE TABLE daily_sales_summary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_date DATE NOT NULL UNIQUE,
    total_orders INT DEFAULT 0,
    completed_orders INT DEFAULT 0,
    cancelled_orders INT DEFAULT 0,
    total_revenue DECIMAL(15, 2) DEFAULT 0,
    total_items_sold INT DEFAULT 0,
    unique_customers INT DEFAULT 0,
    average_order_value DECIMAL(10, 2) DEFAULT 0,
    top_selling_product_id VARCHAR(36),
    top_selling_category VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for daily_sales_summary
CREATE INDEX idx_daily_sales_sale_date ON daily_sales_summary (sale_date);
CREATE INDEX idx_daily_sales_total_revenue ON daily_sales_summary (total_revenue);
CREATE INDEX idx_daily_sales_total_orders ON daily_sales_summary (total_orders);

-- Inventory alerts projection
CREATE TABLE inventory_alerts_view (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    current_stock INT NOT NULL,
    minimum_threshold INT DEFAULT 10,
    alert_type VARCHAR(50) NOT NULL, -- LOW_STOCK, OUT_OF_STOCK, OVERSTOCKED
    alert_level VARCHAR(20) NOT NULL, -- INFO, WARNING, CRITICAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_product_id (product_id),
    INDEX idx_alert_type (alert_type),
    INDEX idx_alert_level (alert_level),
    INDEX idx_is_active (is_active),
    INDEX idx_created_at (created_at)
);

-- Order status tracking projection
CREATE TABLE order_status_tracking (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    customer_id VARCHAR(36) NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    previous_status VARCHAR(50),
    status_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    item_count INT DEFAULT 0,
    processing_time_minutes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_event_id VARCHAR(36),
    
    -- Indexes
    INDEX idx_order_id (order_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_current_status (current_status),
    INDEX idx_status_changed_at (status_changed_at)
);

-- Product popularity ranking
CREATE TABLE product_popularity_ranking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    category VARCHAR(100),
    view_count INT DEFAULT 0,
    order_count INT DEFAULT 0,
    quantity_sold INT DEFAULT 0,
    revenue_generated DECIMAL(15, 2) DEFAULT 0,
    popularity_score DECIMAL(8, 2) DEFAULT 0,
    ranking_position INT DEFAULT 0,
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_product_id (product_id),
    INDEX idx_category (category),
    INDEX idx_popularity_score (popularity_score DESC),
    INDEX idx_ranking_position (ranking_position),
    INDEX idx_period (period_start, period_end)
);

-- Projection checkpoint tracking
CREATE TABLE projection_checkpoints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    projection_name VARCHAR(100) NOT NULL UNIQUE,
    last_processed_event_id VARCHAR(36),
    last_processed_sequence BIGINT DEFAULT 0,
    last_processed_timestamp TIMESTAMP NULL,
    events_processed_count BIGINT DEFAULT 0,
    status VARCHAR(50) DEFAULT 'ACTIVE', -- ACTIVE, PAUSED, ERROR, REBUILDING
    error_message TEXT,
    retry_count INT DEFAULT 0,
    max_retry_attempts INT DEFAULT 3,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Indexes
    INDEX idx_projection_name (projection_name),
    INDEX idx_status (status),
    INDEX idx_last_processed_sequence (last_processed_sequence)
);

-- Event processing metrics
CREATE TABLE event_processing_metrics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    projection_name VARCHAR(100) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processing_date DATE NOT NULL,
    events_processed INT DEFAULT 0,
    total_processing_time_ms BIGINT DEFAULT 0,
    average_processing_time_ms DECIMAL(10, 2) DEFAULT 0,
    errors_count INT DEFAULT 0,
    success_rate DECIMAL(5, 2) DEFAULT 100.0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Unique constraint to prevent duplicates
    UNIQUE KEY uk_projection_event_date (projection_name, event_type, processing_date),
    
    -- Indexes
    INDEX idx_projection_name (projection_name),
    INDEX idx_event_type (event_type),
    INDEX idx_processing_date (processing_date),
    INDEX idx_success_rate (success_rate)
);