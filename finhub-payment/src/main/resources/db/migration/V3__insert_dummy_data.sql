-- 유저 1번 결제 수단 2개
INSERT INTO payment_methods (user_id, method_type, name, details, is_default)
VALUES (1, 'CARD', '신한카드', '신한카드 1234-****-****-5678', TRUE),
       (1, 'BANK_ACCOUNT', '국민은행 계좌', '110-100-123456', FALSE);

-- 결제 내역 5개
INSERT INTO payments (user_id, payment_method_id, amount, description, status)
VALUES (1, 1, 15000.00, '스타벅스 커피', 'COMPLETED'),
       (1, 1, 89000.00, '온라인 쇼핑', 'COMPLETED'),
       (1, 2, 250000.00, '공과금 납부', 'COMPLETED'),
       (1, 1, 32000.00, '편의점', 'COMPLETED'),
       (1, 2, 120000.00, '통신비', 'COMPLETED');
