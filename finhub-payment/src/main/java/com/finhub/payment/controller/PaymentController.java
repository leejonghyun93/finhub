package com.finhub.payment.controller;

import com.finhub.payment.dto.request.PayRequest;
import com.finhub.payment.dto.request.RegisterPaymentMethodRequest;
import com.finhub.payment.dto.response.ApiResponse;
import com.finhub.payment.dto.response.PaymentMethodResponse;
import com.finhub.payment.dto.response.PaymentResponse;
import com.finhub.payment.security.CustomUserDetails;
import com.finhub.payment.service.PaymentService;
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
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/methods")
    public ResponseEntity<ApiResponse<PaymentMethodResponse>> registerMethod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody RegisterPaymentMethodRequest request) {
        PaymentMethodResponse response = paymentService.registerMethod(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "결제 수단 등록 성공"));
    }

    @GetMapping("/methods")
    public ResponseEntity<ApiResponse<List<PaymentMethodResponse>>> getMethods(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PaymentMethodResponse> response = paymentService.getMethods(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "결제 수단 목록 조회 성공"));
    }

    @DeleteMapping("/methods/{methodId}")
    public ResponseEntity<ApiResponse<Void>> deleteMethod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long methodId) {
        paymentService.deleteMethod(userDetails.getUserId(), methodId);
        return ResponseEntity.ok(ApiResponse.success(null, "결제 수단 삭제 성공"));
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<PaymentResponse>> pay(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PayRequest request) {
        PaymentResponse response = paymentService.pay(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "결제 완료"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PaymentResponse> response = paymentService.getHistory(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "결제 내역 조회 성공"));
    }
}
