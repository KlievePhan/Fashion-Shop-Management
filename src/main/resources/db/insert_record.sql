-- =============================================
-- 1. Roles
-- =============================================
INSERT INTO roles (code, name, description) VALUES ('ROLE_USER', 'User', 'Basic permission to view products, add to cart and payment.');
INSERT INTO roles (code, name, description) VALUES ('ROLE_STAFF', 'Staff', 'Basic permissions and Limited management permission.');
INSERT INTO roles (code, name, description) VALUES ('ROLE_ADMIN', 'Admin', 'All permissions are allowed.');

-- =============================================
-- 2. Brands
-- =============================================
INSERT INTO brands (name, slug, created_at)
VALUES
('Nike', 'nike', NOW()),
('Adidas', 'adidas', NOW()),
('Puma', 'puma', NOW()),
('Himmel', 'himmel', NOW()),
('LouisVutton', 'lv', NOW());

-- =============================================
-- 3. Categories
-- =============================================
-- Top-level categories
INSERT INTO categories (name, slug, parent_id)
VALUES
('Man', 'man', NULL),
('Woman', 'woman', NULL),
('Unisex', 'unisex', NULL);
-- Subcategories under 'Man'
INSERT INTO categories (name, slug, parent_id)
VALUES
('Man Shoes', 'man-shoes', 1),
('Man Jacket', 'man-jacket', 1),
('Man Shirt', 'man-shirt', 1),
('Man Pants', 'man-pants', 1),
('Man Accessories', 'man-accessories', 1);
-- Subcategories under 'Woman'
INSERT INTO categories (name, slug, parent_id)
VALUES
('Woman Shoes', 'woman-shoes', 2),
('Woman Jacket', 'woman-jacket', 2),
('Woman Dress', 'woman-dress', 2),
('Woman Bag', 'woman-bag', 2),
('Woman Accessories', 'woman-accessories', 2);
-- Subcategories under 'Unisex'
INSERT INTO categories (name, slug, parent_id)
VALUES
('Unisex Hat', 'unisex-hat', 3),
('Unisex Jacket', 'unisex-jacket', 3),
('Unisex Sneaker', 'unisex-sneaker', 3),
('Unisex Backpack', 'unisex-backpack', 3);

-- =============================================
-- 4. Blogs (7 records)
-- =============================================
INSERT INTO blogs (
  title, excerpt, content, category, image_url, read_time, is_featured, view_count,
  created_at, updated_at, published_at, status, author, created_by, updated_by,
  tags, meta_description, slug
) VALUES
(
  'Summer Collection 2024: Vibrant Colors & Light Fabrics',
  'This season brings a fresh wave of energy with bold colors, flowing silhouettes, and sustainable materials.',
  '<p>Summer 2024 is all about embracing vibrant colors and breathable fabrics that keep you cool and stylish...</p><h2>Key Trends for Summer 2024</h2><p>From neon brights to pastel perfection...</p>',
  'TRENDS',
  'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1200&h=800&fit=crop',
  8, TRUE, 245,
  '2024-11-25 10:00:00', '2024-11-25 10:00:00', '2024-11-25 10:00:00',
  'PUBLISHED', 'Emma Stevens', 1, 1,
  'summer,2024,trends,sustainable,vibrant',
  'Discover the hottest summer fashion trends of 2024 with vibrant colors and eco-friendly fabrics.',
  'summer-collection-2024-vibrant-colors'
),
(
  '10 Must-Have Pieces for Your Capsule Wardrobe',
  'Build a versatile wardrobe with these essential pieces that mix and match effortlessly for any occasion.',
  '<p>A capsule wardrobe is the key to effortless style and reduced decision fatigue...</p>',
  'STYLE TIPS',
  'https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1200&h=800&fit=crop',
  5, FALSE, 189,
  '2024-11-20 14:30:00', '2024-11-20 14:30:00', '2024-11-20 14:30:00',
  'PUBLISHED', 'Sarah Johnson', 1, 1,
  'capsule wardrobe,minimalist,essentials',
  'Learn the 10 essential pieces to build a timeless capsule wardrobe.',
  '10-must-have-capsule-wardrobe'
),
(
  'Sustainable Fashion: The Future is Green',
  'Explore how eco-friendly brands are revolutionizing the fashion industry with innovative materials and ethical practices.',
  '<p>The fashion industry is undergoing a major transformation...</p>',
  'TRENDS',
  'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=1200&h=800&fit=crop',
  7, FALSE, 312,
  '2024-11-18 09:15:00', '2024-11-18 09:15:00', '2024-11-18 09:15:00',
  'PUBLISHED', 'Michael Chen', 2, 2,
  'sustainable,eco-friendly,ethical fashion',
  'How sustainable fashion is changing the industry with green materials and fair practices.',
  'sustainable-fashion-future-green'
),
(
  'Accessorize Like a Pro: Complete Your Look',
  'Learn the art of accessorizing with jewelry, bags, and shoes that elevate your outfit from ordinary to extraordinary.',
  '<p>Accessories have the power to transform any outfit...</p>',
  'ACCESSORIES',
  'https://images.unsplash.com/photo-1604176354204-9268737828e4?w=1200&h=800&fit=crop',
  4, FALSE, 156,
  '2024-11-15 16:45:00', '2024-11-15 16:45:00', '2024-11-15 16:45:00',
  'PUBLISHED', 'Emma Stevens', 1, 1,
  'accessories,jewelry,bags,shoes',
  'Master the art of accessorizing to complete every outfit perfectly.',
  'accessorize-like-a-pro'
),
(
  'Street Style: Fashion Inspiration from Around the World',
  'Get inspired by the most stylish street looks from fashion capitals like Paris, Tokyo, and New York.',
  '<p>Street style has become a major influence in the fashion world...</p>',
  'INSPIRATION',
  'https://images.unsplash.com/photo-1469334031218-e382a71b716b?w=1200&h=800&fit=crop',
  6, FALSE, 278,
  '2024-11-12 11:20:00', '2024-11-12 11:20:00', '2024-11-12 11:20:00',
  'PUBLISHED', 'Lisa Martinez', 3, 3,
  'street style,global fashion,inspiration',
  'Street style inspiration from Paris, Tokyo, New York, and Milan.',
  'street-style-global-inspiration'
),
(
  'Winter Elegance: Cozy Yet Chic Outfits',
  'Stay warm and stylish this winter with layering techniques and cozy pieces that don''t compromise on fashion.',
  '<p>Winter fashion doesn''t have to mean bulky sweaters...</p>',
  'SEASONAL',
  'https://images.unsplash.com/photo-1445205170230-053b83016050?w=1200&h=800&fit=crop',
  5, FALSE, 203,
  '2024-11-10 13:00:00', '2024-11-10 13:00:00', '2024-11-10 13:00:00',
  'PUBLISHED', 'Sarah Johnson', 1, 1,
  'winter,cozy,chic,layering',
  'Stay warm and stylish this winter with expert layering tips.',
  'winter-elegance-cozy-chic'
),
(
  'Fashion & Beauty: The Perfect Pairing',
  'Discover how to coordinate your makeup and hairstyle with your outfit for a cohesive, polished look.',
  '<p>Fashion and beauty go hand in hand...</p>',
  'BEAUTY',
  'https://images.unsplash.com/photo-1492707892479-7bc8d5a4ee93?w=1200&h=800&fit=crop',
  4, FALSE, 167,
  '2024-11-08 15:30:00', '2024-11-08 15:30:00', '2024-11-08 15:30:00',
  'PUBLISHED', 'Michael Chen', 2, 2,
  'beauty,makeup,hairstyle,coordination',
  'How to perfectly match your makeup and hair with any outfit.',
  'fashion-beauty-perfect-pairing'
);


