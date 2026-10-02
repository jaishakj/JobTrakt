package com.jobtrakt.notification;

import com.jobtrakt.common.PageResponse;
import com.jobtrakt.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    public NotificationService(NotificationRepository notifications) { this.notifications = notifications; }

    @Transactional(readOnly = true)
    public NotificationPage list(Long userId, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new com.jobtrakt.common.exception.InvalidRequestException("Page must be non-negative and size must be between 1 and 100");
        var result = notifications.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(page, size));
        return new NotificationPage(PageResponse.from(result.map(NotificationResponse::from)), notifications.countByUserIdAndReadFalse(userId));
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long id) {
        var notification = notifications.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification"));
        notification.markRead();
        return NotificationResponse.from(notification);
    }

    @Transactional
    public long markAllRead(Long userId) { return notifications.markAllRead(userId); }
}
