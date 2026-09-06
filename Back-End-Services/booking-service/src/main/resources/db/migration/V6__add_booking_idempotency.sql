ALTER TABLE service_requests
    ADD COLUMN idempotency_key VARCHAR(100);

ALTER TABLE service_requests
    ADD COLUMN idempotency_fingerprint VARCHAR(64);

ALTER TABLE service_requests
    ADD CONSTRAINT chk_service_requests_idempotency_pair
        CHECK (
            (idempotency_key IS NULL AND idempotency_fingerprint IS NULL)
                OR
            (idempotency_key IS NOT NULL AND idempotency_fingerprint IS NOT NULL)
            );

CREATE UNIQUE INDEX uq_service_requests_customer_idempotency
    ON service_requests(customer_id, idempotency_key)
    WHERE idempotency_key IS NOT NULL;

COMMENT ON COLUMN service_requests.idempotency_key IS
    'Client supplied idempotency key scoped to the customer';

COMMENT ON COLUMN service_requests.idempotency_fingerprint IS
    'SHA-256 fingerprint of the booking request payload';