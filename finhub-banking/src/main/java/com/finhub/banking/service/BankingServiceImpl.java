package com.finhub.banking.service;

import com.finhub.banking.domain.Account;
import com.finhub.banking.domain.AccountStatus;
import com.finhub.banking.domain.Transaction;
import com.finhub.banking.domain.TransactionType;
import com.finhub.banking.dto.event.TransferCompletedEvent;
import com.finhub.banking.dto.request.CreateAccountRequest;
import com.finhub.banking.dto.request.TransferRequest;
import com.finhub.banking.dto.response.AccountResponse;
import com.finhub.banking.dto.response.TransactionResponse;
import com.finhub.banking.exception.CustomException;
import com.finhub.banking.exception.ErrorCode;
import com.finhub.banking.repository.AccountRepository;
import com.finhub.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BankingServiceImpl implements BankingService {

    private static final String TRANSFER_TOPIC = "banking.transfer.completed";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, TransferCompletedEvent> kafkaTemplate;

    @Override
    public AccountResponse createAccount(Long userId, CreateAccountRequest request) {
        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .userId(userId)
                .balance(BigDecimal.ZERO)
                .accountName(request.accountName())
                .status(AccountStatus.ACTIVE)
                .build();

        return AccountResponse.from(accountRepository.save(account));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountDetail(Long userId, Long accountId) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));
        return AccountResponse.from(account);
    }

    @Override
    public void transfer(Long userId, TransferRequest request) {
        Account fromAccount = accountRepository.findByIdAndUserId(request.fromAccountId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        Account toAccount = accountRepository.findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        fromAccount.withdraw(request.amount());
        toAccount.deposit(request.amount());

        String desc = request.description() != null ? request.description() : "송금";

        transactionRepository.save(Transaction.builder()
                .account(fromAccount)
                .transactionType(TransactionType.TRANSFER_OUT)
                .amount(request.amount())
                .balanceAfter(fromAccount.getBalance())
                .description(desc)
                .counterpartAccountNumber(toAccount.getAccountNumber())
                .build());

        transactionRepository.save(Transaction.builder()
                .account(toAccount)
                .transactionType(TransactionType.TRANSFER_IN)
                .amount(request.amount())
                .balanceAfter(toAccount.getBalance())
                .description(desc)
                .counterpartAccountNumber(fromAccount.getAccountNumber())
                .build());

        kafkaTemplate.send(TRANSFER_TOPIC, new TransferCompletedEvent(
                fromAccount.getId(),
                fromAccount.getAccountNumber(),
                toAccount.getId(),
                toAccount.getAccountNumber(),
                request.amount(),
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Long userId, Long accountId, Pageable pageable) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(accountId, pageable)
                .map(TransactionResponse::from);
    }

    private String generateAccountNumber() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        return String.format("%03d-%03d-%06d",
                rnd.nextInt(100, 999),
                rnd.nextInt(100, 999),
                rnd.nextInt(100000, 999999));
    }
}
