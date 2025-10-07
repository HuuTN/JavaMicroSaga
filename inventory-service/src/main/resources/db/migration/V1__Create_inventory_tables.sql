CREATE DATABASE IF NOT EXISTS inventory_service_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE inventory_service_db;

-- Inventory Items Table
CREATE TABLE IF NOT EXISTS inventory_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id VARCHAR(255) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(10,2),
    location VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_product_id (product_id),
    INDEX idx_product_name (product_name),
    INDEX idx_available_quantity (available_quantity),
    INDEX idx_location (location)
) ENGINE=InnoDB;

-- Reservations Table
CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id VARCHAR(255) NOT NULL UNIQUE,
    inventory_item_id BIGINT NOT NULL,
    order_id VARCHAR(255),
    saga_id VARCHAR(255),
    quantity INT NOT NULL,
    status ENUM('ACTIVE', 'CONFIRMED', 'RELEASED', 'EXPIRED') NOT NULL DEFAULT 'ACTIVE',
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(id) ON DELETE CASCADE,
    INDEX idx_reservation_id (reservation_id),
    INDEX idx_inventory_item_id (inventory_item_id),
    INDEX idx_order_id (order_id),
    INDEX idx_saga_id (saga_id),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Insert some sample inventory data
INSERT IGNORE INTO inventory_items (product_id, product_name, available_quantity, unit_price, location) VALUES
('product-1', 'Wireless Headphones', 100, 99.99, 'Warehouse A'),
('product-2', 'Bluetooth Speaker', 50, 49.99, 'Warehouse A'),
('product-3', 'USB Cable', 200, 9.99, 'Warehouse B'),
('product-4', 'Phone Case', 150, 19.99, 'Warehouse B'),
('product-5', 'Power Bank', 75, 29.99, 'Warehouse A');