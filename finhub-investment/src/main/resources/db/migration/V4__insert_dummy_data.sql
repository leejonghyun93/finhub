-- 주식 종목 10개
INSERT INTO stocks (ticker, name, current_price, market)
VALUES ('005930', '삼성전자', 75000.00, 'KOSPI'),
       ('AAPL', '애플', 189.50, 'NASDAQ'),
       ('TSLA', '테슬라', 245.30, 'NASDAQ'),
       ('035720', '카카오', 52000.00, 'KOSPI'),
       ('035420', '네이버', 198000.00, 'KOSPI'),
       ('000660', 'SK하이닉스', 168000.00, 'KOSPI'),
       ('373220', 'LG에너지솔루션', 385000.00, 'KOSPI'),
       ('005380', '현대차', 245000.00, 'KOSPI'),
       ('GOOGL', '알파벳(구글)', 175.80, 'NASDAQ'),
       ('MSFT', '마이크로소프트', 415.20, 'NASDAQ');

-- 유저 1번 포트폴리오
INSERT INTO portfolios (user_id, name, description)
VALUES (1, '내 포트폴리오', '장기 투자 포트폴리오');

-- 보유 종목 3개 (삼성전자 10주, 애플 5주, 테슬라 2주)
INSERT INTO holdings (portfolio_id, stock_id, quantity, average_price)
VALUES (1, 1, 10, 72000.00),
       (1, 2, 5, 182.00),
       (1, 3, 2, 230.00);

-- 매매 내역 (holdings와 일치하는 매수 내역)
INSERT INTO trade_history (portfolio_id, stock_id, user_id, trade_type, quantity, price, total_amount)
VALUES (1, 1, 1, 'BUY', 10, 72000.00, 720000.00),
       (1, 2, 1, 'BUY', 5, 182.00, 910.00),
       (1, 3, 1, 'BUY', 2, 230.00, 460.00);
