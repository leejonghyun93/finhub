package com.finhub.banking.controller;

import com.finhub.banking.dto.request.CreateAccountRequest;
import com.finhub.banking.dto.request.TransferRequest;
import com.finhub.banking.dto.response.AccountResponse;
import com.finhub.banking.dto.response.ApiResponse;
import com.finhub.banking.dto.response.TransactionResponse;
import com.finhub.banking.security.CustomUserDetails;
import com.finhub.banking.service.BankingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/banking/accounts")
@RequiredArgsConstructor
public class BankingController {

    private final BankingService bankingService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = bankingService.createAccount(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "계좌 개설 성공"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getMyAccounts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AccountResponse> response = bankingService.getMyAccounts(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "계좌 목록 조회 성공"));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long accountId) {
        AccountResponse response = bankingService.getAccountDetail(userDetails.getUserId(), accountId);
        return ResponseEntity.ok(ApiResponse.success(response, "계좌 상세 조회 성공"));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<Void>> transfer(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TransferRequest request) {
        bankingService.transfer(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "송금 성공"));
    }

    @GetMapping("/{accountId}/transactions")
    public ResponseEntity<ApiResponse<Page<TransactionResponse>>> getTransactions(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long accountId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TransactionResponse> response = bankingService.getTransactions(userDetails.getUserId(), accountId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "거래내역 조회 성공"));
    }
}
