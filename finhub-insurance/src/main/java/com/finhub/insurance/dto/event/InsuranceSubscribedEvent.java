package com.finhub.insurance.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InsuranceSubscribedEvent(
        Long subscriptionId,
        Long userId,
        Long productId,
        String productName,
        String category,
        BigDecimal monthlyPremium,
        LocalDateTime subscribedAt
) {
}
