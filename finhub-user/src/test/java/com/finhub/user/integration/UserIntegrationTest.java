package com.finhub.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finhub.user.dto.request.LoginRequest;
import com.finhub.user.dto.request.SignupRequest;
import com.finhub.user.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * finhub-user 통합 테스트
 *
 * - H2 인메모리 DB (application-test.yml)
 * - Redis: @MockBean RefreshTokenRepository 로 대체
 * - Eureka: disabled
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("User 서비스 통합 테스트")
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RefreshTokenRepository refreshTokenRepository;

    // ── 1. 전체 플로우 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("회원가입 → 로그인 → 내 정보 조회 전체 플로우 성공")
    void signupLoginGetMyInfo_fullFlow_success() throws Exception {
        String email = "flow@finhub.com";

        // 1단계: 회원가입
        SignupRequest signupRequest = new SignupRequest(email, "password123!", "플로우유저", "010-1111-0001");
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // 2단계: 로그인 → accessToken 추출
        LoginRequest loginRequest = new LoginRequest(email, "password123!");
        MvcResult loginResult = mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn();

        String accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();

        // 3단계: 내 정보 조회 (Gateway가 주입하는 X-User-* 헤더 방식)
        mockMvc.perform(get("/api/v1/users/me")
                        .header("X-User-Id", "1")
                        .header("X-User-Email", email)
                        .header("X-User-Role", "ROLE_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(email))
                .andExpect(jsonPath("$.data.name").value("플로우유저"));
    }

    // ── 2. 중복 이메일 ────────────────────────────────────────────────────────

    @Test
    @DisplayName("중복 이메일로 회원가입 시 409 CONFLICT 반환")
    void signup_duplicateEmail_returnsConflict() throws Exception {
        SignupRequest request = new SignupRequest("dup@finhub.com", "password123!", "홍길동", "010-2222-0002");

        // 첫 번째 가입 성공
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // 두 번째 가입 실패 — EMAIL_ALREADY_EXISTS → 409
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 사용 중인 이메일입니다."));
    }

    // ── 3. 잘못된 비밀번호 ────────────────────────────────────────────────────

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 UNAUTHORIZED 반환")
    void login_wrongPassword_returnsUnauthorized() throws Exception {
        // 회원가입
        SignupRequest signupRequest = new SignupRequest("wrong@finhub.com", "correctPassword!", "테스트유저", "010-3333-0003");
        mockMvc.perform(post("/api/v1/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isCreated());

        // 잘못된 비밀번호로 로그인 — INVALID_PASSWORD → 401
        LoginRequest loginRequest = new LoginRequest("wrong@finhub.com", "wrongPassword!");
        mockMvc.perform(post("/api/v1/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."));
    }

    // ── 4. 인증 없이 접근 ─────────────────────────────────────────────────────

    @Test
    @DisplayName("토큰 없이 인증 필요 API 접근 시 403 FORBIDDEN 반환")
    void getMyInfo_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }
}
