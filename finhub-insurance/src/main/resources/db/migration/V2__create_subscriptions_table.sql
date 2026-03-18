CREATE TABLE subscriptions
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT    NOT NULL,
    product_id   BIGINT    NOT NULL REFERENCES insurance_products (id),
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    subscribed_at TIMESTAMP NOT NULL DEFAULT NOW(),
    cancelled_at  TIMESTAMP
);