-- =============================================
-- 5. Products (Total 42 records)
-- =============================================
-- Initial 6 products
INSERT INTO products (sku, title, description, brand_id, category_id, base_price, active, created_at, updated_at)
VALUES
('NK-AIR-001', 'Nike Air Zoom Pegasus 40', 'Giày chạy bộ hiệu năng cao của Nike.', 1, 3, 2990000, 1, NOW(), NOW()),
('NK-DUNK-002', 'Nike Dunk Low Retro', 'Sneaker cổ điển phong cách streetwear.', 1, 3, 2590000, 1, NOW(), NOW()),
('AD-ULTRA-003', 'Adidas Ultraboost 23', 'Giày chạy bộ cao cấp với công nghệ Boost.', 2, 3, 3290000, 1, NOW(), NOW()),
('AD-STAN-004', 'Adidas Stan Smith', 'Mẫu sneaker huyền thoại với thiết kế tối giản.', 2, 3, 2190000, 1, NOW(), NOW()),
('PM-RSX-005', 'Puma RS-X3 Puzzle', 'Giày thời trang hiện đại với phối màu năng động.', 3, 3, 2890000, 1, NOW(), NOW()),
('PM-SUEDE-006', 'Puma Suede Classic', 'Biểu tượng thời trang với chất liệu da lộn cổ điển.', 3, 3, 1990000, 1, NOW(), NOW());

-- Man Apparel (11 records)
INSERT INTO products (sku, title, description, brand_id, category_id, base_price, active, created_at, updated_at)
VALUES
-- 👕 MAN SHIRTS (ID 7-11)
('NK-TSHIRT-001', 'Nike Essential Heavy T-Shirt', 'Men t-shirt in black, loose fit, heavy fabric, easy to match with jeans or joggers.', 1, 6, 599000, 1, NOW(), NOW()),
('AD-TSHIRT-002', 'Adidas Relaxed Fit Tee', 'Men t-shirt in cream color, loose fit, heavy fabric, easy to match with jeans or joggers.', 2, 6, 629000, 1, NOW(), NOW()),
('PM-TSHIRT-003', 'Puma Graphic Marni Tee', 'Men T-shirt with fashionable prints, outstanding streetwear style.', 3, 6, 679000, 1, NOW(), NOW()),
('NK-SHIRT-004', 'Nike Classic Oxford Shirt', 'Men shirt with collar, cool cotton material, suitable for work or going out.', 1, 6, 749000, 1, NOW(), NOW()),
('AD-TSHIRT-005', 'Adidas UNESCO Graphic Tee', 'Men t-shirt with graphic print, basic form, easy to wear, suitable for everyday casual style.', 2, 6, 589000, 1, NOW(), NOW()),
-- 👖 MAN PANTS (ID 12-17)
('NK-PANTS-001', 'Nike Outdoor Utility Pants', 'Quần dài nam dáng slim, chất liệu chống nhăn, phù hợp hoạt động ngoài trời.', 1, 7, 899000, 1, NOW(), NOW()),
('AD-CHINO-002', 'Adidas Casual Chino Pants', 'Quần chino nam màu be, phom vừa, dễ phối áo thun hoặc sơ mi.', 2, 7, 799000, 1, NOW(), NOW()),
('PM-JOGGER-003', 'Puma Tech Jogger Black', 'Quần jogger nam chất vải co giãn, phù hợp tập luyện và mặc thường ngày.', 3, 7, 759000, 1, NOW(), NOW()),
('NK-PANTS-004', 'Nike Urban Cargo Pants', 'Quần cargo túi hộp, kiểu dáng hiện đại, phù hợp phong cách streetwear.', 1, 7, 829000, 1, NOW(), NOW()),
('AD-PANTS-005', 'Adidas Everyday Slim Pants', 'Quần dài nam dáng slim-fit, chất liệu mềm, mặc thoải mái cả ngày.', 2, 7, 769000, 1, NOW(), NOW()),
('PM-SHORT-006', 'Puma Summer Casual Shorts', 'Quần short nam thoáng mát, phù hợp đi chơi, dạo phố hoặc du lịch.', 3, 7, 559000, 1, NOW(), NOW());

-- Other Products (ID 18-42) - Prices in USD
INSERT INTO products (sku, title, description, brand_id, category_id, base_price, active, created_at, updated_at)
VALUES
-- NIKE PRODUCTS (Brand ID: 1) - 5 products
('NK-SHOES-018', 'Nike Air Max 270 React', 'Men running shoes with air cushion technology, comfortable for all-day wear and sports activities.', 1, 4, 150.00, 1, NOW(), NOW()),
('NK-JACKET-019', 'Nike Windrunner Jacket', 'Men windbreaker jacket, water-resistant material, perfect for outdoor activities and light rain.', 1, 5, 95.00, 1, NOW(), NOW()),
('NK-ACC-020', 'Nike Dri-FIT Headband', 'Men sports headband with sweat-wicking technology, keeps you dry during intense workouts.', 1, 8, 15.00, 1, NOW(), NOW()),
('NK-SNEAKER-021', 'Nike Court Vision Low', 'Unisex sneakers with classic basketball-inspired design, versatile for casual everyday wear.', 1, 16, 80.00, 1, NOW(), NOW()),
('NK-HAT-022', 'Nike Sportswear Heritage86', 'Unisex baseball cap with adjustable strap, breathable and lightweight for all-day comfort.', 1, 14, 30.00, 1, NOW(), NOW()),
-- ADIDAS PRODUCTS (Brand ID: 2) - 5 products
('AD-WSHOES-023', 'Adidas Cloudfoam Pure 2.0', 'Women running-inspired shoes, lightweight and comfortable for daily wear.', 2, 9, 65.00, 1, NOW(), NOW()),
('AD-WBAG-024', 'Adidas Shopper Bag Large', 'Women versatile shopper bag with spacious compartment, durable material for everyday use.', 2, 12, 40.00, 1, NOW(), NOW()),
('AD-WJACKET-025', 'Adidas Track Jacket Essentials', 'Women classic track jacket, soft fabric for warmth and casual style.', 2, 10, 110.00, 1, NOW(), NOW()),
('AD-BAG-026', 'Adidas Linear Core Duffel', 'Unisex duffel bag with shoulder strap, durable and spacious for gym or weekend trips.', 2, 17, 45.00, 1, NOW(), NOW()),
('AD-BACKPACK-027', 'Adidas Classic Badge of Sport', 'Unisex backpack with laptop sleeve, comfortable and suitable for school or work.', 2, 17, 55.00, 1, NOW(), NOW()),
-- PUMA PRODUCTS (Brand ID: 3) - 5 products
('PM-WACC-028', 'Puma Training Fitness Gloves', 'Women fitness gloves with padded palm, excellent grip for weightlifting and training.', 3, 13, 25.00, 1, NOW(), NOW()),
('PM-UJACKET-029', 'Puma Essential Padded Jacket', 'Unisex padded jacket with wind-resistant material, comfortable for outdoor activities.', 3, 15, 89.00, 1, NOW(), NOW()),
('PM-HAT-030', 'Puma Archive Logo Cap', 'Unisex snapback cap with embroidered logo, adjustable fit for street style fashion.', 3, 14, 28.00, 1, NOW(), NOW()),
('PM-SHOES-031', 'Puma Tazon 6 FM', 'Men training shoes with supportive cushioning, ideal for gym workouts and running.', 3, 4, 70.00, 1, NOW(), NOW()),
('PM-JACKET-032', 'Puma Evostripe Hooded Jacket', 'Men hooded jacket with full zip, moisture-wicking fabric for active lifestyle.', 3, 5, 85.00, 1, NOW(), NOW()),
-- HUMMELS PRODUCTS (Brand ID: 4) - 5 products
('HM-SNEAKER-033', 'Hummel Stadil Light Canvas', 'Unisex canvas sneakers with retro design, lightweight and comfortable for everyday wear.', 4, 16, 65.00, 1, NOW(), NOW()),
('HM-WSHOES-034', 'Hummel Aerocharge Supreme', 'Women indoor court shoes with excellent grip, designed for handball and volleyball.', 4, 9, 88.00, 1, NOW(), NOW()),
('HM-WDRESS-035', 'Hummel Action Cotton Dress', 'Women casual dress with comfortable cotton fabric, sporty yet elegant design.', 4, 11, 55.00, 1, NOW(), NOW()),
('HM-BACKPACK-036', 'Hummel Core Sports Backpack', 'Unisex sports backpack with multiple pockets, water-resistant material for all activities.', 4, 17, 48.00, 1, NOW(), NOW()),
('HM-SHOES-037', 'Hummel Crosslite Dot', 'Men training shoes with lightweight construction, suitable for daily workouts.', 4, 4, 79.00, 1, NOW(), NOW()),
-- LOUIS VUITTON PRODUCTS (Brand ID: 5) - 5 products
('LV-BAG-038', 'Louis Vuitton Neverfull MM', 'Women iconic tote bag with spacious interior, reversible design for versatility.', 5, 12, 1900.00, 1, NOW(), NOW()),
('LV-WACC-039', 'Louis Vuitton Essential V Necklace', 'Women delicate necklace with V pendant, gold-tone finish for a luxurious look.', 5, 13, 550.00, 1, NOW(), NOW()),
('LV-WJACKET-040', 'Louis Vuitton Monogram Bomber', 'Women stylish bomber jacket with monogram pattern, silk lining for comfort.', 5, 10, 3700.00, 1, NOW(), NOW()),
('LV-WSHOES-041', 'Louis Vuitton Laureate Platform Boot', 'Women platform ankle boots with canvas and calf leather, statement design.', 5, 9, 1650.00, 1, NOW(), NOW()),
('LV-BACKPACK-042', 'Louis Vuitton Keepall Bandoulière', 'Unisex travel bag with shoulder strap, iconic design for luxury travel experience.', 5, 17, 2200.00, 1, NOW(), NOW());


