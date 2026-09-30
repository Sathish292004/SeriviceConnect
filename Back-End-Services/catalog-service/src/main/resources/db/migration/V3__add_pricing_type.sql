-- ============================================================
-- V3: Add Pricing Type to Catalog Items
-- Supports: FIXED, STARTING_FROM, QUOTE_REQUIRED
-- ============================================================

ALTER TABLE catalog_items ADD COLUMN IF NOT EXISTS pricing_type VARCHAR(30) DEFAULT 'FIXED';

-- Default existing repair services to STARTING_FROM
UPDATE catalog_items
SET pricing_type = 'STARTING_FROM'
WHERE name ILIKE '%repair%' OR name ILIKE '%screen%' OR name ILIKE '%battery%' OR name ILIKE '%service%';

-- Specific installation services remain FIXED
UPDATE catalog_items
SET pricing_type = 'FIXED'
WHERE name ILIKE '%installation%' OR name ILIKE '%Automation%';
