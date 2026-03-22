package com.finhub.investment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finhub.investment.domain.Stock;
import com.finhub.investment.domain.TradeType;
import com.finhub.investment.dto.request.CreatePortfolioRequest;
import com.finhub.investment.dto.request.TradeRequest;
import com.finhub.investment.dto.event.TradeCompletedEvent;
import com.finhub.investment.repository.StockRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * finhub-investment 통합 테스트
 *
 * - H2 인메모리 DB (application-test.yml)
 * - Kafka: @MockBean KafkaTemplate 으로 대체 (실제 브로커 불필요)
 * - Eureka: disabled
 * - 주식 종목: 실제 DB 없이 StockRepository로 직접 저장 (Flyway 비활성)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Investment 서비스 통합 테스트")
class InvestmentIntegrationTest {

    private static final String JWT_SECRET =
            "finhub-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final AtomicInteger stockCounter = new AtomicInteger(0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, TradeCompletedEvent> kafkaTemplate;

    @Autowired
    private StockRepository stockRepository;

    private Long testStockId;

    @BeforeEach
    void setUp() {
        int idx = stockCounter.incrementAndGet();
        Stock stock = stockRepository.save(Stock.builder()
                .ticker("TST" + idx)
                .name("테스트주식" + idx)
                .currentPrice(new BigDecimal("75000"))
                .market("KOSPI")
                .build());
        testStockId = stock.getId();
    }

    // ── 1. 포트폴리오 생성 → 매수 → 보유 종목 조회 플로우 ─────────────────────

    @Test
    @DisplayName("포트폴리오 생성 → 주식 매수 → 보유 종목 조회 전체 플로우 성공")
    void createPortfolio_buy_getHoldings_fullFlow() throws Exception {
        String token = generateToken(10L, "inv1@finhub.com");

        // 1단계: 포트폴리오 생성
        MvcResult portfolioResult = mockMvc.perform(post("/api/v1/investment/portfolios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePortfolioRequest("테스트 포트폴리오", "설명"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("테스트 포트폴리오"))
                .andReturn();

        long portfolioId = objectMapper.readTree(portfolioResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2단계: 주식 매수 (5주, 75,000원)
        mockMvc.perform(post("/api/v1/investment/trade")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TradeRequest(portfolioId, testStockId, TradeType.BUY, 5L, new BigDecimal("75000")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // 3단계: 보유 종목 조회 — 1종목, 수량 5 확인
        mockMvc.perform(get("/api/v1/investment/portfolios/" + portfolioId + "/holdings")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].quantity").value(5));
    }

    // ── 2. 보유 수량 부족 매도 실패 ────────────────────────────────────────────

    @Test
    @DisplayName("보유 수량 부족 시 매도 실패 — 400 BAD REQUEST 반환")
    void sell_insufficientHolding_returnsBadRequest() throws Exception {
        String token = generateToken(11L, "inv2@finhub.com");

        // 포트폴리오 생성
        MvcResult portfolioResult = mockMvc.perform(post("/api/v1/investment/portfolios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePortfolioRequest("매도 테스트", null))))
                .andExpect(status().isCreated())
                .andReturn();
        long portfolioId = objectMapper.readTree(portfolioResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 매수 (2주)
        mockMvc.perform(post("/api/v1/investment/trade")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TradeRequest(portfolioId, testStockId, TradeType.BUY, 2L, new BigDecimal("75000")))))
                .andExpect(status().isOk());

        // 매도 (99주 — 보유 수량 2 초과) → INSUFFICIENT_HOLDING → 400
        mockMvc.perform(post("/api/v1/investment/trade")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new TradeRequest(portfolioId, testStockId, TradeType.SELL, 99L, new BigDecimal("75000")))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("보유 수량이 부족합니다."));
    }

    // ── 3. 매매 내역 페이징 조회 ──────────────────────────────────────────────

    @Test
    @DisplayName("매수 후 매매 내역 페이징 조회 성공")
    void getTradeHistory_afterBuys_returnsPagedResults() throws Exception {
        String token = generateToken(12L, "inv3@finhub.com");

        // 포트폴리오 생성
        MvcResult portfolioResult = mockMvc.perform(post("/api/v1/investment/portfolios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreatePortfolioRequest("내역 포트폴리오", null))))
                .andExpect(status().isCreated())
                .andReturn();
        long portfolioId = objectMapper.readTree(portfolioResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 매수 2회
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post("/api/v1/investment/trade")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new TradeRequest(portfolioId, testStockId, TradeType.BUY, 1L, new BigDecimal("75000")))))
                    .andExpect(status().isOk());
        }

        // 매매 내역 조회 — userId=12 기준 2건 반환
        mockMvc.perform(get("/api/v1/investment/trade/history")
                        .header("Authorization", "Bearer " + token)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.page.totalElements").value(2));
    }

    // ── 헬퍼 메서드 ──────────────────────────────────────────────────────────

    private String generateToken(Long userId, String email) {
        SecretKey key = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", "ROLE_USER")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000L))
                .signWith(key)
                .compact();
    }
}
