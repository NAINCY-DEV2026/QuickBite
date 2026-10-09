package com.quickbite.backend.service.impl;

import com.quickbite.backend.dto.response.NotificationResponse;
import com.quickbite.backend.entity.Notification;
import com.quickbite.backend.entity.User;
import com.quickbite.backend.exception.ResourceNotFoundException;
import com.quickbite.backend.exception.UnauthorizedException;
import com.quickbite.backend.mapper.NotificationMapper;
import com.quickbite.backend.repository.NotificationRepository;
import com.quickbite.backend.repository.UserRepository;
import com.quickbite.backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Joins the caller's transaction, so a notification is only saved if the business action commits
    @Override
    @Transactional
    public void notify(Long userId, String message, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationRepository.save(Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getForUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!n.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("This notification does not belong to you");
        }
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.findByUserIdAndIsReadFalse(userId)
                .forEach(n -> n.setIsRead(true));
    }
}