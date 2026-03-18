package com.finhub.payment.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    PAYMENT_METHOD_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 수단을 찾을 수 없습니다."),
    PAYMENT_METHOD_FORBIDDEN(HttpStatus.FORBIDDEN, "해당 결제 수단에 접근 권한이 없습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}
