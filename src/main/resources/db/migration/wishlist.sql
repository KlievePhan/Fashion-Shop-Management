-- Migration: Update wishlist table structure
-- Replace product_variant_id with selected_options_json

-- Step 1: Add new column
ALTER TABLE wish_list
    ADD COLUMN selected_options_json TEXT;

-- Step 2: (Optional) Migrate existing data if you have productVariant references
-- This copies the variant's attribute JSON to the new column
UPDATE wish_list wl
    INNER JOIN product_variant pv ON wl.product_variant_id = pv.id
    SET wl.selected_options_json = pv.attribute_json
WHERE wl.product_variant_id IS NOT NULL;

-- Step 3: Drop old column (after backing up data!)
ALTER TABLE wish_list
DROP COLUMN product_variant_id;

-- Step 4: Add index for faster lookups
CREATE INDEX idx_wishlist_options
    ON wish_list(user_id, product_id, selected_options_json(255));