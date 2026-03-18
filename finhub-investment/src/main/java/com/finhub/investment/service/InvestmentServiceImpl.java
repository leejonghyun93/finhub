package com.finhub.investment.service;

import com.finhub.investment.domain.*;
import com.finhub.investment.dto.event.TradeCompletedEvent;
import com.finhub.investment.dto.request.CreatePortfolioRequest;
import com.finhub.investment.dto.request.TradeRequest;
import com.finhub.investment.dto.response.*;
import com.finhub.investment.exception.CustomException;
import com.finhub.investment.exception.ErrorCode;
import com.finhub.investment.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestmentServiceImpl implements InvestmentService {

    private static final String TRADE_TOPIC = "investment.trade.completed";

    private final StockRepository stockRepository;
    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final KafkaTemplate<String, TradeCompletedEvent> kafkaTemplate;

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> getStocks() {
        return stockRepository.findAll().stream()
                .map(StockResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StockResponse getStock(Long stockId) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new CustomException(ErrorCode.STOCK_NOT_FOUND));
        return StockResponse.from(stock);
    }

    @Override
    public void trade(Long userId, TradeRequest request) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(request.portfolioId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        Stock stock = stockRepository.findById(request.stockId())
                .orElseThrow(() -> new CustomException(ErrorCode.STOCK_NOT_FOUND));

        BigDecimal totalAmount = request.price().multiply(BigDecimal.valueOf(request.quantity()));

        if (request.tradeType() == TradeType.BUY) {
            executeBuy(portfolio, stock, request.quantity(), request.price());
        } else {
            executeSell(portfolio, stock, request.quantity());
        }

        tradeHistoryRepository.save(TradeHistory.builder()
                .portfolio(portfolio)
                .stock(stock)
                .userId(userId)
                .tradeType(request.tradeType())
                .quantity(request.quantity())
                .price(request.price())
                .totalAmount(totalAmount)
                .build());

        kafkaTemplate.send(TRADE_TOPIC, new TradeCompletedEvent(
                portfolio.getId(),
                stock.getId(),
                stock.getTicker(),
                stock.getName(),
                request.tradeType(),
                request.quantity(),
                request.price(),
                totalAmount,
                userId,
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TradeHistoryResponse> getTradeHistory(Long userId, Pageable pageable) {
        return tradeHistoryRepository.findByUserIdWithStock(userId, pageable)
                .map(TradeHistoryResponse::from);
    }

    @Override
    public PortfolioResponse createPortfolio(Long userId, CreatePortfolioRequest request) {
        Portfolio portfolio = Portfolio.builder()
                .userId(userId)
                .name(request.name())
                .description(request.description())
                .build();
        return PortfolioResponse.from(portfolioRepository.save(portfolio));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortfolioResponse> getPortfolios(Long userId) {
        return portfolioRepository.findByUserId(userId).stream()
                .map(PortfolioResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioDetail(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));
        return PortfolioResponse.from(portfolio);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoldingResponse> getHoldings(Long userId, Long portfolioId) {
        portfolioRepository.findByIdAndUserId(portfolioId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.PORTFOLIO_NOT_FOUND));

        return holdingRepository.findByPortfolioIdWithStock(portfolioId).stream()
                .map(HoldingResponse::from)
                .collect(Collectors.toList());
    }

    private void executeBuy(Portfolio portfolio, Stock stock, Long quantity, BigDecimal price) {
        holdingRepository.findByPortfolioIdAndStockId(portfolio.getId(), stock.getId())
                .ifPresentOrElse(
                        holding -> holding.buy(quantity, price),
                        () -> holdingRepository.save(Holding.builder()
                                .portfolio(portfolio)
                                .stock(stock)
                                .quantity(quantity)
                                .averagePrice(price)
                                .build())
                );
    }

    private void executeSell(Portfolio portfolio, Stock stock, Long quantity) {
        Holding holding = holdingRepository.findByPortfolioIdAndStockId(portfolio.getId(), stock.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.HOLDING_NOT_FOUND));

        holding.sell(quantity);

        if (holding.getQuantity() == 0) {
            holdingRepository.delete(holding);
        }
    }
}
