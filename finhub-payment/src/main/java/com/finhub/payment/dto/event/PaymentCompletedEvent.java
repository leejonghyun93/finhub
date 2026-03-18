package com.finhub.payment.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
        Long paymentId,
        Long userId,
        Long paymentMethodId,
        String paymentMethodName,
        String methodType,
        BigDecimal amount,
        String description,
        LocalDateTime paidAt
) {
}
