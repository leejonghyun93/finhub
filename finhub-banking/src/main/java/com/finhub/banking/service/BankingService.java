package com.finhub.banking.service;

import com.finhub.banking.dto.request.CreateAccountRequest;
import com.finhub.banking.dto.request.DepositRequest;
import com.finhub.banking.dto.request.TransferRequest;
import com.finhub.banking.dto.response.AccountResponse;
import com.finhub.banking.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BankingService {

    AccountResponse createAccount(Long userId, CreateAccountRequest request);

    List<AccountResponse> getMyAccounts(Long userId);

    AccountResponse getAccountDetail(Long userId, Long accountId);

    AccountResponse deposit(Long userId, Long accountId, DepositRequest request);

    void transfer(Long userId, TransferRequest request);

    Page<TransactionResponse> getTransactions(Long userId, Long accountId, Pageable pageable);
}
