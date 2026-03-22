package com.finhub.user.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtTokenProvider 단위 테스트")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String TEST_SECRET =
            "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm";
    private static final long ACCESS_TOKEN_EXPIRATION = 1800000L;   // 30분
    private static final long REFRESH_TOKEN_EXPIRATION = 604800000L; // 7일

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(
                TEST_SECRET,
                ACCESS_TOKEN_EXPIRATION,
                REFRESH_TOKEN_EXPIRATION
        );
    }

    @Test
    @DisplayName("AccessToken 생성 후 유효성 검증 성공")
    void generateAccessToken_andValidate_success() {
        // when
        String token = jwtTokenProvider.generateAccessToken(1L, "user@finhub.com", "ROLE_USER");

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("AccessToken에서 이메일 추출 성공")
    void getEmail_fromAccessToken_success() {
        // given
        String token = jwtTokenProvider.generateAccessToken(1L, "user@finhub.com", "ROLE_USER");

        // when
        String email = jwtTokenProvider.getEmail(token);

        // then
        assertThat(email).isEqualTo("user@finhub.com");
    }

    @Test
    @DisplayName("RefreshToken 생성 후 유효성 검증 성공")
    void generateRefreshToken_andValidate_success() {
        // when
        String token = jwtTokenProvider.generateRefreshToken("user@finhub.com");

        // then
        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("위조된 토큰은 validateToken이 false 반환")
    void validateToken_forgedToken_returnsFalse() {
        // when
        boolean result = jwtTokenProvider.validateToken("this.is.a.forged.token");

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("빈 문자열 토큰은 validateToken이 false 반환")
    void validateToken_emptyToken_returnsFalse() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
        assertThat(jwtTokenProvider.validateToken(null)).isFalse();
    }

    @Test
    @DisplayName("getRefreshTokenExpiration이 설정값을 반환")
    void getRefreshTokenExpiration_returnsConfiguredValue() {
        assertThat(jwtTokenProvider.getRefreshTokenExpiration()).isEqualTo(REFRESH_TOKEN_EXPIRATION);
    }
}
