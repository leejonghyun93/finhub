package com.finhub.payment.service;

import com.finhub.payment.domain.Payment;
import com.finhub.payment.domain.PaymentMethod;
import com.finhub.payment.domain.PaymentStatus;
import com.finhub.payment.dto.event.PaymentCompletedEvent;
import com.finhub.payment.dto.request.PayRequest;
import com.finhub.payment.dto.request.RegisterPaymentMethodRequest;
import com.finhub.payment.dto.response.PaymentMethodResponse;
import com.finhub.payment.dto.response.PaymentResponse;
import com.finhub.payment.exception.CustomException;
import com.finhub.payment.exception.ErrorCode;
import com.finhub.payment.repository.PaymentMethodRepository;
import com.finhub.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final String PAYMENT_TOPIC = "payment.completed";

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    @Override
    public PaymentMethodResponse registerMethod(Long userId, RegisterPaymentMethodRequest request) {
        PaymentMethod method = PaymentMethod.builder()
                .userId(userId)
                .methodType(request.getMethodType())
                .name(request.getName())
                .details(request.getDetails())
                .isDefault(request.getIsDefault() != null ? request.getIsDefault() : false)
                .build();
        return PaymentMethodResponse.from(paymentMethodRepository.save(method));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentMethodResponse> getMethods(Long userId) {
        return paymentMethodRepository.findByUserId(userId).stream()
                .map(PaymentMethodResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteMethod(Long userId, Long methodId) {
        PaymentMethod method = paymentMethodRepository.findByIdAndUserId(methodId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));
        paymentMethodRepository.delete(method);
    }

    @Override
    public PaymentResponse pay(Long userId, PayRequest request) {
        PaymentMethod method = paymentMethodRepository.findByIdAndUserId(request.getPaymentMethodId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_METHOD_NOT_FOUND));

        Payment payment = Payment.builder()
                .userId(userId)
                .paymentMethod(method)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(PaymentStatus.COMPLETED)
                .build();

        Payment saved = paymentRepository.save(payment);

        kafkaTemplate.send(PAYMENT_TOPIC, new PaymentCompletedEvent(
                saved.getId(),
                userId,
                method.getId(),
                method.getName(),
                method.getMethodType().name(),
                request.getAmount(),
                request.getDescription(),
                LocalDateTime.now()
        ));

        return PaymentResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getHistory(Long userId, Pageable pageable) {
        return paymentRepository.findByUserIdWithMethod(userId, pageable)
                .map(PaymentResponse::from);
    }
}
