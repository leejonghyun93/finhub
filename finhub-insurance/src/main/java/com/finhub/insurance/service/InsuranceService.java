package com.finhub.insurance.service;

import com.finhub.insurance.dto.request.SubscribeRequest;
import com.finhub.insurance.dto.response.InsuranceProductResponse;
import com.finhub.insurance.dto.response.SubscriptionResponse;

import java.util.List;

public interface InsuranceService {

    List<InsuranceProductResponse> getProducts();

    InsuranceProductResponse getProduct(Long productId);

    SubscriptionResponse subscribe(Long userId, SubscribeRequest request);

    List<SubscriptionResponse> getSubscriptions(Long userId);

    void cancel(Long userId, Long subscriptionId);
}
