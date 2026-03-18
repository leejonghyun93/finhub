CREATE TABLE accounts
(
    id             BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20)    NOT NULL UNIQUE,
    user_id        BIGINT         NOT NULL,
    balance        DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    account_name   VARCHAR(100),
    status         VARCHAR(20)    NOT NULL DEFAULT 'ACTIVE',
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);
