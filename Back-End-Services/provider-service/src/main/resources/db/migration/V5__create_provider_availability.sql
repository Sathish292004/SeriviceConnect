CREATE TABLE provider_availability (
                                       id BIGSERIAL PRIMARY KEY,

                                       provider_id BIGINT NOT NULL,

                                       day_of_week VARCHAR(20) NOT NULL,

                                       start_time TIME NOT NULL,

                                       end_time TIME NOT NULL,

                                       active BOOLEAN NOT NULL DEFAULT TRUE,

                                       created_at TIMESTAMPTZ NOT NULL,

                                       updated_at TIMESTAMPTZ NOT NULL,

                                       CONSTRAINT fk_provider_availability_provider
                                           FOREIGN KEY (provider_id)
                                               REFERENCES providers(id)
                                               ON DELETE CASCADE,

                                       CONSTRAINT chk_provider_availability_time
                                           CHECK (start_time < end_time)
);

CREATE INDEX idx_provider_availability_provider_id
    ON provider_availability(provider_id);

CREATE INDEX idx_provider_availability_provider_day
    ON provider_availability(provider_id, day_of_week);

CREATE INDEX idx_provider_availability_active
    ON provider_availability(provider_id, active);

CREATE UNIQUE INDEX uq_provider_availability_window
    ON provider_availability(
                             provider_id,
                             day_of_week,
                             start_time,
                             end_time
        );