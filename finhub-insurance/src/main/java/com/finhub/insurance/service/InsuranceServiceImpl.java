package com.finhub.insurance.service;

import com.finhub.insurance.domain.InsuranceProduct;
import com.finhub.insurance.domain.Subscription;
import com.finhub.insurance.domain.SubscriptionStatus;
import com.finhub.insurance.dto.event.InsuranceSubscribedEvent;
import com.finhub.insurance.dto.request.SubscribeRequest;
import com.finhub.insurance.dto.response.InsuranceProductResponse;
import com.finhub.insurance.dto.response.SubscriptionResponse;
import com.finhub.insurance.exception.CustomException;
import com.finhub.insurance.exception.ErrorCode;
import com.finhub.insurance.repository.InsuranceProductRepository;
import com.finhub.insurance.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InsuranceServiceImpl implements InsuranceService {

    private static final String SUBSCRIBE_TOPIC = "insurance.subscribed";

    private final InsuranceProductRepository productRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final KafkaTemplate<String, InsuranceSubscribedEvent> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<InsuranceProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(InsuranceProductResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InsuranceProductResponse getProduct(Long productId) {
        InsuranceProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));
        return InsuranceProductResponse.from(product);
    }

    @Override
    public SubscriptionResponse subscribe(Long userId, SubscribeRequest request) {
        InsuranceProduct product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new CustomException(ErrorCode.PRODUCT_NOT_FOUND));

        if (subscriptionRepository.existsByUserIdAndProductIdAndStatus(userId, product.getId(), SubscriptionStatus.ACTIVE)) {
            throw new CustomException(ErrorCode.ALREADY_SUBSCRIBED);
        }

        Subscription subscription = Subscription.builder()
                .userId(userId)
                .product(product)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription saved = subscriptionRepository.save(subscription);

        kafkaTemplate.send(SUBSCRIBE_TOPIC, new InsuranceSubscribedEvent(
                saved.getId(),
                userId,
                product.getId(),
                product.getName(),
                product.getCategory().name(),
                product.getMonthlyPremium(),
                LocalDateTime.now()
        ));

        return SubscriptionResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptions(Long userId) {
        return subscriptionRepository.findByUserIdWithProduct(userId).stream()
                .map(SubscriptionResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    public void cancel(Long userId, Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findByIdAndUserId(subscriptionId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.SUBSCRIPTION_NOT_FOUND));

        if (subscription.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new CustomException(ErrorCode.ALREADY_CANCELLED);
        }

        subscription.cancel();
    }
}
