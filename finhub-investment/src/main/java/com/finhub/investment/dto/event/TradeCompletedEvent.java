package com.finhub.investment.dto.event;

import com.finhub.investment.domain.TradeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeCompletedEvent(
        Long portfolioId,
        Long stockId,
        String stockTicker,
        String stockName,
        TradeType tradeType,
        Long quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        Long userId,
        LocalDateTime tradedAt
) {
}
