CREATE INDEX idx_catalog_provider_active
    ON catalog_items(provider_id, active);

CREATE INDEX idx_catalog_active_category
    ON catalog_items(active, category);