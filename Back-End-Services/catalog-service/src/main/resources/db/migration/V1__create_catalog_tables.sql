CREATE TABLE catalog_items (

                               id BIGSERIAL PRIMARY KEY,

                               provider_id BIGINT NOT NULL,

                               name VARCHAR(150) NOT NULL,

                               description VARCHAR(1000),

                               category VARCHAR(100) NOT NULL,

                               price NUMERIC(10,2) NOT NULL,

                               duration_minutes INTEGER,

                               active BOOLEAN NOT NULL DEFAULT TRUE,

                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);