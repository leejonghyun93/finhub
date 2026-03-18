package com.finhub.investment.dto.response;

import com.finhub.investment.domain.Holding;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record HoldingResponse(
        Long id,
        Long stockId,
        String stockTicker,
        String stockName,
        Long quantity,
        BigDecimal averagePrice,
        BigDecimal currentPrice,
        BigDecimal currentValue,
        BigDecimal profitLoss,
        BigDecimal profitLossRate
) {
    public static HoldingResponse from(Holding holding) {
        BigDecimal currentPrice = holding.getStock().getCurrentPrice();
        BigDecimal currentValue = currentPrice.multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal costBasis = holding.getAveragePrice().multiply(BigDecimal.valueOf(holding.getQuantity()));
        BigDecimal profitLoss = currentValue.subtract(costBasis);
        BigDecimal profitLossRate = costBasis.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : profitLoss.divide(costBasis, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

        return new HoldingResponse(
                holding.getId(),
                holding.getStock().getId(),
                holding.getStock().getTicker(),
                holding.getStock().getName(),
                holding.getQuantity(),
                holding.getAveragePrice(),
                currentPrice,
                currentValue,
                profitLoss,
                profitLossRate
        );
    }
}
