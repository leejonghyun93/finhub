package com.finhub.investment.controller;

import com.finhub.investment.dto.response.ApiResponse;
import com.finhub.investment.dto.response.StockResponse;
import com.finhub.investment.service.InvestmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/investment/stocks")
@RequiredArgsConstructor
public class StockController {

    private final InvestmentService investmentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StockResponse>>> getStocks() {
        return ResponseEntity.ok(ApiResponse.success(investmentService.getStocks(), "종목 목록 조회 성공"));
    }

    @GetMapping("/{stockId}")
    public ResponseEntity<ApiResponse<StockResponse>> getStock(@PathVariable Long stockId) {
        return ResponseEntity.ok(ApiResponse.success(investmentService.getStock(stockId), "종목 상세 조회 성공"));
    }
}