-- =============================================
-- 6. Product Variants (Total 12 + 22 + 50 = 84 records)
-- =============================================

-- Initial 6 products (12 variants)
INSERT INTO product_variants (product_id, sku, attribute_json, price, stock, created_at, updated_at)
VALUES
-- Nike Air Zoom Pegasus 40 (ID 1)
(1, 'NK-AIR-001-BLK-41', JSON_OBJECT('size', '41', 'color', 'Black'), 2990000, 20, NOW(), NOW()),
(1, 'NK-AIR-001-WHT-42', JSON_OBJECT('size', '42', 'color', 'White'), 2990000, 15, NOW(), NOW()),
-- Nike Dunk Low Retro (ID 2)
(2, 'NK-DUNK-002-BLU-42', JSON_OBJECT('size', '42', 'color', 'Blue'), 2590000, 10, NOW(), NOW()),
(2, 'NK-DUNK-002-RED-43', JSON_OBJECT('size', '43', 'color', 'Red'), 2590000, 12, NOW(), NOW()),
-- Adidas Ultraboost 23 (ID 3)
(3, 'AD-ULTRA-003-BLK-41', JSON_OBJECT('size', '41', 'color', 'Black'), 3290000, 18, NOW(), NOW()),
(3, 'AD-ULTRA-003-WHT-42', JSON_OBJECT('size', '42', 'color', 'White'), 3290000, 14, NOW(), NOW()),
-- Adidas Stan Smith (ID 4)
(4, 'AD-STAN-004-GRN-41', JSON_OBJECT('size', '41', 'color', 'Green'), 2190000, 25, NOW(), NOW()),
(4, 'AD-STAN-004-WHT-42', JSON_OBJECT('size', '42', 'color', 'White'), 2190000, 30, NOW(), NOW()),
-- Puma RS-X3 Puzzle (ID 5)
(5, 'PM-RSX-005-MULTI-41', JSON_OBJECT('size', '41', 'color', 'Multicolor'), 2890000, 20, NOW(), NOW()),
(5, 'PM-RSX-005-BLK-42', JSON_OBJECT('size', '42', 'color', 'Black'), 2890000, 15, NOW(), NOW()),
-- Puma Suede Classic (ID 6)
(6, 'PM-SUEDE-006-GRY-41', JSON_OBJECT('size', '41', 'color', 'Gray'), 1990000, 25, NOW(), NOW()),
(6, 'PM-SUEDE-006-NAVY-42', JSON_OBJECT('size', '42', 'color', 'Navy'), 1990000, 22, NOW(), NOW());

