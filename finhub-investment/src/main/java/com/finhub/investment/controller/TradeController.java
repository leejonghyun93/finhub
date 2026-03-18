package com.finhub.investment.controller;

import com.finhub.investment.dto.request.TradeRequest;
import com.finhub.investment.dto.response.ApiResponse;
import com.finhub.investment.dto.response.TradeHistoryResponse;
import com.finhub.investment.security.CustomUserDetails;
import com.finhub.investment.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/investment/trade")
@RequiredArgsConstructor
public class TradeController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> trade(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TradeRequest request) {
        investmentService.trade(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "매매 완료"));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<TradeHistoryResponse>>> getTradeHistory(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<TradeHistoryResponse> response = investmentService.getTradeHistory(userDetails.getUserId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "매매 내역 조회 성공"));
    }
}
