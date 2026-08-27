CREATE TABLE reviews (
                         id BIGSERIAL PRIMARY KEY,

                         booking_id BIGINT NOT NULL,

                         customer_id BIGINT NOT NULL,

                         provider_id BIGINT NOT NULL,

                         rating INTEGER NOT NULL,

                         comment VARCHAR(1000),

                         active BOOLEAN NOT NULL DEFAULT TRUE,

                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT chk_review_rating
                             CHECK (rating BETWEEN 1 AND 5),

                         CONSTRAINT uk_review_booking
                             UNIQUE (booking_id)
);