-- Man Apparel (22 variants)
INSERT INTO product_variants (product_id, sku, attribute_json, price, stock, created_at, updated_at)
VALUES
-- 7: Nike Essential Heavy T-Shirt Black
(7, 'NK-TSHIRT-001-BLK-M', JSON_OBJECT('size', 'M', 'color', 'Black'), 599000, 20, NOW(), NOW()),
(7, 'NK-TSHIRT-001-BLK-L', JSON_OBJECT('size', 'L', 'color', 'Black'), 599000, 15, NOW(), NOW()),
-- 8: Adidas Relaxed Fit Tee
(8, 'AD-TSHIRT-002-CRM-M', JSON_OBJECT('size', 'M', 'color', 'Cream'), 629000, 22, NOW(), NOW()),
(8, 'AD-TSHIRT-002-CRM-L', JSON_OBJECT('size', 'L', 'color', 'Cream'), 629000, 18, NOW(), NOW()),
-- 9: Puma Graphic Marni Tee
(9, 'PM-TSHIRT-003-WHT-M', JSON_OBJECT('size', 'M', 'color', 'White'), 679000, 15, NOW(), NOW()),
(9, 'PM-TSHIRT-003-WHT-L', JSON_OBJECT('size', 'L', 'color', 'White'), 679000, 12, NOW(), NOW()),
-- 10: Nike Classic Oxford Shirt
(10, 'NK-SHIRT-004-WHT-M', JSON_OBJECT('size', 'M', 'color', 'White'), 749000, 18, NOW(), NOW()),
(10, 'NK-SHIRT-004-WHT-L', JSON_OBJECT('size', 'L', 'color', 'White'), 749000, 15, NOW(), NOW()),
-- 11: Adidas UNESCO Graphic Tee
(11, 'AD-TSHIRT-005-BLU-M', JSON_OBJECT('size', 'M', 'color', 'Blue'), 589000, 24, NOW(), NOW()),
(11, 'AD-TSHIRT-005-BLU-L', JSON_OBJECT('size', 'L', 'color', 'Blue'), 589000, 20, NOW(), NOW()),
-- 12: Nike Outdoor Utility Pants
(12, 'NK-PANTS-001-BRN-30', JSON_OBJECT('size', '30', 'color', 'Brown'), 899000, 15, NOW(), NOW()),
(12, 'NK-PANTS-001-BRN-32', JSON_OBJECT('size', '32', 'color', 'Brown'), 899000, 12, NOW(), NOW()),
-- 13: Adidas Casual Chino Pants
(13, 'AD-CHINO-002-BE-30', JSON_OBJECT('size', '30', 'color', 'Beige'), 799000, 18, NOW(), NOW()),
(13, 'AD-CHINO-002-BE-32', JSON_OBJECT('size', '32', 'color', 'Beige'), 799000, 14, NOW(), NOW()),
-- 14: Puma Tech Jogger Black
(14, 'PM-JOGGER-003-BLK-M', JSON_OBJECT('size', 'M', 'color', 'Black'), 759000, 20, NOW(), NOW()),
(14, 'PM-JOGGER-003-BLK-L', JSON_OBJECT('size', 'L', 'color', 'Black'), 759000, 16, NOW(), NOW()),
-- 15: Nike Urban Cargo Pants
(15, 'NK-PANTS-004-OLV-M', JSON_OBJECT('size', 'M', 'color', 'Olive'), 829000, 15, NOW(), NOW()),
(15, 'NK-PANTS-004-OLV-L', JSON_OBJECT('size', 'L', 'color', 'Olive'), 829000, 12, NOW(), NOW()),
-- 16: Adidas Everyday Slim Pants
(16, 'AD-PANTS-005-GRY-30', JSON_OBJECT('size', '30', 'color', 'Grey'), 769000, 17, NOW(), NOW()),
(16, 'AD-PANTS-005-GRY-32', JSON_OBJECT('size', '32', 'color', 'Grey'), 769000, 13, NOW(), NOW()),
-- 17: Puma Summer Casual Shorts
(17, 'PM-SHORT-006-NAVY-M', JSON_OBJECT('size', 'M', 'color', 'Navy'), 559000, 25, NOW(), NOW()),
(17, 'PM-SHORT-006-NAVY-L', JSON_OBJECT('size', 'L', 'color', 'Navy'), 559000, 18, NOW(), NOW());

