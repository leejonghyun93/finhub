package com.finhub.banking.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(
        @NotNull(message = "출금 계좌 ID는 필수입니다.")
        Long fromAccountId,

        @NotBlank(message = "수취인 계좌번호는 필수입니다.")
        String toAccountNumber,

        @NotNull(message = "이체 금액은 필수입니다.")
        @DecimalMin(value = "1", message = "이체 금액은 1원 이상이어야 합니다.")
        BigDecimal amount,

        String description
) {
}
