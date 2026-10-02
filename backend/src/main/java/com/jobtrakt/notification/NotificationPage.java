package com.jobtrakt.notification;

import com.jobtrakt.common.PageResponse;
public record NotificationPage(PageResponse<NotificationResponse> notifications, long unreadCount) { }
