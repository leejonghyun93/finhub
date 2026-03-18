INSERT INTO notifications (user_id, type, title, message, is_read)
VALUES (1, 'USER_REGISTERED',    '가입을 환영합니다!',      'FinHub에 오신 것을 환영합니다. 다양한 금융 서비스를 이용해보세요.', FALSE),
       (1, 'TRANSFER_COMPLETED', '송금 완료',             '110-200-234567 계좌로 100,000원 송금이 완료되었습니다.', FALSE),
       (1, 'PAYMENT_COMPLETED',  '결제 완료',             '스타벅스 커피 15,000원 결제가 완료되었습니다.', TRUE),
       (1, 'TRADE_COMPLETED',    '매매 완료',             '삼성전자 10주 매수가 완료되었습니다.', FALSE),
       (1, 'INSURANCE_SUBSCRIBED', '보험 가입 완료',      '삼성생명 종신보험 가입이 완료되었습니다.', TRUE);
