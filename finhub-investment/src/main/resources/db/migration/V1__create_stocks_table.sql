CREATE TABLE stocks
(
    id            BIGSERIAL PRIMARY KEY,
    ticker        VARCHAR(20)    NOT NULL UNIQUE,
    name          VARCHAR(100)   NOT NULL,
    current_price DECIMAL(19, 2) NOT NULL,
    market        VARCHAR(20)    NOT NULL,
    created_at    TIMESTAMP      NOT NULL DEFAULT NOW()
);
