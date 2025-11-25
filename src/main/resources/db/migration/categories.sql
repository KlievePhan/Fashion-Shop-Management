-- Top-level categories
INSERT INTO categories (name, slug, parent_id)
VALUES
    ('Man', 'man', NULL),
    ('Woman', 'woman', NULL),
    ('Unisex', 'unisex', NULL);

-- Subcategories under 'Man'-- Top-level categories
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

INSERT INTO categories (name, slug, parent_id)
VALUES
    ('Unisex Hat', 'unisex-hat', 3),
    ('Unisex Jacket', 'unisex-jacket', 3),
    ('Unisex Sneaker', 'unisex-sneaker', 3),
    ('Unisex Backpack', 'unisex-backpack', 3);
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

INSERT INTO categories (name, slug, parent_id)
VALUES
    ('Unisex Hat', 'unisex-hat', 3),
    ('Unisex Jacket', 'unisex-jacket', 3),
    ('Unisex Sneaker', 'unisex-sneaker', 3),
    ('Unisex Backpack', 'unisex-backpack', 3);