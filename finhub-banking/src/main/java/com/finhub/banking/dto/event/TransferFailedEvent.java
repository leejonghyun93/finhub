package com.finhub.banking.dto.event;

import java.math.BigDecimal;

public record TransferFailedEvent(
        Long userId,
        Long fromAccountId,
        String fromAccountNumber,
        Long toAccountId,
        String toAccountNumber,
        BigDecimal amount,
        String reason
) {
}
