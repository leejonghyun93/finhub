package com.finhub.search.security;

import com.finhub.search.exception.CustomException;
import com.finhub.search.exception.ErrorCode;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey key;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("JWT 파싱 에러: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        Object userId = Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("userId");
        if (userId == null) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return ((Number) userId).longValue();
    }

    public String getEmail(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public String getRole(String token) {
        return (String) Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload().get("role");
    }
}
