package com.quickbite.backend.controller;

import com.quickbite.backend.dto.response.NotificationResponse;
import com.quickbite.backend.security.CurrentUserUtil;
import com.quickbite.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserUtil currentUserUtil;

    @GetMapping
    public List<NotificationResponse> getMine() {
        return notificationService.getForUser(currentUserUtil.getCurrentUserId());
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unreadCount() {
        return Map.of("count", notificationService.getUnreadCount(currentUserUtil.getCurrentUserId()));
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(currentUserUtil.getCurrentUserId(), id);
    }

    @PutMapping("/read-all")
    public void markAllAsRead() {
        notificationService.markAllAsRead(currentUserUtil.getCurrentUserId());
    }
}