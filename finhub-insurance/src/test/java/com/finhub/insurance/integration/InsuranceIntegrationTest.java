package com.finhub.insurance.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finhub.insurance.domain.InsuranceCategory;
import com.finhub.insurance.domain.InsuranceProduct;
import com.finhub.insurance.dto.event.InsuranceSubscribedEvent;
import com.finhub.insurance.repository.InsuranceProductRepository;
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

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * finhub-insurance 통합 테스트
 *
 * - H2 인메모리 DB (application-test.yml)
 * - Kafka: @MockBean KafkaTemplate 으로 대체 (실제 브로커 불필요)
 * - Eureka: disabled
 * - 보험 상품: 실제 DB 없이 InsuranceProductRepository로 직접 저장 (Flyway 비활성)
 * - 인증: Gateway가 주입하는 X-User-* 헤더 방식
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Insurance 서비스 통합 테스트")
class InsuranceIntegrationTest {

    private static final AtomicInteger productCounter = new AtomicInteger(0);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, InsuranceSubscribedEvent> kafkaTemplate;

    @Autowired
    private InsuranceProductRepository productRepository;

    private Long testProductId;

    @BeforeEach
    void setUp() {
        int idx = productCounter.incrementAndGet();
        InsuranceProduct product = productRepository.save(InsuranceProduct.builder()
                .name("테스트 보험상품 " + idx)
                .category(InsuranceCategory.HEALTH)
                .description("통합 테스트용 보험 상품")
                .monthlyPremium(new BigDecimal("15000"))
                .coverageAmount(new BigDecimal("10000000"))
                .build());
        testProductId = product.getId();
    }

    // ── 1. 상품 목록 조회 → 가입 → 가입 내역 조회 플로우 ─────────────────────

    @Test
    @DisplayName("보험 상품 목록 조회 → 가입 → 가입 내역 조회 전체 플로우 성공")
    void getProducts_subscribe_getSubscriptions_fullFlow() throws Exception {
        // 1단계: 상품 목록 조회
        mockMvc.perform(get("/api/v1/insurance/products")
                        .header("X-User-Id", "30")
                        .header("X-User-Email", "ins1@finhub.com")
                        .header("X-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0]").exists());

        // 2단계: 보험 가입
        MvcResult subscribeResult = mockMvc.perform(post("/api/v1/insurance/subscribe")
                        .header("X-User-Id", "30")
                        .header("X-User-Email", "ins1@finhub.com")
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", testProductId))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        long subscriptionId = objectMapper.readTree(subscribeResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 3단계: 가입 내역 조회 — 1건, 가입 ID 확인
        mockMvc.perform(get("/api/v1/insurance/subscriptions")
                        .header("X-User-Id", "30")
                        .header("X-User-Email", "ins1@finhub.com")
                        .header("X-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(subscriptionId));
    }

    // ── 2. 이미 가입한 상품 중복 가입 실패 ───────────────────────────────────

    @Test
    @DisplayName("이미 가입한 보험 상품 중복 가입 시 409 CONFLICT 반환")
    void subscribe_duplicateProduct_returnsConflict() throws Exception {
        // 첫 번째 가입 성공
        mockMvc.perform(post("/api/v1/insurance/subscribe")
                        .header("X-User-Id", "31")
                        .header("X-User-Email", "ins2@finhub.com")
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", testProductId))))
                .andExpect(status().isCreated());

        // 두 번째 가입 실패 — ALREADY_SUBSCRIBED → 409
        mockMvc.perform(post("/api/v1/insurance/subscribe")
                        .header("X-User-Id", "31")
                        .header("X-User-Email", "ins2@finhub.com")
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", testProductId))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 가입한 보험 상품입니다."));
    }

    // ── 3. 보험 해지 ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("보험 가입 후 해지 성공")
    void subscribe_thenCancel_success() throws Exception {
        // 가입
        MvcResult subscribeResult = mockMvc.perform(post("/api/v1/insurance/subscribe")
                        .header("X-User-Id", "32")
                        .header("X-User-Email", "ins3@finhub.com")
                        .header("X-User-Role", "ROLE_USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("productId", testProductId))))
                .andExpect(status().isCreated())
                .andReturn();
        long subscriptionId = objectMapper.readTree(subscribeResult.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        // 해지 → 200 OK
        mockMvc.perform(delete("/api/v1/insurance/subscriptions/" + subscriptionId)
                        .header("X-User-Id", "32")
                        .header("X-User-Email", "ins3@finhub.com")
                        .header("X-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
