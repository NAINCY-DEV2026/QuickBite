package com.quickbite.backend.service;

import com.quickbite.backend.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {
    void notify(Long userId, String message, String type);
    List<NotificationResponse> getForUser(Long userId);
    long getUnreadCount(Long userId);
    void markAsRead(Long userId, Long notificationId);
    void markAllAsRead(Long userId);
}