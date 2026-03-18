package com.finhub.banking.dto.response;

import com.finhub.banking.domain.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        Long accountId,
        String transactionType,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        String counterpartAccountNumber,
        LocalDateTime createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getAccount().getId(),
                transaction.getTransactionType().name(),
                transaction.getAmount(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCounterpartAccountNumber(),
                transaction.getCreatedAt()
        );
    }
}
