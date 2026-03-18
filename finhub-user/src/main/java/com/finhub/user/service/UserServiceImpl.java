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
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .name(request.name())
                .phoneNumber(request.phoneNumber())
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getEmail());
        refreshTokenRepository.save(user.getEmail(), refreshToken, jwtTokenProvider.getRefreshTokenExpiration());

        return LoginResponse.of(accessToken, refreshToken);
    }

    @Override
    public void logout(String email) {
        refreshTokenRepository.delete(email);
    }

    @Override
    public TokenResponse reissue(ReissueRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String email = jwtTokenProvider.getEmail(refreshToken);

        String storedToken = refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), email, user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);
        refreshTokenRepository.save(email, newRefreshToken, jwtTokenProvider.getRefreshTokenExpiration());

        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return UserInfoResponse.from(user);
    }
}
