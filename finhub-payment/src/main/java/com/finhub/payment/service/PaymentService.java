package com.finhub.payment.service;

import com.finhub.payment.dto.request.PayRequest;
import com.finhub.payment.dto.request.RegisterPaymentMethodRequest;
import com.finhub.payment.dto.response.PaymentMethodResponse;
import com.finhub.payment.dto.response.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PaymentService {

    PaymentMethodResponse registerMethod(Long userId, RegisterPaymentMethodRequest request);

    List<PaymentMethodResponse> getMethods(Long userId);

    void deleteMethod(Long userId, Long methodId);

    PaymentResponse pay(Long userId, PayRequest request);

    Page<PaymentResponse> getHistory(Long userId, Pageable pageable);
}
