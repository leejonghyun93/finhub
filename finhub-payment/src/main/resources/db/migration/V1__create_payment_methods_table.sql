CREATE TABLE payment_methods
(
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    method_type VARCHAR(20)  NOT NULL,
    name        VARCHAR(100) NOT NULL,
    details     VARCHAR(255),
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
