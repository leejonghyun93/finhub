package com.finhub.user.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String KEY_PREFIX = "RT:";

    private final StringRedisTemplate redisTemplate;

    public void save(String email, String refreshToken, long expirationMs) {
        redisTemplate.opsForValue().set(
                KEY_PREFIX + email,
                refreshToken,
                expirationMs,
                TimeUnit.MILLISECONDS
        );
    }

    public Optional<String> findByEmail(String email) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(KEY_PREFIX + email));
    }

    public void delete(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}
