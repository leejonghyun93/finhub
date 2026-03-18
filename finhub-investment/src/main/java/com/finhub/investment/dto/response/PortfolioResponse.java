package com.finhub.investment.dto.response;

import com.finhub.investment.domain.Portfolio;

import java.time.LocalDateTime;

public record PortfolioResponse(
        Long id,
        Long userId,
        String name,
        String description,
        LocalDateTime createdAt
) {
    public static PortfolioResponse from(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getUserId(),
                portfolio.getName(),
                portfolio.getDescription(),
                portfolio.getCreatedAt()
        );
    }
}
