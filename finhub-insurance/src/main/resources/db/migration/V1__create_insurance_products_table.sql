CREATE TABLE insurance_products
(
    id               BIGSERIAL PRIMARY KEY,
    name             VARCHAR(100)   NOT NULL,
    category         VARCHAR(20)    NOT NULL,
    description      TEXT,
    monthly_premium  DECIMAL(19, 2) NOT NULL,
    coverage_amount  DECIMAL(19, 2) NOT NULL,
    created_at       TIMESTAMP      NOT NULL DEFAULT NOW()
);
