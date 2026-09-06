ALTER TABLE service_requests
    ADD COLUMN requested_start_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE service_requests
    ADD COLUMN requested_end_at TIMESTAMP WITH TIME ZONE;

CREATE INDEX idx_service_requests_provider_requested_start
    ON service_requests(provider_id, requested_start_at);