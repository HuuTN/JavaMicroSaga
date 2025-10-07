-- Creating tables in notification_db database

-- Notifications Table
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id VARCHAR(255) NOT NULL UNIQUE,
    customer_id VARCHAR(255) NOT NULL,
    order_id VARCHAR(255),
    saga_id VARCHAR(255),
    type ENUM('ORDER_CONFIRMATION', 'PAYMENT_CONFIRMATION', 'ORDER_CANCELLATION', 'PAYMENT_FAILED', 'INVENTORY_LOW') NOT NULL,
    subject VARCHAR(500) NOT NULL,
    message TEXT NOT NULL,
    recipient_email VARCHAR(255),
    recipient_phone VARCHAR(50),
    channel ENUM('EMAIL', 'SMS', 'PUSH') NOT NULL,
    status ENUM('PENDING', 'SENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING',
    sent_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,

    INDEX idx_notification_id (notification_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_order_id (order_id),
    INDEX idx_saga_id (saga_id),
    INDEX idx_type (type),
    INDEX idx_channel (channel),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

-- Notification Templates Table
CREATE TABLE IF NOT EXISTS notification_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    template_id VARCHAR(255) NOT NULL UNIQUE,
    type ENUM('ORDER_CONFIRMATION', 'PAYMENT_CONFIRMATION', 'ORDER_CANCELLATION', 'PAYMENT_FAILED', 'INVENTORY_LOW') NOT NULL,
    name VARCHAR(255) NOT NULL,
    subject_template VARCHAR(500) NOT NULL,
    message_template TEXT NOT NULL,
    channel ENUM('EMAIL', 'SMS', 'PUSH') NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_template_id (template_id),
    INDEX idx_type_channel (type, channel),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB;

-- Insert default notification templates
INSERT IGNORE INTO notification_templates (template_id, type, name, subject_template, message_template, channel) VALUES
('order-confirmation-email', 'ORDER_CONFIRMATION', 'Order Confirmation Email',
 'Your Order {{sagaId}} Has Been Confirmed',
 'Dear Customer,\n\nYour order with ID {{sagaId}} has been successfully confirmed.\n\nOrder Details:\n- Customer ID: {{customerId}}\n- Order Date: {{timestamp}}\n\nThank you for shopping with us!\n\nBest regards,\nE-Commerce Team',
 'EMAIL'),

('payment-confirmation-email', 'PAYMENT_CONFIRMATION', 'Payment Confirmation Email',
 'Payment Confirmation for Order {{sagaId}}',
 'Dear Customer,\\n\\nYour payment for order {{sagaId}} has been successfully processed.\\n\\nPayment Details:\\n- Customer ID: {{customerId}}\\n- Amount: {{amount}}\\n- Payment Date: {{timestamp}}\\n\\nThank you for your payment!\\n\\nBest regards,\\nPayment Team',
 'EMAIL'),

('order-cancellation-email', 'ORDER_CANCELLATION', 'Order Cancellation Email',
 'Order {{sagaId}} Has Been Cancelled',
 'Dear Customer,\n\nWe regret to inform you that your order {{sagaId}} has been cancelled.\n\nCancellation Details:\n- Customer ID: {{customerId}}\n- Cancellation Date: {{timestamp}}\n\nIf you have any questions, please contact our support team.\n\nBest regards,\nCustomer Service Team',
 'EMAIL');