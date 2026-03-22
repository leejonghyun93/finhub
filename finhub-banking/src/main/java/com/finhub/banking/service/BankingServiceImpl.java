package com.finhub.banking.service;

import com.finhub.banking.domain.Account;
import com.finhub.banking.domain.AccountStatus;
import com.finhub.banking.domain.Transaction;
import com.finhub.banking.domain.TransactionType;
import com.finhub.banking.dto.request.CreateAccountRequest;
import com.finhub.banking.dto.request.DepositRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BankingServiceImpl implements BankingService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferSagaService transferSagaService;

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
    public AccountResponse deposit(Long userId, Long accountId, DepositRequest request) {
        Account account = accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        account.deposit(request.amount());

        transactionRepository.save(Transaction.builder()
                .account(account)
                .transactionType(TransactionType.DEPOSIT)
                .amount(request.amount())
                .balanceAfter(account.getBalance())
                .description("충전")
                .build());

        return AccountResponse.from(account);
    }

    @Override
    public void transfer(Long userId, TransferRequest request) {
        transferSagaService.executeTransfer(userId, request);
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
