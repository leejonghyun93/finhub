package com.finhub.banking.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransferInitiatedEvent(
        Long userId,
        Long fromAccountId,
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        LocalDateTime initiatedAt
) {
}
