CREATE TABLE user_profiles (
                               id BIGINT PRIMARY KEY,
                               first_name VARCHAR(100) NOT NULL,
                               last_name VARCHAR(100) NOT NULL,
                               phone VARCHAR(20),
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO user_profiles (
    id,
    first_name,
    last_name,
    phone,
    created_at,
    updated_at
)
SELECT
    id,
    first_name,
    last_name,
    phone,
    created_at,
    updated_at
FROM users;