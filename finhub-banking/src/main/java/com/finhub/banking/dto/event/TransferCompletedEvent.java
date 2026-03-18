package com.finhub.banking.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferCompletedEvent(
        Long userId,
        Long fromAccountId,
        String fromAccountNumber,
        Long toAccountId,
        String toAccountNumber,
        BigDecimal amount,
        LocalDateTime transferredAt
) {
}
