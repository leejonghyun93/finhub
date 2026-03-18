-- 유저 ID 1, 2, 3 각각 계좌 1개씩
INSERT INTO accounts (account_number, user_id, balance, account_name, status)
VALUES ('110-100-123456', 1, 900000.00, '급여통장', 'ACTIVE'),
       ('110-200-234567', 2, 600000.00, '생활비통장', 'ACTIVE'),
       ('110-300-345678', 3, 2000000.00, '비상금통장', 'ACTIVE');

-- 거래내역 더미 5개
INSERT INTO transactions (account_id, transaction_type, amount, balance_after, description, counterpart_account_number)
VALUES (1, 'DEPOSIT', 1000000.00, 1000000.00, '초기 입금', NULL),
       (2, 'DEPOSIT', 500000.00, 500000.00, '초기 입금', NULL),
       (3, 'DEPOSIT', 2000000.00, 2000000.00, '초기 입금', NULL),
       (1, 'TRANSFER_OUT', 100000.00, 900000.00, '생활비 송금', '110-200-234567'),
       (2, 'TRANSFER_IN', 100000.00, 600000.00, '생활비 입금', '110-100-123456');
