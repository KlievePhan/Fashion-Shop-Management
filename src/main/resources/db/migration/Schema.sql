SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS=0;

-- ========== USER AUTHENTICATION ==========
CREATE TABLE roles (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  full_name VARCHAR(255),
  phone VARCHAR(50),
  enabled BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- ========== EMPLOYEES & CUSTOMERS ==========
CREATE TABLE employees (
  id BIGINT PRIMARY KEY,
  position VARCHAR(100),
  hire_date DATE,
  salary DECIMAL(12,2),
  FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE customers (
  id BIGINT PRIMARY KEY,
  address TEXT,
  loyalty_points INT DEFAULT 0,
  FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE
);

-- ========== PRODUCT MANAGEMENT ==========
CREATE TABLE products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  category VARCHAR(100),
  price DECIMAL(12,2) DEFAULT 0.00,
  stock_quantity INT DEFAULT 0,
  image_url VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ========== ORDER MANAGEMENT ==========
CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  customer_id BIGINT,
  employee_id BIGINT,
  status ENUM('PENDING','CONFIRMED','DELIVERED','CANCELLED') DEFAULT 'PENDING',
  total_amount DECIMAL(12,2) DEFAULT 0.00,
  order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (customer_id) REFERENCES customers(id),
  FOREIGN KEY (employee_id) REFERENCES employees(id)
);

CREATE TABLE order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE invoices (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- ========== INVENTORY MANAGEMENT ==========
CREATE TABLE inventory (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  change_qty INT NOT NULL,
  movement_type ENUM('SALE','PURCHASE','ADJUSTMENT') NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE
);

-- ========== SALES & REVENUE ==========
CREATE OR REPLACE VIEW revenue_summary AS
SELECT 
    DATE(o.order_date) AS day,
    SUM(o.total_amount) AS total_sales
FROM orders o
WHERE o.status = 'DELIVERED'
GROUP BY DATE(o.order_date);

-- ========== DASHBOARD (BASIC DATA) ==========
CREATE OR REPLACE VIEW dashboard_overview AS
SELECT 
    (SELECT COUNT(*) FROM users WHERE enabled = TRUE) AS total_users,
    (SELECT COUNT(*) FROM products) AS total_products,
    (SELECT COUNT(*) FROM orders) AS total_orders,
    (SELECT IFNULL(SUM(total_amount), 0) FROM orders WHERE status='DELIVERED') AS total_revenue;

-- ========== SEED DATA ==========
INSERT INTO roles (name) VALUES ('ADMIN'), ('EMPLOYEE'), ('CUSTOMER');

SET FOREIGN_KEY_CHECKS=1;
