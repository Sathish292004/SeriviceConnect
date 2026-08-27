ALTER TABLE service_requests
    ADD COLUMN catalog_item_id BIGINT;

CREATE INDEX idx_service_requests_catalog_item
    ON service_requests(catalog_item_id);