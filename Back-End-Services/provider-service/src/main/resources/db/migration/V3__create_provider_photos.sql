CREATE TABLE provider_photos (
                                 id BIGSERIAL PRIMARY KEY,

                                 provider_id BIGINT NOT NULL,

                                 image_url TEXT NOT NULL,

                                 display_order INTEGER NOT NULL DEFAULT 0,

                                 created_at TIMESTAMPTZ NOT NULL,

                                 CONSTRAINT fk_provider_photos_provider
                                     FOREIGN KEY (provider_id)
                                         REFERENCES providers(id)
                                         ON DELETE CASCADE
);

CREATE INDEX idx_provider_photos_provider_id
    ON provider_photos(provider_id);

CREATE INDEX idx_provider_photos_display_order
    ON provider_photos(provider_id, display_order);