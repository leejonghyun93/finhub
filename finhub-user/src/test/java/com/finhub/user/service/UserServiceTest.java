package com.finhub.user.service;

import com.finhub.user.domain.Role;
import com.finhub.user.domain.User;
import com.finhub.user.dto.request.LoginRequest;
import com.finhub.user.dto.request.ReissueRequest;
import com.finhub.user.dto.request.SignupRequest;
import com.finhub.user.dto.response.LoginResponse;
import com.finhub.user.dto.response.TokenResponse;
import com.finhub.user.dto.response.UserInfoResponse;
import com.finhub.user.exception.CustomException;
import com.finhub.user.exception.ErrorCode;
import com.finhub.user.repository.RefreshTokenRepository;
import com.finhub.user.repository.UserRepository;
import com.finhub.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock JwtTokenProvider jwtTokenProvider;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@finhub.com")
                .password("encodedPassword123!")
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .role(Role.ROLE_USER)
                .build();
    }

    // ── 회원가입 ────────────────────────────────────────────────
    @Nested
    @DisplayName("signup() 회원가입")
    class SignupTest {

        @Test
        @DisplayName("정상 회원가입 성공")
        void signup_success() {
            // given
            SignupRequest request = new SignupRequest("new@finhub.com", "password123!", "김철수", "010-9999-0000");
            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("encodedPw");

            // when
            userService.signup(request);

            // then
            then(userRepository).should().save(any(User.class));
        }

        @Test
        @DisplayName("이미 존재하는 이메일로 가입 시 EMAIL_ALREADY_EXISTS 예외 발생")
        void signup_duplicateEmail_throwsException() {
            // given
            SignupRequest request = new SignupRequest("test@finhub.com", "password123!", "홍길동", "010-0000-0000");
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.signup(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS));

            then(userRepository).should(never()).save(any());
        }
    }

    // ── 로그인 ────────────────────────────────────────────────
    @Nested
    @DisplayName("login() 로그인")
    class LoginTest {

        @Test
        @DisplayName("정상 로그인 성공 — AccessToken과 RefreshToken 반환")
        void login_success() {
            // given
            LoginRequest request = new LoginRequest("test@finhub.com", "password123!");
            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(request.password(), testUser.getPassword())).willReturn(true);
            given(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).willReturn("access.token.jwt");
            given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("refresh.token.jwt");
            given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(604800000L);

            // when
            LoginResponse response = userService.login(request);

            // then
            assertThat(response.accessToken()).isEqualTo("access.token.jwt");
            assertThat(response.refreshToken()).isEqualTo("refresh.token.jwt");
            then(refreshTokenRepository).should().save(eq("test@finhub.com"), eq("refresh.token.jwt"), eq(604800000L));
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 시 USER_NOT_FOUND 예외 발생")
        void login_userNotFound_throwsException() {
            // given
            LoginRequest request = new LoginRequest("notfound@finhub.com", "password123!");
            given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 시 INVALID_PASSWORD 예외 발생")
        void login_invalidPassword_throwsException() {
            // given
            LoginRequest request = new LoginRequest("test@finhub.com", "wrongPassword!");
            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(testUser));
            given(passwordEncoder.matches(request.password(), testUser.getPassword())).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_PASSWORD));
        }
    }

    // ── 로그아웃 ───────────────────────────────────────────────
    @Nested
    @DisplayName("logout() 로그아웃")
    class LogoutTest {

        @Test
        @DisplayName("정상 로그아웃 — RefreshToken 삭제")
        void logout_success() {
            // when
            userService.logout("test@finhub.com");

            // then
            then(refreshTokenRepository).should().delete("test@finhub.com");
        }
    }

    // ── 토큰 재발급 ────────────────────────────────────────────
    @Nested
    @DisplayName("reissue() 토큰 재발급")
    class ReissueTest {

        @Test
        @DisplayName("유효한 RefreshToken으로 재발급 성공")
        void reissue_success() {
            // given
            ReissueRequest request = new ReissueRequest("valid.refresh.token");
            given(jwtTokenProvider.validateToken("valid.refresh.token")).willReturn(true);
            given(jwtTokenProvider.getEmail("valid.refresh.token")).willReturn("test@finhub.com");
            given(refreshTokenRepository.findByEmail("test@finhub.com")).willReturn(Optional.of("valid.refresh.token"));
            given(userRepository.findByEmail("test@finhub.com")).willReturn(Optional.of(testUser));
            given(jwtTokenProvider.generateAccessToken(any(), anyString(), anyString())).willReturn("new.access.token");
            given(jwtTokenProvider.generateRefreshToken(anyString())).willReturn("new.refresh.token");
            given(jwtTokenProvider.getRefreshTokenExpiration()).willReturn(604800000L);

            // when
            TokenResponse response = userService.reissue(request);

            // then
            assertThat(response.accessToken()).isEqualTo("new.access.token");
            assertThat(response.refreshToken()).isEqualTo("new.refresh.token");
        }

        @Test
        @DisplayName("만료된 RefreshToken으로 재발급 시 INVALID_TOKEN 예외 발생")
        void reissue_invalidToken_throwsException() {
            // given
            ReissueRequest request = new ReissueRequest("expired.refresh.token");
            given(jwtTokenProvider.validateToken("expired.refresh.token")).willReturn(false);

            // when & then
            assertThatThrownBy(() -> userService.reissue(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_TOKEN));
        }

        @Test
        @DisplayName("Redis에 저장된 토큰과 불일치 시 INVALID_TOKEN 예외 발생")
        void reissue_tokenMismatch_throwsException() {
            // given
            ReissueRequest request = new ReissueRequest("client.token");
            given(jwtTokenProvider.validateToken("client.token")).willReturn(true);
            given(jwtTokenProvider.getEmail("client.token")).willReturn("test@finhub.com");
            given(refreshTokenRepository.findByEmail("test@finhub.com")).willReturn(Optional.of("different.stored.token"));

            // when & then
            assertThatThrownBy(() -> userService.reissue(request))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_TOKEN));
        }
    }

    // ── 내 정보 조회 ───────────────────────────────────────────
    @Nested
    @DisplayName("getMyInfo() 내 정보 조회")
    class GetMyInfoTest {

        @Test
        @DisplayName("정상 내 정보 조회 성공")
        void getMyInfo_success() {
            // given
            given(userRepository.findByEmail("test@finhub.com")).willReturn(Optional.of(testUser));

            // when
            UserInfoResponse response = userService.getMyInfo("test@finhub.com");

            // then
            assertThat(response.email()).isEqualTo("test@finhub.com");
            assertThat(response.name()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("존재하지 않는 이메일 조회 시 USER_NOT_FOUND 예외 발생")
        void getMyInfo_userNotFound_throwsException() {
            // given
            given(userRepository.findByEmail("ghost@finhub.com")).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getMyInfo("ghost@finhub.com"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(ErrorCode.USER_NOT_FOUND));
        }
    }
}
