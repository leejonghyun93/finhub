package com.finhub.banking.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finhub.banking.dto.request.CreateAccountRequest;
import com.finhub.banking.dto.request.DepositRequest;
import com.finhub.banking.dto.request.TransferRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * finhub-banking 통합 테스트
 *
 * - H2 인메모리 DB (application-test.yml)
 * - Kafka: @MockBean KafkaTemplate 으로 대체 (실제 브로커 불필요)
 * - Eureka: disabled
 * - JWT: JJWT로 테스트 토큰 직접 생성 (banking JwtTokenProvider와 동일한 시크릿 사용)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Banking 서비스 통합 테스트")
class BankingIntegrationTest {

    private static final String JWT_SECRET =
            "finhub-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final String BASE_URL = "/api/v1/banking/accounts";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    private String user1Token;   // userId=1
    private String user2Token;   // userId=2

    @BeforeEach
    void setUp() {
        user1Token = generateToken(1L, "user1@finhub.com");
        user2Token = generateToken(2L, "user2@finhub.com");
    }

    // ── 1. 계좌 개설 → 잔액 충전 → 계좌 목록 조회 플로우 ─────────────────────

    @Test
    @DisplayName("계좌 개설 → 잔액 충전 → 계좌 목록 조회 전체 플로우 성공")
    void createAccount_deposit_getAccounts_fullFlow() throws Exception {
        // 1단계: 계좌 개설
        CreateAccountRequest createRequest = new CreateAccountRequest("통합테스트 통장");
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accountName").value("통합테스트 통장"))
                .andExpect(jsonPath("$.data.balance").value(0))
                .andReturn();

        long accountId = extractAccountId(createResult);

        // 2단계: 잔액 충전 50,000원
        DepositRequest depositRequest = new DepositRequest(new BigDecimal("50000"));
        mockMvc.perform(post(BASE_URL + "/" + accountId + "/deposit")
                        .header("Authorization", "Bearer " + user1Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(depositRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balance").value(50000));

        // 3단계: 계좌 목록 조회 — 잔액 반영 확인
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", "Bearer " + user1Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].balance").value(50000))
                .andExpect(jsonPath("$.data[0].accountName").value("통합테스트 통장"));
    }

    // ── 2. 잔액 부족 송금 실패 ────────────────────────────────────────────────

    @Test
    @DisplayName("잔액 부족 시 송금 실패 — 400 BAD REQUEST 반환")
    void transfer_insufficientBalance_returnsBadRequest() throws Exception {
        // 송금자(userId=3) 계좌 개설 — 잔액 0원
        String senderToken = generateToken(3L, "sender@finhub.com");
        MvcResult senderResult = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccountRequest("송금자 통장"))))
                .andExpect(status().isCreated())
                .andReturn();
        long senderAccountId = extractAccountId(senderResult);

        // 수신자(userId=4) 계좌 개설 — 계좌번호 확보
        String receiverToken = generateToken(4L, "receiver@finhub.com");
        MvcResult receiverResult = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + receiverToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccountRequest("수신자 통장"))))
                .andExpect(status().isCreated())
                .andReturn();
        String receiverAccountNumber = extractAccountNumber(receiverResult);

        // 잔액(0원) < 송금액(10,000원) → INSUFFICIENT_BALANCE → 400
        TransferRequest transferRequest = new TransferRequest(
                senderAccountId, receiverAccountNumber, new BigDecimal("10000"), "테스트 송금");
        mockMvc.perform(post(BASE_URL + "/transfer")
                        .header("Authorization", "Bearer " + senderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("잔액이 부족합니다."));
    }

    // ── 3. 거래내역 페이징 조회 ───────────────────────────────────────────────

    @Test
    @DisplayName("입금 후 거래내역 페이징 조회 성공")
    void getTransactions_afterDeposit_returnsPagedResults() throws Exception {
        String txToken = generateToken(5L, "tx@finhub.com");

        // 계좌 개설
        MvcResult createResult = mockMvc.perform(post(BASE_URL)
                        .header("Authorization", "Bearer " + txToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateAccountRequest("거래내역 계좌"))))
                .andExpect(status().isCreated())
                .andReturn();
        long accountId = extractAccountId(createResult);

        // 입금 2회
        DepositRequest deposit = new DepositRequest(new BigDecimal("10000"));
        for (int i = 0; i < 2; i++) {
            mockMvc.perform(post(BASE_URL + "/" + accountId + "/deposit")
                            .header("Authorization", "Bearer " + txToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deposit)))
                    .andExpect(status().isOk());
        }

        // 거래내역 조회 — 2건 반환
        // Spring Boot 3.x: Page는 { content: [...], page: { totalElements, totalPages, ... } } 구조로 직렬화
        mockMvc.perform(get(BASE_URL + "/" + accountId + "/transactions")
                        .header("Authorization", "Bearer " + txToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.page.totalElements").value(2));
    }

    // ── 헬퍼 메서드 ──────────────────────────────────────────────────────────

    /**
     * 테스트용 JWT 생성.
     * banking JwtTokenProvider와 동일한 시크릿·클레임 구조 사용.
     */
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

    private long extractAccountId(MvcResult result) throws Exception {
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("id").asLong();
    }

    private String extractAccountNumber(MvcResult result) throws Exception {
        JsonNode data = objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
        return data.path("accountNumber").asText();
    }
}
