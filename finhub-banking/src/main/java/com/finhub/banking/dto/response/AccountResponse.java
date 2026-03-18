package com.finhub.banking.dto.response;

import com.finhub.banking.domain.Account;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
        Long id,
        String accountNumber,
        Long userId,
        BigDecimal balance,
        String accountName,
        String status,
        LocalDateTime createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getUserId(),
                account.getBalance(),
                account.getAccountName(),
                account.getStatus().name(),
                account.getCreatedAt()
        );
    }
}
