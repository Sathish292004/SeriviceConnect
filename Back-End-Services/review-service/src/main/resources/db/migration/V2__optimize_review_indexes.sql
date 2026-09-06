CREATE INDEX idx_review_provider_active
    ON reviews(provider_id, active);

CREATE INDEX idx_review_customer_active
    ON reviews(customer_id, active);

CREATE INDEX idx_review_active
    ON reviews(active);