CREATE TABLE providers (
                           id BIGSERIAL PRIMARY KEY,

                           user_id BIGINT NOT NULL UNIQUE,

                           business_name VARCHAR(150) NOT NULL,

                           description TEXT,

                           phone VARCHAR(20) NOT NULL,

                           email VARCHAR(255) NOT NULL,

                           address TEXT,

                           city VARCHAR(100),

                           state VARCHAR(100),

                           postal_code VARCHAR(20),

                           status VARCHAR(30) NOT NULL,

                           created_at TIMESTAMPTZ NOT NULL,

                           updated_at TIMESTAMPTZ NOT NULL
);