package com.finhub.payment.dto.response;

import com.finhub.payment.domain.PaymentMethod;

import java.time.LocalDateTime;

public record PaymentMethodResponse(
        Long id,
        String methodType,
        String name,
        String details,
        Boolean isDefault,
        LocalDateTime createdAt
) {
    public static PaymentMethodResponse from(PaymentMethod method) {
        return new PaymentMethodResponse(
                method.getId(),
                method.getMethodType().name(),
                method.getName(),
                method.getDetails(),
                method.getIsDefault(),
                method.getCreatedAt()
        );
    }
}
