CREATE TABLE service_requests (

                                  id BIGSERIAL PRIMARY KEY,

                                  customer_id BIGINT NOT NULL,

                                  provider_id BIGINT NOT NULL,

                                  service_type VARCHAR(100) NOT NULL,

                                  description TEXT,

                                  service_address VARCHAR(500) NOT NULL,

                                  latitude DOUBLE PRECISION,

                                  longitude DOUBLE PRECISION,

                                  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

                                  created_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                  updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

                                  CONSTRAINT chk_service_request_status
                                      CHECK (
                                          status IN (
                                                     'PENDING',
                                                     'ACCEPTED',
                                                     'REJECTED',
                                                     'CANCELLED',
                                                     'COMPLETED'
                                              )
                                          )
);

CREATE INDEX idx_service_requests_customer
    ON service_requests(customer_id);

CREATE INDEX idx_service_requests_provider
    ON service_requests(provider_id);

CREATE INDEX idx_service_requests_provider_status
    ON service_requests(provider_id, status);

CREATE INDEX idx_service_requests_customer_status
    ON service_requests(customer_id, status);