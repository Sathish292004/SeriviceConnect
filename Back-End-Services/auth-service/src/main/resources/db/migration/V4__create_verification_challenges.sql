CREATE TABLE verification_challenges (
                                         id UUID PRIMARY KEY,

                                         user_id BIGINT NOT NULL,

                                         channel VARCHAR(20) NOT NULL,

                                         purpose VARCHAR(30) NOT NULL,

                                         code_hash VARCHAR(255) NOT NULL,

                                         expires_at TIMESTAMPTZ NOT NULL,

                                         attempts INT NOT NULL DEFAULT 0,

                                         consumed_at TIMESTAMPTZ,

                                         created_at TIMESTAMPTZ NOT NULL,

                                         last_sent_at TIMESTAMPTZ NOT NULL,

                                         CONSTRAINT fk_verification_challenges_user
                                             FOREIGN KEY (user_id)
                                                 REFERENCES users(id)
                                                 ON DELETE CASCADE
);

CREATE INDEX idx_verification_challenges_user_id
    ON verification_challenges(user_id);

CREATE INDEX idx_verification_challenges_expires_at
    ON verification_challenges(expires_at);

CREATE INDEX idx_verification_challenges_user_channel_purpose
    ON verification_challenges(user_id, channel, purpose);