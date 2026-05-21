package com.hostelmanagement.service;

import com.hostelmanagement.dto.response.NotificationResponse;
import com.hostelmanagement.entity.User;

import java.util.List;

public interface NotificationService {
    void sendNotification(User user, String message);
    List<NotificationResponse> getMyNotifications(Long userId);
    void markAllAsRead(Long userId);
    long getUnreadCount(Long userId);
}
