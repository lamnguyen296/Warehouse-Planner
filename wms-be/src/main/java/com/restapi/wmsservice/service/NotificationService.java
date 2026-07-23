package com.restapi.wmsservice.service;

import com.restapi.wmsservice.dto.response.NotificationResponse;
import com.restapi.wmsservice.dto.response.UnreadNotificationCountResponse;

import java.util.List;

public interface NotificationService {
    List<NotificationResponse> getMyNotifications();
    UnreadNotificationCountResponse getMyUnreadCount();
    NotificationResponse markAsRead(Long id);
    UnreadNotificationCountResponse markAllAsRead();
}
