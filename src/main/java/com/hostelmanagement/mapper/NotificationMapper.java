package com.hostelmanagement.mapper;

import com.hostelmanagement.dto.response.NotificationResponse;
import com.hostelmanagement.entity.Notification;

public class NotificationMapper {

    public static NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
