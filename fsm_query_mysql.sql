Create database FashionShopManagement;
Use FashionShopManagement;

-- 1. Roles
CREATE TABLE roles (
  id INT AUTO_INCREMENT PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE, -- 'ROLE_ADMIN','ROLE_STAFF','ROLE_USER','ROLE_GUEST'
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255)
);

-- 2. Users (Google Oauth)
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  google_sub VARCHAR(255) UNIQUE,    -- subject from Google ID token (unique id of each google account/email)
  email VARCHAR(255) UNIQUE NOT NULL,
  display_name VARCHAR(255),
  full_name VARCHAR(255),
  password VARCHAR(255),
  phone VARCHAR(50),
  default_address TEXT,
  avatar_url VARCHAR(512),
  role_id INT NOT NULL,
  active BOOLEAN DEFAULT TRUE,
  profile_completed BOOLEAN DEFAULT FALSE, -- if all fields is fullfilled by user?
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- 3. Categories & Brands
CREATE TABLE categories (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL UNIQUE,
  slug VARCHAR(150) NOT NULL UNIQUE, -- (aimple url for user: ex 'man-shoes')
  parent_id INT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (parent_id) REFERENCES categories(id)
);

CREATE TABLE brands (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL UNIQUE,
  slug VARCHAR(150) NOT NULL UNIQUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 4. Products and variants (size/color) & images
CREATE TABLE products (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  sku VARCHAR(100) UNIQUE,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  brand_id INT,
  category_id INT,
  base_price DECIMAL(12,2) NOT NULL,
  active BOOLEAN DEFAULT TRUE,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (brand_id) REFERENCES brands(id),
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- If you need variants (size/color), create product_variants
CREATE TABLE product_variants (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  sku VARCHAR(150) UNIQUE, -- (unique id of each variant, ex: L size, red T Shirt will have sku: TS-L-R)
  attribute_json JSON NULL, -- e.g. {"size":"M","color":"red"}
  price DECIMAL(12,2) NOT NULL,
  stock INT DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE product_images (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  product_id BIGINT NOT NULL,
  url VARCHAR(1000) NOT NULL,
  orders INT DEFAULT 0,
  is_primary BOOLEAN DEFAULT FALSE,
  FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 5. Cart (simple persistent cart per user)
CREATE TABLE carts (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE cart_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  cart_id BIGINT NOT NULL,
  product_variant_id BIGINT,
  qty INT NOT NULL DEFAULT 1,
  added_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (cart_id) REFERENCES carts(id),
  FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
);

-- 6. Orders & order items
CREATE TABLE orders (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_code VARCHAR(100) UNIQUE NOT NULL, -- human readable code
  user_id BIGINT NULL, -- allow guest checkout if desired
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PAID, CANCELLED, SHIPPED...
  total_amount DECIMAL(14,2) NOT NULL,
  shipping_amount DECIMAL(12,2) DEFAULT 0,
  address TEXT,
  phone VARCHAR(50),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  product_variant_id BIGINT,
  product_title VARCHAR(255) NOT NULL,
  unit_price DECIMAL(12,2) NOT NULL,
  qty INT NOT NULL,
  subtotal DECIMAL(14,2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES orders(id),
  FOREIGN KEY (product_variant_id) REFERENCES product_variants(id)
);

-- 7. Payments (VNPay)
CREATE TABLE payments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  order_id BIGINT NOT NULL,
  vnp_txnref VARCHAR(100) UNIQUE,   -- vnp_TxnRef returned or used
  vnp_trans_date VARCHAR(50),
  vnp_response_code VARCHAR(10),
  vnp_payment_no VARCHAR(100),
  amount DECIMAL(14,2) NOT NULL,
  method VARCHAR(50) DEFAULT 'VNPAY',
  status VARCHAR(50) DEFAULT 'INIT', -- INIT, SUCCESS, FAILED, REFUNDED
  raw_request TEXT,
  raw_response TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (order_id) REFERENCES orders(id)
);

-- 8. Audit log (record CRUD actions)
CREATE TABLE audit_log (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  actor_id BIGINT NULL,              -- user who did the action (nullable for system)
  actor_role VARCHAR(100) NULL,
  entity VARCHAR(100) NOT NULL,      -- table/entity name e.g. 'products'
  entity_id VARCHAR(255) NULL,       -- id of entity changed
  action VARCHAR(50) NOT NULL,       -- CREATE, UPDATE, DELETE, VIEW
  changes JSON NULL,                 -- optional: diff / payload
  ip_address VARCHAR(100),
  user_agent VARCHAR(512),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (actor_id) REFERENCES users(id)
);

-- 9. Indexes for performance (examples)
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_brand ON products(brand_id);
CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_payments_order ON payments(order_id);
select * from users