-- Other products (50 variants)
INSERT INTO product_variants (product_id, sku, attribute_json, price, stock, created_at, updated_at)
VALUES
-- 18: Nike Air Max 270 React ($150.00)
(18, 'NK-SHOES-018-BLK-42', JSON_OBJECT('size', '42', 'color', 'Black'), 150.00, 18, NOW(), NOW()),
(18, 'NK-SHOES-018-BLK-43', JSON_OBJECT('size', '43', 'color', 'Black'), 150.00, 15, NOW(), NOW()),
-- 19: Nike Windrunner Jacket ($95.00)
(19, 'NK-JACKET-019-BLU-M', JSON_OBJECT('size', 'M', 'color', 'Blue'), 95.00, 20, NOW(), NOW()),
(19, 'NK-JACKET-019-BLU-L', JSON_OBJECT('size', 'L', 'color', 'Blue'), 95.00, 16, NOW(), NOW()),
(19, 'NK-JACKET-019-BLU-XL', JSON_OBJECT('size', 'XL', 'color', 'Blue'), 95.00, 20, NOW(), NOW()),
-- 20: Nike Dri-FIT Headband ($15.00)
(20, 'NK-ACC-020-BLK-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Black'), 15.00, 50, NOW(), NOW()),
(20, 'NK-ACC-020-WHT-OS', JSON_OBJECT('size', 'OneSize', 'color', 'White'), 15.00, 45, NOW(), NOW()),
-- 21: Nike Court Vision Low ($80.00)
(21, 'NK-SNEAKER-021-WHT-40', JSON_OBJECT('size', '40', 'color', 'White'), 80.00, 30, NOW(), NOW()),
(21, 'NK-SNEAKER-021-WHT-41', JSON_OBJECT('size', '41', 'color', 'White'), 80.00, 25, NOW(), NOW()),
-- 22: Nike Sportswear Heritage86 ($30.00)
(22, 'NK-HAT-022-BLK-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Black'), 30.00, 40, NOW(), NOW()),
(22, 'NK-HAT-022-RED-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Red'), 30.00, 35, NOW(), NOW()),
-- 23: Adidas Cloudfoam Pure 2.0 ($65.00)
(23, 'AD-WSHOES-023-PNK-38', JSON_OBJECT('size', '38', 'color', 'Pink'), 65.00, 25, NOW(), NOW()),
(23, 'AD-WSHOES-023-PNK-39', JSON_OBJECT('size', '39', 'color', 'Pink'), 65.00, 20, NOW(), NOW()),
-- 24: Adidas Shopper Bag Large ($40.00)
(24, 'AD-WBAG-024-RED-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Red'), 40.00, 30, NOW(), NOW()),
(24, 'AD-WBAG-024-BLK-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Black'), 40.00, 28, NOW(), NOW()),
-- 25: Adidas Track Jacket Essentials ($110.00)
(25, 'AD-WJACKET-025-WHT-M', JSON_OBJECT('size', 'M', 'color', 'White'), 110.00, 18, NOW(), NOW()),
(25, 'AD-WJACKET-025-WHT-L', JSON_OBJECT('size', 'L', 'color', 'White'), 110.00, 14, NOW(), NOW()),
(25, 'AD-WJACKET-025-WHT-XL', JSON_OBJECT('size', 'XL', 'color', 'White'), 110.00, 14, NOW(), NOW()),
-- 26: Adidas Linear Core Duffel ($45.00)
(26, 'AD-BAG-026-BLK-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Black'), 45.00, 25, NOW(), NOW()),
(26, 'AD-BAG-026-BLU-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Blue'), 45.00, 22, NOW(), NOW()),
-- 27: Adidas Classic Badge of Sport ($55.00)
(27, 'AD-BACKPACK-027-BLK-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Black'), 55.00, 30, NOW(), NOW()),
(27, 'AD-BACKPACK-027-GRY-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Grey'), 55.00, 28, NOW(), NOW()),
-- 28: Puma Training Fitness Gloves ($25.00)
(28, 'PM-WACC-028-BLK-M', JSON_OBJECT('size', 'M', 'color', 'Black'), 25.00, 40, NOW(), NOW()),
(28, 'PM-WACC-028-BLK-L', JSON_OBJECT('size', 'L', 'color', 'Black'), 25.00, 35, NOW(), NOW()),
(28, 'PM-WACC-028-WHT-M', JSON_OBJECT('size', 'M', 'color', 'White'), 25.00, 40, NOW(), NOW()),
(28, 'PM-WACC-028-WHT-L', JSON_OBJECT('size', 'L', 'color', 'White'), 25.00, 35, NOW(), NOW()),
-- 29: Puma Essential Padded Jacket ($89.00)
(29, 'PM-UJACKET-029-RED-S', JSON_OBJECT('size', 'S', 'color', 'Red'), 89.00, 15, NOW(), NOW()),
(29, 'PM-UJACKET-029-RED-M', JSON_OBJECT('size', 'M', 'color', 'Red'), 89.00, 12, NOW(), NOW()),
-- 30: Puma Archive Logo Cap ($28.00)
(30, 'PM-HAT-030-BLU-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Blue'), 28.00, 30, NOW(), NOW()),
(30, 'PM-HAT-030-GRN-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Green'), 28.00, 25, NOW(), NOW()),
-- 31: Puma Tazon 6 FM ($70.00)
(31, 'PM-SHOES-031-BLK-42', JSON_OBJECT('size', '42', 'color', 'Black'), 70.00, 20, NOW(), NOW()),
(31, 'PM-SHOES-031-BLK-43', JSON_OBJECT('size', '43', 'color', 'Black'), 70.00, 18, NOW(), NOW()),
-- 32: Puma Evostripe Hooded Jacket ($85.00)
(32, 'PM-JACKET-032-GRY-M', JSON_OBJECT('size', 'M', 'color', 'Grey'), 85.00, 22, NOW(), NOW()),
(32, 'PM-JACKET-032-GRY-L', JSON_OBJECT('size', 'L', 'color', 'Grey'), 85.00, 18, NOW(), NOW()),
(32, 'PM-JACKET-032-GRY-XL', JSON_OBJECT('size', 'XL', 'color', 'Grey'), 85.00, 18, NOW(), NOW()),
-- 33: Hummel Stadil Light Canvas ($65.00)
(33, 'HM-SNEAKER-033-BLU-40', JSON_OBJECT('size', '40', 'color', 'Blue'), 65.00, 25, NOW(), NOW()),
(33, 'HM-SNEAKER-033-BLU-41', JSON_OBJECT('size', '41', 'color', 'Blue'), 65.00, 25, NOW(), NOW()),
(33, 'HM-SNEAKER-033-BLU-42', JSON_OBJECT('size', '42', 'color', 'Blue'), 65.00, 20, NOW(), NOW()),
(33, 'HM-SNEAKER-033-BLK-40', JSON_OBJECT('size', '40', 'color', 'Black'), 65.00, 25, NOW(), NOW()),
(33, 'HM-SNEAKER-033-BLK-41', JSON_OBJECT('size', '41', 'color', 'Black'), 65.00, 25, NOW(), NOW()),
(33, 'HM-SNEAKER-033-BLK-42', JSON_OBJECT('size', '42', 'color', 'Black'), 65.00, 20, NOW(), NOW()),
-- 34: Hummel Aerocharge Supreme ($88.00)
(34, 'HM-WSHOES-034-PNK-37', JSON_OBJECT('size', '37', 'color', 'Pink'), 88.00, 18, NOW(), NOW()),
(34, 'HM-WSHOES-034-PNK-38', JSON_OBJECT('size', '38', 'color', 'Pink'), 88.00, 15, NOW(), NOW()),
(34, 'HM-WSHOES-034-PNK-39', JSON_OBJECT('size', '39', 'color', 'Pink'), 88.00, 15, NOW(), NOW()),
-- 35: Hummel Action Cotton Dress ($55.00)
(35, 'HM-WDRESS-035-BLU-S', JSON_OBJECT('size', 'S', 'color', 'Blue'), 55.00, 20, NOW(), NOW()),
(35, 'HM-WDRESS-035-BLU-M', JSON_OBJECT('size', 'M', 'color', 'Blue'), 55.00, 18, NOW(), NOW()),
-- 36: Hummel Core Sports Backpack ($48.00)
(36, 'HM-BACKPACK-036-RED-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Red'), 48.00, 30, NOW(), NOW()),
(36, 'HM-BACKPACK-036-BLK-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Black'), 48.00, 25, NOW(), NOW()),
-- 37: Hummel Crosslite Dot ($79.00)
(37, 'HM-SHOES-037-PUR-42', JSON_OBJECT('size', '42', 'color', 'Purple'), 79.00, 18, NOW(), NOW()),
(37, 'HM-SHOES-037-PUR-43', JSON_OBJECT('size', '43', 'color', 'Purple'), 79.00, 16, NOW(), NOW()),
-- 38: Louis Vuitton Neverfull MM ($1900.00)
(38, 'LV-BAG-038-MON-BRN', JSON_OBJECT('size', 'OneSize', 'color', 'Brown'), 1900.00, 8, NOW(), NOW()),
(38, 'LV-BAG-038-DAM-WHT', JSON_OBJECT('size', 'OneSize', 'color', 'White'), 1900.00, 6, NOW(), NOW()),
-- 39: Louis Vuitton Essential V Necklace ($550.00)
(39, 'LV-WACC-039-GLD-YLW', JSON_OBJECT('size', 'OneSize', 'color', 'Yellow'), 550.00, 15, NOW(), NOW()),
(39, 'LV-WACC-039-SLV-WHT', JSON_OBJECT('size', 'OneSize', 'color', 'White'), 550.00, 12, NOW(), NOW()),
-- 40: Louis Vuitton Monogram Bomber ($3700.00)
(40, 'LV-WJACKET-040-PUR-S', JSON_OBJECT('size', 'S', 'color', 'Purple'), 3700.00, 5, NOW(), NOW()),
(40, 'LV-WJACKET-040-PUR-M', JSON_OBJECT('size', 'M', 'color', 'Purple'), 3700.00, 4, NOW(), NOW()),
(40, 'LV-WJACKET-040-PUR-L', JSON_OBJECT('size', 'L', 'color', 'Purple'), 3700.00, 4, NOW(), NOW()),
(40, 'LV-WJACKET-040-BLK-S', JSON_OBJECT('size', 'S', 'color', 'Black'), 3700.00, 5, NOW(), NOW()),
(40, 'LV-WJACKET-040-BLK-M', JSON_OBJECT('size', 'M', 'color', 'Black'), 3700.00, 4, NOW(), NOW()),
(40, 'LV-WJACKET-040-BLK-L', JSON_OBJECT('size', 'L', 'color', 'Black'), 3700.00, 4, NOW(), NOW()),
-- 41: Louis Vuitton Laureate Platform Boot ($1650.00)
(41, 'LV-WSHOES-041-BRN-37', JSON_OBJECT('size', '37', 'color', 'Brown'), 1650.00, 10, NOW(), NOW()),
(41, 'LV-WSHOES-041-BRN-38', JSON_OBJECT('size', '38', 'color', 'Brown'), 1650.00, 8, NOW(), NOW()),
(41, 'LV-WSHOES-041-BRN-39', JSON_OBJECT('size', '39', 'color', 'Brown'), 1650.00, 8, NOW(), NOW()),
(41, 'LV-WSHOES-041-BLK-37', JSON_OBJECT('size', '37', 'color', 'Black'), 1650.00, 10, NOW(), NOW()),
(41, 'LV-WSHOES-041-BLK-38', JSON_OBJECT('size', '38', 'color', 'Black'), 1650.00, 8, NOW(), NOW()),
(41, 'LV-WSHOES-041-BLK-39', JSON_OBJECT('size', '39', 'color', 'Black'), 1650.00, 8, NOW(), NOW()),
-- 42: Louis Vuitton Keepall Bandoulière ($2200.00)
(42, 'LV-BACKPACK-042-MON-WHT', JSON_OBJECT('size', 'OneSize', 'color', 'White'), 2200.00, 5, NOW(), NOW()),
(42, 'LV-BACKPACK-042-BRN-OS', JSON_OBJECT('size', 'OneSize', 'color', 'Brown'), 2200.00, 5, NOW(), NOW());


