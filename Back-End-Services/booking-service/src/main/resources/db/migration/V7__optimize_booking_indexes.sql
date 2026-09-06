DROP INDEX IF EXISTS idx_service_requests_customer;

DROP INDEX IF EXISTS idx_service_requests_provider;

DROP INDEX IF EXISTS idx_service_requests_provider_status;

DROP INDEX IF EXISTS idx_service_requests_customer_status;


CREATE INDEX idx_service_requests_customer_created
    ON service_requests(customer_id, created_at DESC);


CREATE INDEX idx_service_requests_provider_created
    ON service_requests(provider_id, created_at DESC);


CREATE INDEX idx_service_requests_provider_status_created
    ON service_requests(provider_id, status, created_at DESC);