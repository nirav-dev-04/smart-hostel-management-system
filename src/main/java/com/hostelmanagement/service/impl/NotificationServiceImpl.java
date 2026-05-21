package com.hostelmanagement.service.impl;

import com.hostelmanagement.dto.response.NotificationResponse;
import com.hostelmanagement.entity.Notification;
import com.hostelmanagement.entity.User;
import com.hostelmanagement.mapper.NotificationMapper;
import com.hostelmanagement.notification.NotificationWebSocketHandler;
import com.hostelmanagement.repository.NotificationRepository;
import com.hostelmanagement.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationWebSocketHandler webSocketHandler;

    public NotificationServiceImpl(NotificationRepository notificationRepository, NotificationWebSocketHandler webSocketHandler) {
        this.notificationRepository = notificationRepository;
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    @Transactional
    public void sendNotification(User user, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notification);

        // Push real-time alert via WebSockets
        webSocketHandler.sendNotification(user.getId(), message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NotificationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
        notifications.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(notifications);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }
}
