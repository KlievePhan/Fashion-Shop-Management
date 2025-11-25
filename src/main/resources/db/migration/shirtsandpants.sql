-- 👕 MAN SHIRTS
INSERT INTO products (sku, title, description, brand_id, category_id, base_price, active, created_at, updated_at)
VALUES
    ('NK-TSHIRT-001', 'Nike Essential Heavy T-Shirt Black',
     'Mens t-shirt with loose fit, thick cotton material, breathable, suitable for everyday wear.',
     1, 6, 599000, 1, NOW(), NOW()),

    ('AD-TSHIRT-002', 'Adidas Natural Heavyweight Tee',
     'Men t-shirt in cream color, loose fit, heavy fabric, easy to match with jeans or joggers.',
     2, 6, 629000, 1, NOW(), NOW()),

    ('PM-TSHIRT-003', 'Puma Graphic Marni Tee',
     'Men T-shirt with fashionable prints, outstanding streetwear style.',
     3, 6, 679000, 1, NOW(), NOW()),

    ('NK-SHIRT-004', 'Nike Classic Oxford Shirt',
     'Men shirt with collar, cool cotton material, suitable for work or going out.',
     1, 6, 749000, 1, NOW(), NOW()),

    ('AD-TSHIRT-005', 'Adidas UNESCO Graphic Tee',
     'Men t-shirt with graphic print, basic form, easy to wear, suitable for everyday casual style.',
     2, 6, 589000, 1, NOW(), NOW());

-- 👖 MAN PANTS
INSERT INTO products (sku, title, description, brand_id, category_id, base_price, active, created_at, updated_at)
VALUES
    ('NK-PANTS-001', 'Nike Outdoor Utility Pants',
     'Quần dài nam dáng slim, chất liệu chống nhăn, phù hợp hoạt động ngoài trời.',
     1, 7, 899000, 1, NOW(), NOW()),

    ('AD-CHINO-002', 'Adidas Casual Chino Pants',
     'Quần chino nam màu be, phom vừa, dễ phối áo thun hoặc sơ mi.',
     2, 7, 799000, 1, NOW(), NOW()),

    ('PM-JOGGER-003', 'Puma Tech Jogger Black',
     'Quần jogger nam chất vải co giãn, phù hợp tập luyện và mặc thường ngày.',
     3, 7, 759000, 1, NOW(), NOW()),

    ('NK-PANTS-004', 'Nike Urban Cargo Pants',
     'Quần cargo túi hộp, kiểu dáng hiện đại, phù hợp phong cách streetwear.',
     1, 7, 829000, 1, NOW(), NOW()),

    ('AD-PANTS-005', 'Adidas Everyday Slim Pants',
     'Quần dài nam dáng slim-fit, chất liệu mềm, mặc thoải mái cả ngày.',
     2, 7, 769000, 1, NOW(), NOW()),

    ('PM-SHORT-006', 'Puma Summer Casual Shorts',
     'Quần short nam thoáng mát, phù hợp đi chơi, dạo phố hoặc du lịch.',
     3, 7, 559000, 1, NOW(), NOW());
     
     
     -- VARIANTS CHO ÁO & QUẦN
INSERT INTO product_variants (product_id, sku, attribute_json, price, stock, created_at, updated_at)
VALUES
    -- 7: Nike Essential Heavy T-Shirt Black
    (7, 'NK-TSHIRT-001-BLK-M', JSON_OBJECT('size', 'M', 'color', 'Black'), 599000, 25, NOW(), NOW()),
    (7, 'NK-TSHIRT-001-BLK-L', JSON_OBJECT('size', 'L', 'color', 'Black'), 599000, 20, NOW(), NOW()),

    -- 8: Adidas Natural Heavyweight Tee
    (8, 'AD-TSHIRT-002-NAT-M', JSON_OBJECT('size', 'M', 'color', 'Natural'), 629000, 22, NOW(), NOW()),
    (8, 'AD-TSHIRT-002-NAT-L', JSON_OBJECT('size', 'L', 'color', 'Natural'), 629000, 18, NOW(), NOW()),

    -- 9: Puma Graphic Marni Tee
    (9, 'PM-TSHIRT-003-MULTI-M', JSON_OBJECT('size', 'M', 'color', 'Multicolor'), 679000, 20, NOW(), NOW()),
    (9, 'PM-TSHIRT-003-MULTI-L', JSON_OBJECT('size', 'L', 'color', 'Multicolor'), 679000, 16, NOW(), NOW()),

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
    (16, 'AD-PANTS-005-GRY-30', JSON_OBJECT('size', '30', 'color', 'Grey'), 769000, 18, NOW(), NOW()),
    (16, 'AD-PANTS-005-GRY-32', JSON_OBJECT('size', '32', 'color', 'Grey'), 769000, 14, NOW(), NOW()),

    -- 17: Puma Summer Casual Shorts
    (17, 'PM-SHORT-006-KHK-M', JSON_OBJECT('size', 'M', 'color', 'Khaki'), 559000, 25, NOW(), NOW()),
    (17, 'PM-SHORT-006-KHK-L', JSON_OBJECT('size', 'L', 'color', 'Khaki'), 559000, 20, NOW(), NOW());


