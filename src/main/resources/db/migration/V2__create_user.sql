CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    username        VARCHAR(18) NOT NULL UNIQUE
                    CHECK (CHAR_LENGTH(username) BETWEEN 6 AND 18),
    password_hash   TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE user_urls (
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    url_id          BIGINT NOT NULL REFERENCES url(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, url_id)
);

CREATE INDEX idx_user_urls_url_id ON user_urls(user_id);