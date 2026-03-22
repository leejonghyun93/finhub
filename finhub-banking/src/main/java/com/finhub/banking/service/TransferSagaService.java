package com.finhub.banking.service;

import com.finhub.banking.domain.Account;
import com.finhub.banking.domain.Transaction;
import com.finhub.banking.domain.TransactionType;
import com.finhub.banking.dto.event.TransferCompletedEvent;
import com.finhub.banking.dto.event.TransferFailedEvent;
import com.finhub.banking.dto.event.TransferInitiatedEvent;
import com.finhub.banking.dto.request.TransferRequest;
import com.finhub.banking.exception.CustomException;
import com.finhub.banking.exception.ErrorCode;
import com.finhub.banking.repository.AccountRepository;
import com.finhub.banking.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Choreography-based Saga 패턴으로 송금 처리.
 * <pre>
 *   transfer.initiated 발행
 *       → 계좌 조회 · 잔액 이체 · 거래내역 저장
 *           → 성공: banking.transfer.completed 발행
 *           → 실패: transfer.failed 발행 후 예외 재전파
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TransferSagaService {

    private static final String TOPIC_INITIATED = "transfer.initiated";
    private static final String TOPIC_COMPLETED = "banking.transfer.completed";
    private static final String TOPIC_FAILED    = "transfer.failed";

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void executeTransfer(Long userId, TransferRequest request) {
        // Step 1. Saga 시작 이벤트
        kafkaTemplate.send(TOPIC_INITIATED, new TransferInitiatedEvent(
                userId,
                request.fromAccountId(),
                null,
                request.toAccountNumber(),
                request.amount(),
                LocalDateTime.now()
        ));

        try {
            // Step 2. 계좌 조회
            Account fromAccount = accountRepository.findByIdAndUserId(request.fromAccountId(), userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

            Account toAccount = accountRepository.findByAccountNumber(request.toAccountNumber())
                    .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

            // Step 3. 잔액 이체
            fromAccount.withdraw(request.amount());
            toAccount.deposit(request.amount());

            // Step 4. 거래내역 저장 (TRANSFER_OUT / TRANSFER_IN)
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

            // Step 5. Saga 완료 이벤트
            kafkaTemplate.send(TOPIC_COMPLETED, new TransferCompletedEvent(
                    userId,
                    fromAccount.getId(),
                    fromAccount.getAccountNumber(),
                    toAccount.getId(),
                    toAccount.getAccountNumber(),
                    request.amount(),
                    LocalDateTime.now()
            ));

        } catch (CustomException e) {
            // Step 5-alt. 보상 이벤트 발행
            log.warn("[Saga] 송금 실패 — userId={}, reason={}", userId, e.getMessage());
            kafkaTemplate.send(TOPIC_FAILED, new TransferFailedEvent(
                    userId,
                    request.fromAccountId(),
                    null,
                    null,
                    request.toAccountNumber(),
                    request.amount(),
                    e.getMessage()
            ));
            throw e;
        }
    }

    /**
     * transfer.failed 이벤트 수신 시 보상 트랜잭션 처리.
     * DB 트랜잭션 롤백은 @Transactional이 보장하므로,
     * 여기서는 알림·감사 로그 등 추가 보상 로직을 구현한다.
     */
    public void handleTransferFailed(TransferFailedEvent event) {
        log.warn("[Saga 보상] 송금 실패 처리 완료 — userId={}, amount={}, reason={}",
                event.userId(), event.amount(), event.reason());
    }
}
