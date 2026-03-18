CREATE TABLE holdings
(
    id            BIGSERIAL PRIMARY KEY,
    portfolio_id  BIGINT         NOT NULL REFERENCES portfolios (id),
    stock_id      BIGINT         NOT NULL REFERENCES stocks (id),
    quantity      BIGINT         NOT NULL,
    average_price DECIMAL(19, 2) NOT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP      NOT NULL DEFAULT NOW(),
    UNIQUE (portfolio_id, stock_id)
);

CREATE TABLE trade_history
(
    id           BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT         NOT NULL REFERENCES portfolios (id),
    stock_id     BIGINT         NOT NULL REFERENCES stocks (id),
    user_id      BIGINT         NOT NULL,
    trade_type   VARCHAR(10)    NOT NULL,
    quantity     BIGINT         NOT NULL,
    price        DECIMAL(19, 2) NOT NULL,
    total_amount DECIMAL(19, 2) NOT NULL,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);
