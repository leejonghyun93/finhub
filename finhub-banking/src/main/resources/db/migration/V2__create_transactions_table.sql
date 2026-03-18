CREATE TABLE transactions
(
    id                       BIGSERIAL PRIMARY KEY,
    account_id               BIGINT         NOT NULL REFERENCES accounts (id),
    transaction_type         VARCHAR(20)    NOT NULL,
    amount                   DECIMAL(19, 2) NOT NULL,
    balance_after            DECIMAL(19, 2) NOT NULL,
    description              VARCHAR(255),
    counterpart_account_number VARCHAR(20),
    created_at               TIMESTAMP      NOT NULL DEFAULT NOW()
);
