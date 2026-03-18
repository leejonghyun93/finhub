package com.finhub.insurance.dto.response;

import com.finhub.insurance.domain.Subscription;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long id,
        Long productId,
        String productName,
        String category,
        BigDecimal monthlyPremium,
        BigDecimal coverageAmount,
        String status,
        LocalDateTime subscribedAt,
        LocalDateTime cancelledAt
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getProduct().getId(),
                subscription.getProduct().getName(),
                subscription.getProduct().getCategory().name(),
                subscription.getProduct().getMonthlyPremium(),
                subscription.getProduct().getCoverageAmount(),
                subscription.getStatus().name(),
                subscription.getSubscribedAt(),
                subscription.getCancelledAt()
        );
    }
}
