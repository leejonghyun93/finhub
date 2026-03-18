package com.finhub.investment.dto.response;

import com.finhub.investment.domain.Stock;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockResponse(
        Long id,
        String ticker,
        String name,
        BigDecimal currentPrice,
        String market,
        LocalDateTime createdAt
) {
    public static StockResponse from(Stock stock) {
        return new StockResponse(
                stock.getId(),
                stock.getTicker(),
                stock.getName(),
                stock.getCurrentPrice(),
                stock.getMarket(),
                stock.getCreatedAt()
        );
    }
}
