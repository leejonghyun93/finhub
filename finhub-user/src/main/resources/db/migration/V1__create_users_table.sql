CREATE TABLE users
(
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(100) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    name         VARCHAR(50)  NOT NULL,
    phone_number VARCHAR(20),
    role         VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);
