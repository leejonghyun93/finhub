-- 더미 유저 데이터 (비밀번호: "password" - BCrypt 인코딩)
INSERT INTO users (email, password, name, phone_number, role, created_at, updated_at)
VALUES ('alice@finhub.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Alice Kim',
        '010-1111-2222', 'ROLE_USER', NOW(), NOW()),
       ('bob@finhub.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Bob Lee', '010-3333-4444',
        'ROLE_USER', NOW(), NOW()),
       ('admin@finhub.com', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'Admin User',
        '010-5555-6666', 'ROLE_ADMIN', NOW(), NOW());