INSERT INTO product_images (product_id, url, orders, is_primary)
VALUES
    -- 7: Nike Essential Heavy T-Shirt Black
    (7, 'https://img.sonofatailor.com/images/customizer/product/extra-heavy-cotton/ss/Black.jpg', 1, TRUE),
    (7, 'https://img.sonofatailor.com/images/customizer/product/extra-heavy-cotton/ss/Black.jpg', 2, FALSE),

    -- 8: Adidas Natural Heavyweight Tee
    (8, 'https://www.houseofblanks.com/cdn/shop/files/HeavyweightTshirt_Natural_01_1.jpg?v=1726516460&width=1920', 1, TRUE),
    (8, 'https://www.houseofblanks.com/cdn/shop/files/HeavyweightTshirt_Natural_01_1.jpg?v=1726516460&width=1920', 2, FALSE),

    -- 9: Puma Graphic Marni Tee
    (9, 'https://www.marni.com/on/demandware.static/-/Sites-marni-master-catalog/default/dw209d44fb/images/large/HUMU0287X0_UTC406_00W01_E.jpg', 1, TRUE),
    (9, 'https://www.marni.com/on/demandware.static/-/Sites-marni-master-catalog/default/dw209d44fb/images/large/HUMU0287X0_UTC406_00W01_E.jpg', 2, FALSE),

    -- 10: Nike Classic Oxford Shirt
    (10, 'https://dictionary.cambridge.org/images/thumb/shirt_noun_002_33400.jpg?version=6.0.64', 1, TRUE),
    (10, 'https://dictionary.cambridge.org/images/thumb/shirt_noun_002_33400.jpg?version=6.0.64', 2, FALSE),

    -- 11: Adidas UNESCO Graphic Tee
    (11, 'https://shop.unesco.org/cdn/shop/files/eshop-produits_t-shirt_04.jpg?v=1719243665&width=2048', 1, TRUE),
    (11, 'https://shop.unesco.org/cdn/shop/files/eshop-produits_t-shirt_04.jpg?v=1719243665&width=2048', 2, FALSE),

    -- 12: Nike Outdoor Utility Pants
    (12, 'https://outdoorvitals.com/cdn/shop/products/brownsatushopify.png?v=1701706579&width=1000', 1, TRUE),
    (12, 'https://outdoorvitals.com/cdn/shop/products/brownsatushopify.png?v=1701706579&width=1000', 2, FALSE),

    -- 13: Adidas Casual Chino Pants
    (13, 'https://www.untuckit.com/cdn/shop/files/12_PANT-FLATLAY_SP1-24_BGOLDSTEIN_2180_91178e67-9314-4a84-a9cd-2bba7177341f.jpg?height=1000&v=1759431666', 1, TRUE),
    (13, 'https://www.untuckit.com/cdn/shop/files/12_PANT-FLATLAY_SP1-24_BGOLDSTEIN_2180_91178e67-9314-4a84-a9cd-2bba7177341f.jpg?height=1000&v=1759431666', 2, FALSE),

    -- 14: Puma Tech Jogger Black
    (14, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_OaS1rInQt_0WCRCXHlDfSDPAH38DBMf4QQ&s', 1, TRUE),
    (14, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcR_OaS1rInQt_0WCRCXHlDfSDPAH38DBMf4QQ&s', 2, FALSE),

    -- 15: Nike Urban Cargo Pants
    (15, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQQdg1LxF9yy42FKfv6HT2Zj2CB0dBe7lwd5A&s', 1, TRUE),
    (15, 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQQdg1LxF9yy42FKfv6HT2Zj2CB0dBe7lwd5A&s', 2, FALSE),

    -- 16: Adidas Everyday Slim Pants
    (16, 'https://m.media-amazon.com/images/I/51ROK8iRSyL._AC_UY1000_.jpg', 1, TRUE),
    (16, 'https://m.media-amazon.com/images/I/51ROK8iRSyL._AC_UY1000_.jpg', 2, FALSE),

    -- 17: Puma Summer Casual Shorts
    (17, 'https://img.cdn.91app.hk/webapi/imagesV3/Cropped/SalePage/537991/2/638977783983470000?v=1', 1, TRUE),
    (17, 'https://img.cdn.91app.hk/webapi/imagesV3/Cropped/SalePage/537991/2/638977783983470000?v=1', 2, FALSE);
