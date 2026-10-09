package com.quickbite.backend.mapper;

import com.quickbite.backend.dto.response.NotificationResponse;
import com.quickbite.backend.entity.Notification;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .message(n.getMessage())
                .type(n.getType())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}