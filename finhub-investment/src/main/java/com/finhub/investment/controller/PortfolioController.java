package com.finhub.investment.controller;

import com.finhub.investment.dto.request.CreatePortfolioRequest;
import com.finhub.investment.dto.response.ApiResponse;
import com.finhub.investment.dto.response.HoldingResponse;
import com.finhub.investment.dto.response.PortfolioResponse;
import com.finhub.investment.security.CustomUserDetails;
import com.finhub.investment.service.InvestmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investment/portfolios")
@RequiredArgsConstructor
public class PortfolioController {

    private final InvestmentService investmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> createPortfolio(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreatePortfolioRequest request) {
        PortfolioResponse response = investmentService.createPortfolio(userDetails.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "포트폴리오 생성 성공"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PortfolioResponse>>> getPortfolios(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<PortfolioResponse> response = investmentService.getPortfolios(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response, "포트폴리오 목록 조회 성공"));
    }

    @GetMapping("/{portfolioId}")
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolioDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long portfolioId) {
        PortfolioResponse response = investmentService.getPortfolioDetail(userDetails.getUserId(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success(response, "포트폴리오 상세 조회 성공"));
    }

    @GetMapping("/{portfolioId}/holdings")
    public ResponseEntity<ApiResponse<List<HoldingResponse>>> getHoldings(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long portfolioId) {
        List<HoldingResponse> response = investmentService.getHoldings(userDetails.getUserId(), portfolioId);
        return ResponseEntity.ok(ApiResponse.success(response, "보유 종목 조회 성공"));
    }
}
