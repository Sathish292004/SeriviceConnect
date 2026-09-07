CREATE TABLE help_articles (
                               id BIGSERIAL PRIMARY KEY,

                               slug VARCHAR(150) NOT NULL UNIQUE,

                               title VARCHAR(200) NOT NULL,

                               category VARCHAR(80) NOT NULL,

                               content TEXT NOT NULL,

                               published BOOLEAN NOT NULL DEFAULT FALSE,

                               display_order INTEGER NOT NULL DEFAULT 0,

                               created_at TIMESTAMPTZ NOT NULL,

                               updated_at TIMESTAMPTZ NOT NULL,

                               version BIGINT NOT NULL DEFAULT 0
);


CREATE INDEX idx_help_article_published_category_order
    ON help_articles (
                      published,
                      category,
                      display_order
        );


CREATE INDEX idx_help_article_category
    ON help_articles (
                      category
        );