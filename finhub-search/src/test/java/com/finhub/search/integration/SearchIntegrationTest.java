package com.finhub.search.integration;

import com.finhub.search.document.FinancialProductDocument;
import com.finhub.search.repository.FinancialProductRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * finhub-search 통합 테스트
 *
 * - Elasticsearch: @MockBean FinancialProductRepository 로 대체 (실제 ES 불필요)
 * - Eureka: disabled
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Search 서비스 통합 테스트")
class SearchIntegrationTest {

    private static final String JWT_SECRET =
            "finhub-secret-key-must-be-at-least-256-bits-long-for-hs256";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FinancialProductRepository financialProductRepository;

    // ── 1. 키워드 검색 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("키워드 검색 시 매칭 결과 반환")
    void search_byKeyword_returnsMatchingResults() throws Exception {
        String token = generateToken(10L, "search1@finhub.com");

        FinancialProductDocument doc = FinancialProductDocument.builder()
                .id("prod-1")
                .type("FUND")
                .name("국내 주식형 펀드")
                .description("안정적인 수익을 추구하는 펀드 상품")
                .extraInfo("최소 가입금액 100만원")
                .build();

        when(financialProductRepository.findByNameContainingOrDescriptionContaining(
                eq("펀드"), eq("펀드"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(doc)));

        mockMvc.perform(get("/api/v1/search")
                        .param("keyword", "펀드")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("국내 주식형 펀드"))
                .andExpect(jsonPath("$.data.content[0].type").value("FUND"));
    }

    // ── 2. 파라미터 없이 전체 조회 ────────────────────────────────────────

    @Test
    @DisplayName("검색 파라미터 없이 전체 조회 시 빈 결과 반환")
    void search_noParams_returnsEmptyPage() throws Exception {
        String token = generateToken(11L, "search2@finhub.com");

        when(financialProductRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/search")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());
    }

    // ── 3. 카테고리 필터 검색 ─────────────────────────────────────────────

    @Test
    @DisplayName("카테고리 필터로 해당 타입 상품만 반환")
    void search_byCategory_returnsFilteredResults() throws Exception {
        String token = generateToken(12L, "search3@finhub.com");

        FinancialProductDocument doc = FinancialProductDocument.builder()
                .id("prod-2")
                .type("DEPOSIT")
                .name("정기예금 상품")
                .description("연 3.5% 금리 정기예금")
                .extraInfo("6개월 ~ 24개월")
                .build();

        when(financialProductRepository.findByType(eq("DEPOSIT"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(doc)));

        mockMvc.perform(get("/api/v1/search")
                        .param("category", "DEPOSIT")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$.data.content[0].name").value("정기예금 상품"));
    }

    // ── 헬퍼 메서드 ───────────────────────────────────────────────────────

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
