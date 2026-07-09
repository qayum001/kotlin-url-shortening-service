CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    keycloak_id UUID NOT NULL UNIQUE,
    username    VARCHAR(18) NOT NULL,
    name        VARCHAR(100),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_urls (
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    url_id          BIGINT NOT NULL REFERENCES url(id) ON DELETE CASCADE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    PRIMARY KEY (user_id, url_id)
);

CREATE INDEX idx_user_urls_url_id ON user_urls(user_id);