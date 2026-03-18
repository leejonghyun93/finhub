CREATE TABLE payments
(
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT         NOT NULL,
    payment_method_id BIGINT         NOT NULL REFERENCES payment_methods (id),
    amount            DECIMAL(19, 2) NOT NULL,
    description       VARCHAR(255),
    status            VARCHAR(20)    NOT NULL DEFAULT 'COMPLETED',
    created_at        TIMESTAMP      NOT NULL DEFAULT NOW()
);
