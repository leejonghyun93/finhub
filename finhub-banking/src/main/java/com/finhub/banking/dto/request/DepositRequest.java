package com.finhub.banking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DepositRequest(
        @NotNull(message = "충전 금액은 필수입니다.")
        @DecimalMin(value = "1", message = "충전 금액은 1원 이상이어야 합니다.")
        BigDecimal amount
) {
}
