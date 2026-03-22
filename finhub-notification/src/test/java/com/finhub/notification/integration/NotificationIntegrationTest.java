package com.finhub.notification.integration;

import com.finhub.notification.domain.Notification;
import com.finhub.notification.domain.NotificationType;
import com.finhub.notification.repository.NotificationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * finhub-notification 통합 테스트
 *
 * - H2 인메모리 DB (application-test.yml)
 * - Kafka: listener.auto-startup=false (실제 브로커 불필요)
 * - Redis: 자동구성 제외 (서비스 로직에서 미사용)
 * - Eureka: disabled
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Notification 서비스 통합 테스트")
class NotificationIntegrationTest {

    private static final String JWT_SECRET =
            "finhub-secret-key-must-be-at-least-256-bits-long-for-hs256";
    private static final AtomicInteger userCounter = new AtomicInteger(40);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    private Long testUserId;
    private Long testNotificationId;

    @BeforeEach
    void setUp() {
        testUserId = (long) userCounter.incrementAndGet();

        Notification notification = notificationRepository.save(Notification.builder()
                .userId(testUserId)
                .type(NotificationType.PAYMENT_COMPLETED)
                .title("결제 완료")
                .message("스타벅스 5,500원 결제가 완료되었습니다.")
                .isRead(false)
                .build());
        testNotificationId = notification.getId();
    }

    // ── 1. 알림 목록 조회 ─────────────────────────────────────────────────

    @Test
    @DisplayName("알림 목록 조회 성공 — 1건 반환")
    void getNotifications_returnsOneItem() throws Exception {
        String token = generateToken(testUserId, "notif" + testUserId + "@finhub.com");

        mockMvc.perform(get("/api/v1/notification")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].id").value(testNotificationId))
                .andExpect(jsonPath("$.data.content[0].isRead").value(false));
    }

    // ── 2. 단건 알림 읽음 처리 ───────────────────────────────────────────

    @Test
    @DisplayName("알림 읽음 처리 성공 후 200 OK 반환")
    void markAsRead_success() throws Exception {
        String token = generateToken(testUserId, "notif" + testUserId + "@finhub.com");

        mockMvc.perform(patch("/api/v1/notification/" + testNotificationId + "/read")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("알림 읽음 처리 성공"));
    }

    // ── 3. 전체 알림 읽음 처리 ───────────────────────────────────────────

    @Test
    @DisplayName("전체 알림 읽음 처리 성공 후 200 OK 반환")
    void markAllAsRead_success() throws Exception {
        String token = generateToken(testUserId, "notif" + testUserId + "@finhub.com");

        mockMvc.perform(patch("/api/v1/notification/read-all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 읽음 처리 성공"));
    }

    // ── 4. 알림 삭제 ──────────────────────────────────────────────────────

    @Test
    @DisplayName("알림 삭제 성공 후 200 OK 반환")
    void deleteNotification_success() throws Exception {
        String token = generateToken(testUserId, "notif" + testUserId + "@finhub.com");

        mockMvc.perform(delete("/api/v1/notification/" + testNotificationId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("알림 삭제 성공"));
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
