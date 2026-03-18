package com.finhub.payment.dto.response;

import com.finhub.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        Long paymentMethodId,
        String paymentMethodName,
        String methodType,
        BigDecimal amount,
        String description,
        String status,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getPaymentMethod().getId(),
                payment.getPaymentMethod().getName(),
                payment.getPaymentMethod().getMethodType().name(),
                payment.getAmount(),
                payment.getDescription(),
                payment.getStatus().name(),
                payment.getCreatedAt()
        );
    }
}
