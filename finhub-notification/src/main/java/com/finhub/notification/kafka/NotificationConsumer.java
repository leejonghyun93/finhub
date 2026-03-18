package com.finhub.notification.kafka;

import com.finhub.notification.domain.NotificationType;
import com.finhub.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "user.registered", groupId = "notification-group")
    public void handleUserRegistered(Map<String, Object> payload) {
        try {
            Long userId = toLong(payload.get("userId"));
            String name = (String) payload.getOrDefault("name", "회원");
            notificationService.createNotification(
                    userId,
                    NotificationType.USER_REGISTERED,
                    "가입을 환영합니다!",
                    name + "님, FinHub에 오신 것을 환영합니다. 다양한 금융 서비스를 이용해보세요."
            );
        } catch (Exception e) {
            log.error("user.registered 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "banking.transfer.completed", groupId = "notification-group")
    public void handleTransferCompleted(Map<String, Object> payload) {
        try {
            Long userId = toLong(payload.get("userId"));
            String toAccountNumber = (String) payload.getOrDefault("toAccountNumber", "");
            Object amount = payload.get("amount");
            notificationService.createNotification(
                    userId,
                    NotificationType.TRANSFER_COMPLETED,
                    "송금 완료",
                    toAccountNumber + " 계좌로 " + amount + "원 송금이 완료되었습니다."
            );
        } catch (Exception e) {
            log.error("banking.transfer.completed 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "payment.completed", groupId = "notification-group")
    public void handlePaymentCompleted(Map<String, Object> payload) {
        try {
            Long userId = toLong(payload.get("userId"));
            String description = (String) payload.getOrDefault("description", "결제");
            Object amount = payload.get("amount");
            notificationService.createNotification(
                    userId,
                    NotificationType.PAYMENT_COMPLETED,
                    "결제 완료",
                    description + " " + amount + "원 결제가 완료되었습니다."
            );
        } catch (Exception e) {
            log.error("payment.completed 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "investment.trade.completed", groupId = "notification-group")
    public void handleTradeCompleted(Map<String, Object> payload) {
        try {
            Long userId = toLong(payload.get("userId"));
            String stockName = (String) payload.getOrDefault("stockName", "종목");
            Object quantity = payload.get("quantity");
            String tradeType = (String) payload.getOrDefault("tradeType", "매매");
            String action = "BUY".equals(tradeType) ? "매수" : "매도";
            notificationService.createNotification(
                    userId,
                    NotificationType.TRADE_COMPLETED,
                    "매매 완료",
                    stockName + " " + quantity + "주 " + action + "가 완료되었습니다."
            );
        } catch (Exception e) {
            log.error("investment.trade.completed 처리 실패: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "insurance.subscribed", groupId = "notification-group")
    public void handleInsuranceSubscribed(Map<String, Object> payload) {
        try {
            Long userId = toLong(payload.get("userId"));
            String productName = (String) payload.getOrDefault("productName", "보험");
            notificationService.createNotification(
                    userId,
                    NotificationType.INSURANCE_SUBSCRIBED,
                    "보험 가입 완료",
                    productName + " 가입이 완료되었습니다. 보험료는 매월 자동 납부됩니다."
            );
        } catch (Exception e) {
            log.error("insurance.subscribed 처리 실패: {}", e.getMessage());
        }
    }

    private Long toLong(Object value) {
        if (value == null) throw new IllegalArgumentException("userId is null");
        return ((Number) value).longValue();
    }
}
