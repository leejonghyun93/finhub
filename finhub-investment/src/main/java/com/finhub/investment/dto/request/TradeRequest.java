package com.finhub.investment.dto.request;

import com.finhub.investment.domain.TradeType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TradeRequest(
        @NotNull(message = "포트폴리오 ID는 필수입니다.")
        Long portfolioId,

        @NotNull(message = "종목 ID는 필수입니다.")
        Long stockId,

        @NotNull(message = "매매 유형은 필수입니다.")
        TradeType tradeType,

        @NotNull(message = "수량은 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        Long quantity,

        @NotNull(message = "가격은 필수입니다.")
        @DecimalMin(value = "0.01", message = "가격은 0.01 이상이어야 합니다.")
        BigDecimal price
) {
}
