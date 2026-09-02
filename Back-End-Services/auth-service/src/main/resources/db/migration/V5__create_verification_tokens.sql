CREATE TABLE verification_tokens (
                                     id UUID PRIMARY KEY,

                                     user_id BIGINT NOT NULL,

                                     token_hash VARCHAR(64) NOT NULL UNIQUE,

                                     expires_at TIMESTAMPTZ NOT NULL,

                                     consumed_at TIMESTAMPTZ,

                                     created_at TIMESTAMPTZ NOT NULL,

                                     CONSTRAINT fk_verification_tokens_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES users(id)
                                             ON DELETE CASCADE
);

CREATE INDEX idx_verification_tokens_user_id
    ON verification_tokens(user_id);

CREATE INDEX idx_verification_tokens_expires_at
    ON verification_tokens(expires_at);