package com.finhub.investment.dto.response;

import com.finhub.investment.domain.TradeHistory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeHistoryResponse(
        Long id,
        Long portfolioId,
        Long stockId,
        String stockTicker,
        String stockName,
        String tradeType,
        Long quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
    public static TradeHistoryResponse from(TradeHistory history) {
        return new TradeHistoryResponse(
                history.getId(),
                history.getPortfolio().getId(),
                history.getStock().getId(),
                history.getStock().getTicker(),
                history.getStock().getName(),
                history.getTradeType().name(),
                history.getQuantity(),
                history.getPrice(),
                history.getTotalAmount(),
                history.getCreatedAt()
        );
    }
}
