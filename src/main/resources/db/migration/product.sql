
-- 2️⃣ PRODUCTS
INSERT INTO products (sku, title, description, brand_id, category_id, base_price, active, created_at, updated_at)
VALUES
    ('NK-AIR-001', 'Nike Air Zoom Pegasus 40', 'Giày chạy bộ hiệu năng cao của Nike.', 1, 3, 2990000, 1, NOW(), NOW()),
    ('NK-DUNK-002', 'Nike Dunk Low Retro', 'Sneaker cổ điển phong cách streetwear.', 1, 3, 2590000, 1, NOW(), NOW()),
    ('AD-ULTRA-003', 'Adidas Ultraboost 23', 'Giày chạy bộ cao cấp với công nghệ Boost.', 2, 3, 3290000, 1, NOW(), NOW()),
    ('AD-STAN-004', 'Adidas Stan Smith', 'Mẫu sneaker huyền thoại với thiết kế tối giản.', 2, 3, 2190000, 1, NOW(), NOW()),
    ('PM-RSX-005', 'Puma RS-X3 Puzzle', 'Giày thời trang hiện đại với phối màu năng động.', 3, 3, 2890000, 1, NOW(), NOW()),
    ('PM-SUEDE-006', 'Puma Suede Classic', 'Biểu tượng thời trang với chất liệu da lộn cổ điển.', 3, 3, 1990000, 1, NOW(), NOW());

-- 3️⃣ PRODUCT VARIANTS
INSERT INTO product_variants (product_id, sku, attribute_json, price, stock, created_at, updated_at)
VALUES
-- Nike Air Zoom Pegasus 40
(1, 'NK-AIR-001-BLK-41', JSON_OBJECT('size', '41', 'color', 'Black'), 2990000, 20, NOW(), NOW()),
(1, 'NK-AIR-001-WHT-42', JSON_OBJECT('size', '42', 'color', 'White'), 2990000, 15, NOW(), NOW()),

-- Nike Dunk Low Retro
(2, 'NK-DUNK-002-BLU-42', JSON_OBJECT('size', '42', 'color', 'Blue'), 2590000, 10, NOW(), NOW()),
(2, 'NK-DUNK-002-RED-43', JSON_OBJECT('size', '43', 'color', 'Red'), 2590000, 12, NOW(), NOW()),

-- Adidas Ultraboost 23
(3, 'AD-ULTRA-003-BLK-41', JSON_OBJECT('size', '41', 'color', 'Black'), 3290000, 18, NOW(), NOW()),
(3, 'AD-ULTRA-003-WHT-42', JSON_OBJECT('size', '42', 'color', 'White'), 3290000, 14, NOW(), NOW()),

-- Adidas Stan Smith
(4, 'AD-STAN-004-GRN-41', JSON_OBJECT('size', '41', 'color', 'Green'), 2190000, 25, NOW(), NOW()),
(4, 'AD-STAN-004-WHT-42', JSON_OBJECT('size', '42', 'color', 'White'), 2190000, 30, NOW(), NOW()),

-- Puma RS-X3 Puzzle
(5, 'PM-RSX-005-MULTI-41', JSON_OBJECT('size', '41', 'color', 'Multicolor'), 2890000, 20, NOW(), NOW()),
(5, 'PM-RSX-005-BLK-42', JSON_OBJECT('size', '42', 'color', 'Black'), 2890000, 15, NOW(), NOW()),

-- Puma Suede Classic
(6, 'PM-SUEDE-006-GRY-41', JSON_OBJECT('size', '41', 'color', 'Gray'), 1990000, 25, NOW(), NOW()),
(6, 'PM-SUEDE-006-NAVY-42', JSON_OBJECT('size', '42', 'color', 'Navy'), 1990000, 22, NOW(), NOW());

-- 4️⃣ PRODUCT IMAGES
INSERT INTO product_images (product_id, url, orders, is_primary)
VALUES
-- Nike Air Zoom Pegasus 40
(1, 'https://ash.vn/cdn/shop/files/AURORA_FD2722-002_PHSRH001-2000_1800x.jpg?v=1752137613', 1, TRUE),
(1, 'https://ash.vn/cdn/shop/files/AURORA_FD2722-002_PHSRH001-2000_1800x.jpg?v=1752137613', 2, FALSE),

-- Nike Dunk Low Retro
(2, 'https://static.nike.com/a/images/t_web_pdp_936_v2/f_auto/41f5c226-fb8d-40b2-ab37-9929f3bd4590/NIKE+DUNK+LOW+RETRO+SE.png', 1, TRUE),
(2, 'https://static.nike.com/a/images/t_web_pdp_936_v2/f_auto/41f5c226-fb8d-40b2-ab37-9929f3bd4590/NIKE+DUNK+LOW+RETRO+SE.png', 2, FALSE),

-- Adidas Ultraboost 23
(3, 'https://authentic-shoes.com/wp-content/uploads/2023/10/Giay_Ultraboost_23_Ngoc_lam_IE16-transformed.png', 1, TRUE),
(3, 'https://authentic-shoes.com/wp-content/uploads/2023/10/Giay_Ultraboost_23_Ngoc_lam_IE16-transformed.png3-2.jpg', 2, FALSE),

-- Adidas Stan Smith
(4, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/f6bfb2c064a64c498e57af1700593332_9366/Giay_Stan_Smith_Lux_trang_HQ6785_HM1.jpg', 1, TRUE),
(4, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/f6bfb2c064a64c498e57af1700593332_9366/Giay_Stan_Smith_Lux_trang_HQ6785_HM1.jpg', 2, FALSE),

-- Puma RS-X3 Puzzle
(5, 'https://thesneakerhouse.com/wp-content/uploads/2020/12/Puma-RS-X%C2%B3-Super-Red-White-Men-372884-01-2.jpg', 1, TRUE),
(5, 'https://thesneakerhouse.com/wp-content/uploads/2020/12/Puma-RS-X%C2%B3-Super-Red-White-Men-372884-01-2.jpg', 2, FALSE),

-- Puma Suede Classic
(6, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/399781/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-th%E1%BB%83-thao-Suede-Classic-Unisex', 1, TRUE),
(6, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa,w_2000,h_2000/global/399781/01/sv01/fnd/VNM/fmt/png/Gi%C3%A0y-th%E1%BB%83-thao-Suede-Classic-Unisex', 2, FALSE);