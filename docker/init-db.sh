#!/bin/bash
set -e

echo "추가 데이터베이스 생성 중..."

for db in finhub_banking finhub_investment finhub_payment finhub_insurance finhub_notification; do
    echo "  - $db 생성"
    psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
        -c "CREATE DATABASE $db;" 2>/dev/null || echo "  - $db 이미 존재 (skip)"
done

echo "데이터베이스 초기화 완료"
