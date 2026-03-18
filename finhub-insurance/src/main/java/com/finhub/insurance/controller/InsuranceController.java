package com.finhub.insurance.controller;

import com.finhub.insurance.dto.request.SubscribeRequest;
import com.finhub.insurance.dto.response.ApiResponse;
import com.finhub.insurance.dto.response.InsuranceProductResponse;
import com.finhub.insurance.dto.response.SubscriptionResponse;
import com.finhub.insurance.security.CustomUserDetails;
import com.finhub.insurance.service.InsuranceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/insurance")
@RequiredArgsConstructor
public class InsuranceController {

    private final InsuranceService insuranceService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<InsuranceProductResponse>>> getProducts() {
        return ResponseEntity.ok(ApiResponse.success(insuranceService.getProducts(), "보험 상품 목록 조회 성공"));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<InsuranceProductResponse>> getProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(insuranceService.getProduct(productId), "보험 상품 상세 조회 성공"));
    }

    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> subscribe(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SubscribeRequest request) {
        SubscriptionResponse response = insuranceService.subscribe(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "보험 가입 성공"));
    }

    @GetMapping("/subscriptions")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptions(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SubscriptionResponse> response = insuranceService.getSubscriptions(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "가입 내역 조회 성공"));
    }

    @DeleteMapping("/subscriptions/{subscriptionId}")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long subscriptionId) {
        insuranceService.cancel(userDetails.getUserId(), subscriptionId);
        return ResponseEntity.ok(ApiResponse.success(null, "보험 해지 성공"));
    }
}
