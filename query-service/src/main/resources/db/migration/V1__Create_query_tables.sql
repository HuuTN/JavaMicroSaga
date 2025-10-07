-- Create product view for read model
CREATE TABLE product_view (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    stock_quantity INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_event_id VARCHAR(36),
    last_event_version BIGINT
);

-- Create order view for read model
CREATE TABLE order_view (
    id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    item_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_event_id VARCHAR(36),
    last_event_version BIGINT
);

-- Create order item view
CREATE TABLE order_item_view (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES order_view(id) ON DELETE CASCADE
);

-- Create product summary view for analytics
CREATE TABLE product_summary_view (
    id VARCHAR(36) PRIMARY KEY,
    category VARCHAR(100) NOT NULL,
    total_products INT DEFAULT 0,
    total_stock_value DECIMAL(15, 2) DEFAULT 0,
    avg_price DECIMAL(10, 2) DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create customer order summary view
CREATE TABLE customer_order_summary_view (
    customer_id VARCHAR(36) PRIMARY KEY,
    total_orders INT DEFAULT 0,
    total_amount DECIMAL(15, 2) DEFAULT 0,
    avg_order_amount DECIMAL(10, 2) DEFAULT 0,
    last_order_date TIMESTAMP NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create event processing log to track processed events
CREATE TABLE event_processing_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL UNIQUE,
    event_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    event_version BIGINT NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processing_time_ms BIGINT,
    INDEX idx_event_id (event_id),
    INDEX idx_aggregate (aggregate_id, aggregate_type),
    INDEX idx_processed_at (processed_at)
);

-- Create indexes for better query performance
CREATE INDEX idx_product_view_category ON product_view(category);
CREATE INDEX idx_product_view_name ON product_view(name);
CREATE INDEX idx_product_view_price ON product_view(price);
CREATE INDEX idx_product_view_stock ON product_view(stock_quantity);

CREATE INDEX idx_order_view_customer ON order_view(customer_id);
CREATE INDEX idx_order_view_status ON order_view(status);
CREATE INDEX idx_order_view_created ON order_view(created_at);

CREATE INDEX idx_order_item_view_order ON order_item_view(order_id);
CREATE INDEX idx_order_item_view_product ON order_item_view(product_id);

CREATE INDEX idx_product_summary_category ON product_summary_view(category);

-- Create triggers to maintain denormalized data
DELIMITER //

CREATE TRIGGER update_order_item_count 
AFTER INSERT ON order_item_view
FOR EACH ROW
BEGIN
    UPDATE order_view 
    SET item_count = (
        SELECT COUNT(*) FROM order_item_view WHERE order_id = NEW.order_id
    )
    WHERE id = NEW.order_id;
END//

CREATE TRIGGER update_order_item_count_delete
AFTER DELETE ON order_item_view
FOR EACH ROW
BEGIN
    UPDATE order_view 
    SET item_count = (
        SELECT COUNT(*) FROM order_item_view WHERE order_id = OLD.order_id
    )
    WHERE id = OLD.order_id;
END//

DELIMITER ;