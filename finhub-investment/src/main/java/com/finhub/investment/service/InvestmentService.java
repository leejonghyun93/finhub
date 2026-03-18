package com.finhub.investment.service;

import com.finhub.investment.dto.request.CreatePortfolioRequest;
import com.finhub.investment.dto.request.TradeRequest;
import com.finhub.investment.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InvestmentService {

    List<StockResponse> getStocks();

    StockResponse getStock(Long stockId);

    void trade(Long userId, TradeRequest request);

    Page<TradeHistoryResponse> getTradeHistory(Long userId, Pageable pageable);

    PortfolioResponse createPortfolio(Long userId, CreatePortfolioRequest request);

    List<PortfolioResponse> getPortfolios(Long userId);

    PortfolioResponse getPortfolioDetail(Long userId, Long portfolioId);

    List<HoldingResponse> getHoldings(Long userId, Long portfolioId);
}
