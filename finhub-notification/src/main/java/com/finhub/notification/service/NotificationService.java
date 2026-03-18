package com.finhub.notification.service;

import com.finhub.notification.domain.NotificationType;
import com.finhub.notification.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    Page<NotificationResponse> getNotifications(Long userId, Pageable pageable);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long userId, Long notificationId);

    void createNotification(Long userId, NotificationType type, String title, String message);
}
