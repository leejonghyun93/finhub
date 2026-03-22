package com.finhub.banking.service;

import com.finhub.banking.domain.Account;
import com.finhub.banking.domain.AccountStatus;
import com.finhub.banking.domain.Transaction;
import com.finhub.banking.domain.TransactionType;
import com.finhub.banking.dto.event.TransferCompletedEvent;
import com.finhub.banking.dto.event.TransferFailedEvent;
import com.finhub.banking.dto.request.TransferRequest;
import com.finhub.banking.exception.CustomException;
import com.finhub.banking.exception.ErrorCode;
import com.finhub.banking.repository.AccountRepository;
import com.finhub.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferSagaService 단위 테스트 (Saga 패턴)")
class TransferSagaServiceTest {

    @Mock AccountRepository accountRepository;
    @Mock TransactionRepository transactionRepository;
    @Mock KafkaTemplate<String, Object> kafkaTemplate;
    @InjectMocks TransferSagaService transferSagaService;

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

    @Nested
    @DisplayName("executeTransfer() Saga 송금 실행")
    class ExecuteTransferTest {

        @Test
        @DisplayName("정상 송금 — transfer.initiated → transfer.completed Kafka 이벤트 발행")
        void executeTransfer_success_publishesInitiatedAndCompleted() {
            // given
            TransferRequest request = new TransferRequest(1L, "002-002-000002", new BigDecimal("30000"), "테스트 송금");
            given(accountRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("002-002-000002")).willReturn(Optional.of(toAccount));
            given(transactionRepository.save(any(Transaction.class))).willAnswer(inv -> inv.getArgument(0));

            // when
            transferSagaService.executeTransfer(1L, request);

            // then: 잔액 검증
            assertThat(fromAccount.getBalance()).isEqualByComparingTo(new BigDecimal("70000"));
            assertThat(toAccount.getBalance()).isEqualByComparingTo(new BigDecimal("80000"));

            // Kafka 이벤트 발행 순서 검증: transfer.initiated → banking.transfer.completed
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            then(kafkaTemplate).should(times(2)).send(topicCaptor.capture(), any());
            assertThat(topicCaptor.getAllValues()).containsExactly(
                    "transfer.initiated",
                    "banking.transfer.completed"
            );

            // 거래 내역 2건 저장 검증 (TRANSFER_OUT, TRANSFER_IN)
            then(transactionRepository).should(times(2)).save(any(Transaction.class));
        }

        @Test
        @DisplayName("잔액 부족 시 transfer.failed Kafka 이벤트 발행 후 예외 전파")
        void executeTransfer_insufficientBalance_publishesFailedEvent() {
            // given
            TransferRequest request = new TransferRequest(1L, "002-002-000002", new BigDecimal("999999"), "무리한 송금");
            given(accountRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("002-002-000002")).willReturn(Optional.of(toAccount));

            // when & then
            assertThatThrownBy(() -> transferSagaService.executeTransfer(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE));

            // transfer.initiated 후 transfer.failed 발행 검증
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            then(kafkaTemplate).should(atLeast(1)).send(topicCaptor.capture(), any());
            assertThat(topicCaptor.getAllValues()).contains("transfer.initiated", "transfer.failed");
        }

        @Test
        @DisplayName("출금 계좌 없으면 ACCOUNT_NOT_FOUND 예외 발생")
        void executeTransfer_fromAccountNotFound_throwsException() {
            // given
            TransferRequest request = new TransferRequest(999L, "002-002-000002", new BigDecimal("10000"), null);
            given(accountRepository.findByIdAndUserId(999L, 1L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> transferSagaService.executeTransfer(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
        }

        @Test
        @DisplayName("입금 계좌 없으면 ACCOUNT_NOT_FOUND 예외 발생")
        void executeTransfer_toAccountNotFound_throwsException() {
            // given
            TransferRequest request = new TransferRequest(1L, "000-000-000000", new BigDecimal("10000"), null);
            given(accountRepository.findByIdAndUserId(1L, 1L)).willReturn(Optional.of(fromAccount));
            given(accountRepository.findByAccountNumber("000-000-000000")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> transferSagaService.executeTransfer(1L, request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND));
        }
    }

    @Nested
    @DisplayName("handleTransferFailed() 보상 트랜잭션")
    class CompensationTest {

        @Test
        @DisplayName("transfer.failed 이벤트 수신 시 보상 로그 출력 (예외 없이 처리)")
        void handleTransferFailed_doesNotThrow() {
            // given
            TransferFailedEvent event = new TransferFailedEvent(
                    1L, 1L, "001-001-000001", 2L, "002-002-000002",
                    new BigDecimal("30000"), "잔액 부족"
            );

            // when & then: 예외 없이 정상 처리
            assertThatCode(() -> transferSagaService.handleTransferFailed(event))
                    .doesNotThrowAnyException();
        }
    }
}
