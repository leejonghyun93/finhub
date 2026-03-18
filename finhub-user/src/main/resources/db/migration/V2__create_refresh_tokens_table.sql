CREATE TABLE refresh_tokens
(
    id         BIGSERIAL PRIMARY KEY,
    user_email VARCHAR(100) NOT NULL,
    token      TEXT         NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
