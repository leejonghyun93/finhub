package com.finhub.insurance.dto.response;

import com.finhub.insurance.domain.InsuranceProduct;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record InsuranceProductResponse(
        Long id,
        String name,
        String category,
        String description,
        BigDecimal monthlyPremium,
        BigDecimal coverageAmount,
        LocalDateTime createdAt
) {
    public static InsuranceProductResponse from(InsuranceProduct product) {
        return new InsuranceProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory().name(),
                product.getDescription(),
                product.getMonthlyPremium(),
                product.getCoverageAmount(),
                product.getCreatedAt()
        );
    }
}