-- =============================================
-- 7. Product Images (Total 12 + 22 + 50 = 84 records)
-- =============================================

-- Images for initial 6 products (12 records)
INSERT INTO product_images (product_id, url, orders, is_primary)
VALUES
-- Nike Air Zoom Pegasus 40 (ID 1)
(1, 'https://ash.vn/cdn/shop/files/AURORA_FD2722-002_PHSRH001-2000_1800x.jpg?v=1752137613', 1, TRUE),
(1, 'https://ash.vn/cdn/shop/files/AURORA_FD2722-002_PHSRH001-2000_1800x.jpg?v=1752137613', 2, FALSE),
-- Nike Dunk Low Retro (ID 2)
(2, 'https://static.nike.com/a/images/t_web_pdp_936_v2/f_auto/41f5c226-fb8d-40b2-ab37-9929f3bd4590/NIKE+DUNK+LOW+RETRO+SE.png', 1, TRUE),
(2, 'https://static.nike.com/a/images/t_web_pdp_936_v2/f_auto/41f5c226-fb8d-40b2-ab37-9929f3bd4590/NIKE+DUNK+LOW+RETRO+SE.png', 2, FALSE),
-- Adidas Ultraboost 23 (ID 3)
(3, 'https://authentic-shoes.com/wp-content/uploads/2023/10/Giay_Ultraboost_23_Ngoc_lam_IE16-transformed.png', 1, TRUE),
(3, 'https://authentic-shoes.com/wp-content/uploads/2023/10/Giay_Ultraboost_23_Ngoc_lam_IE16-transformed.png3-2.jpg', 2, FALSE),
-- Adidas Stan Smith (ID 4)
(4, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/f6bfb2c064a64c498e57af1700593332_9366/Giay_Stan_Smith_Lux_trang_HQ6785_HM1.jpg', 1, TRUE),
(4, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/f6bfb2c064a64c498e57af1700593332_9366/Giay_Stan_Smith_Lux_trang_HQ6785_HM1.jpg', 2, FALSE),
-- Puma RS-X3 Puzzle (ID 5)
(5, 'https://thesneakerhouse.com/wp-content/uploads/2020/12/Puma-RS-X%C2%B3-Super-Red-White-Men-372884-01-2.jpg', 1, TRUE),
(5, 'https://thesneakerhouse.com/wp-content/uploads/2020/12/Puma-RS-X%C2%B3-Super-Red-White-Men-372884-01-2.jpg', 2, FALSE),
-- Puma Suede Classic (ID 6)
(6, 'https://thesneakerhouse.com/wp-content/uploads/2020/12/Puma-Suede-Classic-Navy-White-1.jpg', 1, TRUE),
(6, 'https://thesneakerhouse.com/wp-content/uploads/2020/12/Puma-Suede-Classic-Navy-White-1.jpg', 2, FALSE);

-- Images for Man Apparel (ID 7-17) - (22 records)
INSERT INTO product_images (product_id, url, orders, is_primary)
VALUES
-- 7: Nike Essential Heavy T-Shirt
(7, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/28399e52-b883-4929-87c1-0730b201463a/NIKE+ESSENTIAL+HEAVY+T-SHIRT.png', 1, TRUE),
(7, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/28399e52-b883-4929-87c1-0730b201463a/NIKE+ESSENTIAL+HEAVY+T-SHIRT.png', 2, FALSE),
-- 8: Adidas Relaxed Fit Tee
(8, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/81f964a51e504c3f8730af170058b87c_9366/Ao_thun_tay_ngan_Regular_trang_HN0351_21_model.jpg', 1, TRUE),
(8, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/81f964a51e504c3f8730af170058b87c_9366/Ao_thun_tay_ngan_Regular_trang_HN0351_21_model.jpg', 2, FALSE),
-- 9: Puma Graphic Marni Tee
(9, 'https://vn-live-05.slatic.net/p/e2d03b0c2794c4839843a8532f053e19.jpg_720x720q80.jpg', 1, TRUE),
(9, 'https://vn-live-05.slatic.net/p/e2d03b0c2794c4839843a8532f053e19.jpg_720x720q80.jpg', 2, FALSE),
-- 10: Nike Classic Oxford Shirt
(10, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/1a8848d7-550b-46e3-ae16-373307b22d1f/AO+SOMI+DAI+TAY+NAM.png', 1, TRUE),
(10, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/1a8848d7-550b-46e3-ae16-373307b22d1f/AO+SOMI+DAI+TAY+NAM.png', 2, FALSE),
-- 11: Adidas UNESCO Graphic Tee
(11, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/a3a3d5e9d97a4a28b7e2af27000e700a_9366/Ao_thun_UNESCO_Graphic_trang_II3183_21_model.jpg', 1, TRUE),
(11, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/a3a3d5e9d97a4a28b7e2af27000e700a_9366/Ao_thun_UNESCO_Graphic_trang_II3183_21_model.jpg', 2, FALSE),
-- 12: Nike Outdoor Utility Pants
(12, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/5c3311f4-90a4-4a5f-998f-a9572620a23d/QUAN+DAI+NAM.png', 1, TRUE),
(12, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/5c3311f4-90a4-4a5f-998f-a9572620a23d/QUAN+DAI+NAM.png', 2, FALSE),
-- 13: Adidas Casual Chino Pants
(13, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/c69e2c608b68421c8b32af2c009d7881_9366/Quần_Dài_Nam_Essentials_trắng_HM1820_21_model.jpg', 1, TRUE),
(13, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/c69e2c608b68421c8b32af2c009d7881_9366/Quần_Dài_Nam_Essentials_trắng_HM1820_21_model.jpg', 2, FALSE),
-- 14: Puma Tech Jogger Black
(14, 'https://www.sportizmo.rs/img/l/1026/puma-evostripe-pants-tr-8-2-126284.jpg', 1, TRUE),
(14, 'https://www.sportizmo.rs/img/l/1026/puma-evostripe-pants-tr-8-2-126284.jpg', 2, FALSE),
-- 15: Nike Urban Cargo Pants
(15, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/148e64c2-6734-4536-a337-3729e2f93339/QUAN+DAI+NAM.png', 1, TRUE),
(15, 'https://static.nike.com/a/images/t_PDP_1728_v1/f_auto,q_auto:eco/148e64c2-6734-4536-a337-3729e2f93339/QUAN+DAI+NAM.png', 2, FALSE),
-- 16: Adidas Everyday Slim Pants
(16, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/05b1f802d2014022a106af08003612d7_9366/QUAN+DAI+NAM.jpg', 1, TRUE),
(16, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/05b1f802d2014022a106af08003612d7_9366/QUAN+DAI+NAM.jpg', 2, FALSE),
-- 17: Puma Summer Casual Shorts
(17, 'https://img.cdn.91app.hk/webapi/imagesV3/Cropped/SalePage/537991/2/638977783983470000?v=1', 1, TRUE),
(17, 'https://img.cdn.91app.hk/webapi/imagesV3/Cropped/SalePage/537991/2/638977783983470000?v=1', 2, FALSE);

-- Images for other products (ID 18-42) - (50 records)
INSERT INTO product_images (product_id, url, orders, is_primary)
VALUES
-- 18: Nike Air Max 270 React
(18, 'https://static.nike.com/a/images/t_web_pdp_936_v2/f_auto/gorfwjchoasrrzr1fggt/AIR+MAX+270.png', 1, TRUE),
(18, 'https://static.nike.com/a/images/w_1280,q_auto,f_auto/44913ad2-505b-46f9-b0f8-436e0a8cfb1b/air-max-270-react-eng-laser-bluewhite-release-date.jpg', 2, FALSE),
-- 19: Nike Windrunner Jacket
(19, 'https://static.nike.com/a/images/t_web_pdp_535_v2/f_auto/f4ffab54-5ce7-4654-a885-fc5207e7dcab/KOR+M+NK+LW+WVN+WR+JKT+OLYB.png', 1, TRUE),
(19, 'https://static.nike.com/a/images/t_web_pdp_535_v2/f_auto/29e85ba3-6a44-420a-8bf9-48573610246e/KOR+M+NK+LW+WVN+WR+JKT+OLYB.png', 2, FALSE),
-- 20: Nike Dri-FIT Headband
(20, 'https://static.nike.com/a/images/t_web_pdp_535_v2/f_auto/19361c24-e2cb-41ad-bfb2-ba649b1eca0a/NIKE+HEADBAND+NBA.png', 1, TRUE),
(20, 'https://static.nike.com/a/images/t_web_pdp_535_v2/f_auto/edf1ea5f-4379-4124-9048-96ce3af07482/NIKE+HEADBAND+NBA.png', 2, FALSE),
-- 21: Nike Court Vision Low
(21, 'https://static.nike.com/a/images/t_web_pdp_535_v2/f_auto/44f222ab-96b6-43b9-82e7-9a1bd888611d/NIKE+COURT+VISION+LO.png', 1, TRUE),
(21, 'https://static.nike.com/a/images/t_web_pdp_535_v2/f_auto/c49706ab-570c-4b86-ae6e-f542af305ebd/NIKE+COURT+VISION+LO.png', 2, FALSE),
-- 22: Nike Sportswear Heritage86
(22, 'https://supersports.com.vn/cdn/shop/files/FB5360-010-PHSFM001-2000.jpg?v=1711209322', 1, TRUE),
(22, 'https://supersports.com.vn/cdn/shop/files/FB5360-010-PHSFM001-2000.jpg?v=1711209322', 2, FALSE),
-- 23: Adidas Cloudfoam Pure 2.0
(23, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/210b64d0234a49c6806eaf4200155b1f_9366/Giay_Cloudfoam_Pure_2.0_xanh_nu_GW9983_HM1.jpg', 1, TRUE),
(23, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/210b64d0234a49c6806eaf4200155b1f_9366/Giay_Cloudfoam_Pure_2.0_xanh_nu_GW9983_HM1.jpg', 2, FALSE),
-- 24: Adidas Shopper Bag Large
(24, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/a02095f9c9b74301a711af7700a402f0_9366/Tui_xach_Essentials_xanh_nu_IS1915_01_standard.jpg', 1, TRUE),
(24, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/a02095f9c9b74301a711af7700a402f0_9366/Tui_xach_Essentials_xanh_nu_IS1915_01_standard.jpg', 2, FALSE),
-- 25: Adidas Track Jacket Essentials
(25, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/709f187d9f754779a5e8af280041a7d1_9366/AO+KHOAC+NU.jpg', 1, TRUE),
(25, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/709f187d9f754779a5e8af280041a7d1_9366/AO+KHOAC+NU.jpg', 2, FALSE),
-- 26: Adidas Linear Core Duffel
(26, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/f44b2569c7cc47779d71af4a00af1828_9366/TUI+TRONG+DUFFEL.jpg', 1, TRUE),
(26, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/f44b2569c7cc47779d71af4a00af1828_9366/TUI+TRONG+DUFFEL.jpg', 2, FALSE),
-- 27: Adidas Classic Badge of Sport
(27, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/272f385c7f1a4e528185af2a0067645d_9366/Ba_lo_Classic_Badge_of_Sport_trang_IL5799_01_standard.jpg', 1, TRUE),
(27, 'https://assets.adidas.com/images/w_600,f_auto,q_auto/272f385c7f1a4e528185af2a0067645d_9366/Ba_lo_Classic_Badge_of_Sport_trang_IL5799_01_standard.jpg', 2, FALSE),
-- 28: Puma Training Fitness Gloves
(28, 'https://m.media-amazon.com/images/I/718yG67U8WL.jpg', 1, TRUE),
(28, 'https://m.media-amazon.com/images/I/718yG67U8WL.jpg', 2, FALSE),
-- 29: Puma Essential Padded Jacket
(29, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/675971/01/mod01/fnd/PNA/w/1000/h/1000', 1, TRUE),
(29, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/675971/01/mod01/fnd/PNA/w/1000/h/1000', 2, FALSE),
-- 30: Puma Archive Logo Cap
(30, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/021946/01/mod01/fnd/PNA/w/1000/h/1000', 1, TRUE),
(30, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/021946/01/mod01/fnd/PNA/w/1000/h/1000', 2, FALSE),
-- 31: Puma Tazon 6 FM
(31, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/193231/01/sv01/fnd/PNA/w/1000/h/1000', 1, TRUE),
(31, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/193231/01/sv01/fnd/PNA/w/1000/h/1000', 2, FALSE),
-- 32: Puma Evostripe Hooded Jacket
(32, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/847413/01/mod01/fnd/PNA/w/1000/h/1000', 1, TRUE),
(32, 'https://images.puma.com/image/upload/f_auto,q_auto,b_rgb:fafafa/global/847413/01/mod01/fnd/PNA/w/1000/h/1000', 2, FALSE),
-- 33: Hummel Stadil Light Canvas
(33, 'https://cdn.myshoptet.com/usr/www.sportisimo.cz/user/shop/big/1023774-0_hummel-stadil-light-canvas.jpg?5c34e8f1', 1, TRUE),
(33, 'https://cdn.myshoptet.com/usr/www.sportisimo.cz/user/shop/big/1023774-0_hummel-stadil-light-canvas.jpg?5c34e8f1', 2, FALSE),
-- 34: Hummel Aerocharge Supreme
(34, 'https://hummel.co.uk/media/catalog/product/h/u/hummel-aerocharge-supreme-knit-black-orange-207019-2049-image-02.jpg', 1, TRUE),
(34, 'https://hummel.co.uk/media/catalog/product/h/u/hummel-aerocharge-supreme-knit-black-orange-207019-2049-image-02.jpg', 2, FALSE),
-- 35: Hummel Action Cotton Dress
(35, 'https://hummel.co.uk/media/catalog/product/h/u/hummel-action-cotton-dress-dusty-blue-207019-7988-image-02.jpg', 1, TRUE),
(35, 'https://hummel.co.uk/media/catalog/product/h/u/hummel-action-cotton-dress-dusty-blue-207019-7988-image-02.jpg', 2, FALSE),
-- 36: Hummel Core Sports Backpack
(36, 'https://images.internetstores.de/products/1231362/01/3c1d53/hummel-core-sports-backpack-black.jpg?forceSize=true&maxWidth=600&maxHeight=600&size=600x600', 1, TRUE),
(36, 'https://images.internetstores.de/products/1231362/01/3c1d53/hummel-core-sports-backpack-black.jpg?forceSize=true&maxWidth=600&maxHeight=600&size=600x600', 2, FALSE),
-- 37: Hummel Crosslite Dot
(37, 'https://hummel.co.uk/media/catalog/product/h/u/hummel-crosslite-dot-purple-207019-5026-image-02.jpg', 1, TRUE),
(37, 'https://hummel.co.uk/media/catalog/product/h/u/hummel-crosslite-dot-purple-207019-5026-image-02.jpg', 2, FALSE),
-- 38: Louis Vuitton Neverfull MM
(38, 'https://vn.louisvuitton.com/images/is/image/lv/1/PP_VP_L/louis-vuitton-neverfull-mm--N40599_PM2_Front%20view.jpg', 1, TRUE),
(38, 'https://vn.louisvuitton.com/images/is/image/lv/1/PP_VP_L/louis-vuitton-neverfull-mm--N40604_PM2_Front%20view.jpg', 2, FALSE),
-- 39: Louis Vuitton Essential V Necklace
(39, 'https://i.ebayimg.com/images/g/6MMAAeSwN2RpIW3k/s-l1600.webp', 1, TRUE),
(39, 'https://i.ebayimg.com/images/g/e3cAAeSwKkFpJVhX/s-l1600.webp', 2, FALSE),
-- 40: Louis Vuitton Monogram Bomber
(40, 'https://vn.louisvuitton.com/images/is/image/lv/1/PP_VP_L/louis-vuitton-monogram-embossed-track-top--HSY99WZA86D8_PM2_Front%20view.jpg', 1, TRUE),
(40, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS62McJ249OjTWsDcCE-r1cqrKyS2Bn-1-FkQ&usqp=CAU', 2, FALSE),
-- 41: Louis Vuitton Laureate Platform Boot
(41, 'https://vn.louisvuitton.com/images/is/image/lv/1/PP_VP_L/louis-vuitton-laureate-platform-boot--ALWU2HLC6534_PM2_Front%20view.jpg', 1, TRUE),
(41, 'https://vn.louisvuitton.com/images/is/image/lv/1/PP_VP_L/louis-vuitton-laureate-platform-boot--ALWU2HLC6534_PM2_Front%20view.jpg', 2, FALSE),
-- 42: Louis Vuitton Keepall Bandoulière
(42, 'https://vn.louisvuitton.com/images/is/image/lv/1/PP_VP_L/louis-vuitton-keepall-bandouliere-50--M20513_PM2_Front%20view.jpg', 1, TRUE),
(42, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcS62McJ249OjTWsDcCE-r1cqrKyS2Bn-1-FkQ&usqp=CAU', 2, FALSE);

-- 1. Thêm cột color_variant vào bảng product_images
ALTER TABLE product_images
    ADD COLUMN color_variant VARCHAR(50) NULL AFTER url;

-- 2. Cập nhật ảnh cho các sản phẩm có nhiều màu
-- Nike Air Max 270 React (Product ID 18) - Black
UPDATE product_images SET color_variant = 'Black'
WHERE product_id = 18 AND id IN (
    SELECT id FROM (
                       SELECT id FROM product_images WHERE product_id = 18 ORDER BY orders LIMIT 2
                   ) tmp
);

-- Nike Windrunner Jacket (19) - Blue
UPDATE product_images SET color_variant = 'Blue' WHERE product_id = 19;

-- Nike Dri-FIT Headband (20)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 20 AND orders = 1;
UPDATE product_images SET color_variant = 'White' WHERE product_id = 20 AND orders = 2;

-- Nike Court Vision Low (21) - White
UPDATE product_images SET color_variant = 'White' WHERE product_id = 21;

-- Nike Heritage86 Cap (22)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 22 AND orders = 1;
UPDATE product_images SET color_variant = 'White' WHERE product_id = 22 AND orders = 2;

-- Adidas Cloudfoam Pure (23)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 23 AND orders = 1;
UPDATE product_images SET color_variant = 'Grey' WHERE product_id = 23 AND orders = 2;

-- Adidas Tennis Dress (24) - White
UPDATE product_images SET color_variant = 'White' WHERE product_id = 24;

-- Adidas Puffer Jacket (25)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 25 AND orders = 1;
UPDATE product_images SET color_variant = 'White' WHERE product_id = 25 AND orders = 2;

-- Adidas Duffel Bag (26)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 26 AND orders = 1;
UPDATE product_images SET color_variant = 'Blue' WHERE product_id = 26 AND orders = 2;

-- Adidas Backpack (27)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 27 AND orders = 1;
UPDATE product_images SET color_variant = 'Grey' WHERE product_id = 27 AND orders = 2;

-- Puma Training Gloves (28)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 28 AND orders = 1;
UPDATE product_images SET color_variant = 'White' WHERE product_id = 28 AND orders = 2;

-- Puma Padded Jacket (29)
UPDATE product_images SET color_variant = 'Green' WHERE product_id = 29 AND orders = 1;
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 29 AND orders = 2;

-- Puma Cap (30)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 30 AND orders = 1;
UPDATE product_images SET color_variant = 'Red' WHERE product_id = 30 AND orders = 2;

-- Puma Tazon 6 (31)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 31 AND orders = 1;
UPDATE product_images SET color_variant = 'White' WHERE product_id = 31 AND orders = 2;

-- Puma Evostripe Jacket (32)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 32 AND orders = 1;
UPDATE product_images SET color_variant = 'Grey' WHERE product_id = 32 AND orders = 2;

-- Hummel Stadil Sneakers (33)
UPDATE product_images SET color_variant = 'Blue' WHERE product_id = 33 AND orders = 1;
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 33 AND orders = 2;

-- Hummel Aerocharge (34)
UPDATE product_images SET color_variant = 'Pink' WHERE product_id = 34 AND orders = 1;
UPDATE product_images SET color_variant = 'White' WHERE product_id = 34 AND orders = 2;

-- Hummel Dress (35)
UPDATE product_images SET color_variant = 'Red' WHERE product_id = 35 AND orders = 1;
UPDATE product_images SET color_variant = 'White' WHERE product_id = 35 AND orders = 2;

-- Hummel Backpack (36)
UPDATE product_images SET color_variant = 'Red' WHERE product_id = 36 AND orders = 1;
UPDATE product_images SET color_variant = 'Blue' WHERE product_id = 36 AND orders = 2;

-- Hummel Crosslite (37)
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 37 AND orders = 1;
UPDATE product_images SET color_variant = 'Purple' WHERE product_id = 37 AND orders = 2;

-- Louis Vuitton Neverfull (38)
UPDATE product_images SET color_variant = 'Brown Monogram' WHERE product_id = 38 AND orders = 1;
UPDATE product_images SET color_variant = 'White Damier' WHERE product_id = 38 AND orders = 2;

-- LV Necklace (39)
UPDATE product_images SET color_variant = 'Gold' WHERE product_id = 39 AND orders = 1;
UPDATE product_images SET color_variant = 'Silver' WHERE product_id = 39 AND orders = 2;

-- LV Bomber Jacket (40)
UPDATE product_images SET color_variant = 'Purple' WHERE product_id = 40 AND orders = 1;
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 40 AND orders = 2;

-- LV Platform Boot (41)
UPDATE product_images SET color_variant = 'Brown' WHERE product_id = 41 AND orders = 1;
UPDATE product_images SET color_variant = 'Black' WHERE product_id = 41 AND orders = 2;

-- LV Keepall (42)
UPDATE product_images SET color_variant = 'Brown Monogram' WHERE product_id = 42 AND orders = 1;
UPDATE product_images SET color_variant = 'Graphite' WHERE product_id = 42 AND orders = 2;
