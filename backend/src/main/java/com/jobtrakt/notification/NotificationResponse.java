package com.jobtrakt.notification;

import java.time.Instant;
public record NotificationResponse(Long id, Long applicationId, NotificationType type, String title,
                                   String message, boolean read, Instant createdAt) {
    public static NotificationResponse from(NotificationRecord notification) {
        return new NotificationResponse(notification.getId(), notification.getApplication() == null ? null : notification.getApplication().getId(),
                notification.getType(), notification.getTitle(), notification.getMessage(), notification.isRead(), notification.getCreatedAt());
    }
}
