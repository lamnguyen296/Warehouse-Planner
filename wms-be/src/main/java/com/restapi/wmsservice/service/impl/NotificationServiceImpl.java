package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.response.NotificationResponse;
import com.restapi.wmsservice.dto.response.UnreadNotificationCountResponse;
import com.restapi.wmsservice.entity.Notification;
import com.restapi.wmsservice.exception.AppException;
import com.restapi.wmsservice.exception.ErrorCode;
import com.restapi.wmsservice.mapper.NotificationMapper;
import com.restapi.wmsservice.repository.NotificationRepository;
import com.restapi.wmsservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        return notificationRepository.findTop50ByRecipientUsernameOrderByCreatedAtDesc(currentUsername())
                .stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadNotificationCountResponse getMyUnreadCount() {
        return new UnreadNotificationCountResponse(
                notificationRepository.countByRecipientUsernameAndReadAtIsNull(currentUsername()));
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = notificationRepository.findByIdAndRecipientUsername(id, currentUsername())
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public UnreadNotificationCountResponse markAllAsRead() {
        String username = currentUsername();
        notificationRepository.markAllRead(username, LocalDateTime.now());
        return new UnreadNotificationCountResponse(0);
    }

    private String currentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return authentication.getName();
    }
}
