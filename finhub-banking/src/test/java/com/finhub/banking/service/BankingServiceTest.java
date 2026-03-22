package com.finhub.banking.service;

import com.finhub.banking.domain.Account;
import com.finhub.banking.domain.AccountStatus;
import com.finhub.banking.domain.Transaction;
import com.finhub.banking.domain.TransactionType;
import com.finhub.banking.dto.request.CreateAccountRequest;
import com.finhub.banking.dto.request.DepositRequest;
import com.finhub.banking.dto.request.TransferRequest;
import com.finhub.banking.dto.response.AccountResponse;
import com.finhub.banking.exception.CustomException;
import com.finhub.banking.exception.ErrorCode;
import com.finhub.banking.repository.AccountRepository;
import com.finhub.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BankingService 단위 테스트")
class BankingServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock TransferSagaService transferSagaService;
    @InjectMocks BankingServiceImpl bankingService;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setUp() {
        fromAccount = Account.builder()
                .accountNumber("001-001-000001")
                .userId(1L)
                .balance(new BigDecimal("100000"))
                .accountName("내 계좌")
                .status(AccountStatus.ACTIVE)
                .build();

        toAccount = Account.builder()
                .accountNumber("002-002-000002")
                .userId(2L)
                .balance(new BigDecimal("50000"))
                .accountName("상대 계좌")
                .status(AccountStatus.ACTIVE)
                .build();
    }

    // ── 계좌 개설 ───────────────────────────────────────────────
    @Nested
    @DisplayName("createAccount() 계좌 개설")
    class CreateAccountTest {

        @Test
        @DisplayName("계좌 개설 성공 — 초기 잔액 0원")
        void createAccount_success() {
            // given
            CreateAccountRequest request = new CreateAccountRequest("월급 통장");
            given(accountRepository.save(any(Account.class))).willReturn(fromAccount);

            // when
            AccountResponse response = bankingService.createAccount(1L, request);

            // then
            assertThat(response).isNotNull();
            then(accountRepository).should().save(any(Account.class));
        }
    }

    // ── 계좌 목록 조회 ──────────────────────────────────────────
    @Nested
    @DisplayName("getMyAccounts() 계좌 목록 조회")
    class GetMyAccountsTest {

        @Test
        @DisplayName("사용자 계좌 목록 조회 성공")
        void getMyAccounts_success() {
            // given
            given(accountRepository.findByUserId(1L)).willReturn(List.of(fromAccount));

            // when
            List<AccountResponse> result = bankingService.getMyAccounts(1L);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).accountNumber()).isEqualTo("001-001-000001");
        }

        @Test
        @DisplayName("계좌가 없으면 빈 리스트 반환")
        void getMyAccounts_empty_returnsEmptyList() {
            // given
            given(accountRepository.findByUserId(99L)).willReturn(List.of());

            // when
            List<AccountResponse> result = bankingService.getMyAccounts(99L);

            // then
            assertThat(result).isEmpty();
        }
    }

    // ── 입금 (충전) ─────────────────────────────────────────────
    @Nested
    @DisplayName("deposit() 잔액 충전")
    class DepositTest {

        @Test
        @DisplayName("입금 성공 — 잔액 증가 및 거래내역 생성")
        void deposit_success() {
            // given
            DepositRequest request = new DepositRequest(new BigDecimal("50000"));
            given(accountRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(fromAccount));
            given(transactionRepository.save(any(Transaction.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            AccountResponse response = bankingService.deposit(1L, 1L, request);

            // then
            assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("150000"));
            then(transactionRepository).should().save(argThat(tx ->
                    tx.getTransactionType() == TransactionType.DEPOSIT &&
                    tx.getAmount().compareTo(new BigDecimal("50000")) == 0
            ));
        }

        @Test
        @DisplayName("존재하지 않는 계좌에 입금 시 ACCOUNT_NOT_FOUND 예외 발생")
        void deposit_accountNotFound_throwsException() {
            // given
            DepositRequest request = new DepositRequest(new BigDecimal("50000"));
            given(accountRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bankingService.deposit(1L, 999L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
        }
    }

    // ── 송금 ─────────────────────────────────────────────────
    @Nested
    @DisplayName("transfer() 송금")
    class TransferTest {

        @Test
        @DisplayName("송금 요청 시 TransferSagaService.executeTransfer() 위임 호출")
        void transfer_delegatesToSagaService() {
            // given
            TransferRequest request = new TransferRequest(1L, "002-002-000002", new BigDecimal("30000"), "점심값");

            // when
            bankingService.transfer(1L, request);

            // then
            then(transferSagaService).should().executeTransfer(1L, request);
        }
    }

    // ── 거래내역 조회 ───────────────────────────────────────────
    @Nested
    @DisplayName("getTransactions() 거래내역 조회")
    class GetTransactionsTest {

        @Test
        @DisplayName("거래내역 페이징 조회 성공")
        void getTransactions_success() {
            // given
            PageRequest pageable = PageRequest.of(0, 10);
            Page<Transaction> emptyPage = new PageImpl<>(List.of());
            given(accountRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(fromAccount));
            given(transactionRepository.findByAccountIdOrderByCreatedAtDesc(1L, pageable)).willReturn(emptyPage);

            // when
            var result = bankingService.getTransactions(1L, 1L, pageable);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        @DisplayName("타인 계좌 거래내역 조회 시 ACCOUNT_NOT_FOUND 예외 발생")
        void getTransactions_foreignAccount_throwsException() {
            // given
            given(accountRepository.findByIdAndUserId(2L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> bankingService.getTransactions(1L, 2L, PageRequest.of(0, 10)))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
        }
    }
}
