-- Service appointment system database tables

-- Service order table
CREATE TABLE IF NOT EXISTS service_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(64) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    user_name VARCHAR(50),
    user_phone VARCHAR(20),
    service_type VARCHAR(50) NOT NULL,
    vehicle_brand VARCHAR(50),
    vehicle_model VARCHAR(100),
    vehicle_year VARCHAR(10),
    license_plate VARCHAR(20),
    appointment_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    remark TEXT,
    related_product_id BIGINT,
    related_order_id BIGINT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_appointment_time (appointment_time),
    INDEX idx_status (status),
    INDEX idx_order_no (order_no)
);

-- Insert sample service types
CREATE TABLE IF NOT EXISTS service_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    duration_minutes INT DEFAULT 60,
    sort INT DEFAULT 0,
    status TINYINT DEFAULT 1
);

INSERT IGNORE INTO service_type (id, name, description, price, duration_minutes, sort, status) VALUES
(1, 'Oil Change Service', 'Professional oil and filter replacement', 150.00, 60, 1, 1),
(2, 'Brake System Service', 'Brake pad and disc inspection and replacement', 300.00, 120, 2, 1),
(3, 'Tire Rotation & Balance', 'Tire rotation, balancing and pressure check', 100.00, 45, 3, 1),
(4, 'Engine Diagnostic', 'Computer diagnostic and troubleshooting', 200.00, 90, 4, 1),
(5, 'AC Service', 'Air conditioning inspection and refrigerant refill', 250.00, 90, 5, 1),
(6, 'General Maintenance', 'Comprehensive vehicle inspection and maintenance', 500.00, 180, 6, 1);
