package com.finhub.payment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finhub.payment.dto.event.PaymentCompletedEvent;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * finhub-payment 통합 테스트
 *
 * - H2 인메모리 DB (application-test.yml)
 * - Kafka: @MockBean KafkaTemplate 으로 대체 (실제 브로커 불필요)
 * - Eureka: disabled
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Payment 서비스 통합 테스트")
class PaymentIntegrationTest {

    private static final String JWT_SECRET =
            "finhub-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final String BASE_URL = "/api/v1/payment";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;

    // ── 1. 결제 수단 등록 → 결제 처리 → 내역 조회 플로우 ─────────────────────

    @Test
    @DisplayName("결제 수단 등록 → 결제 처리 → 내역 조회 전체 플로우 성공")
    void registerMethod_pay_getHistory_fullFlow() throws Exception {
        String token = generateToken(20L, "pay1@finhub.com");

        // 1단계: 결제 수단 등록
        MvcResult methodResult = mockMvc.perform(post(BASE_URL + "/methods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "methodType", "CARD",
                                "name", "신한카드",
                                "details", "1234-****",
                                "isDefault", true))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("신한카드"))
                .andReturn();

        long methodId = objectMapper.readTree(methodResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 2단계: 결제 처리 (50,000원)
        mockMvc.perform(post(BASE_URL + "/pay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "paymentMethodId", methodId,
                                "amount", 50000,
                                "description", "테스트 결제"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.amount").value(50000));

        // 3단계: 결제 내역 조회 — 1건 확인
        mockMvc.perform(get(BASE_URL + "/history")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].amount").value(50000));
    }

    // ── 2. 등록되지 않은 결제 수단으로 결제 실패 ─────────────────────────────

    @Test
    @DisplayName("등록되지 않은 결제 수단으로 결제 시 404 NOT FOUND 반환")
    void pay_unknownMethod_returnsNotFound() throws Exception {
        String token = generateToken(21L, "pay2@finhub.com");

        // 존재하지 않는 methodId=9999 → PAYMENT_METHOD_NOT_FOUND → 404
        mockMvc.perform(post(BASE_URL + "/pay")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "paymentMethodId", 9999,
                                "amount", 10000,
                                "description", "실패 결제"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("결제 수단을 찾을 수 없습니다."));
    }

    // ── 3. 결제 내역 페이징 조회 ─────────────────────────────────────────────

    @Test
    @DisplayName("결제 2회 후 내역 페이징 조회 성공")
    void getHistory_afterTwoPayments_returnsPagedResults() throws Exception {
        String token = generateToken(22L, "pay3@finhub.com");

        // 결제 수단 등록
        MvcResult methodResult = mockMvc.perform(post(BASE_URL + "/methods")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "methodType", "BANK_ACCOUNT",
                                "name", "국민은행",
                                "details", "110-123-456",
                                "isDefault", false))))
                .andExpect(status().isCreated())
                .andReturn();
        long methodId = objectMapper.readTree(methodResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 결제 2회
        for (int i = 1; i <= 2; i++) {
            mockMvc.perform(post(BASE_URL + "/pay")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(Map.of(
                                    "paymentMethodId", methodId,
                                    "amount", i * 10000,
                                    "description", "결제 " + i))))
                    .andExpect(status().isCreated());
        }

        // 내역 조회 — userId=22 기준 2건
        mockMvc.perform(get(BASE_URL + "/history")
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
