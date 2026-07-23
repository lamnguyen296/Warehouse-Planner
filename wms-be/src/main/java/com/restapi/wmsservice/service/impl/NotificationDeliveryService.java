package com.restapi.wmsservice.service.impl;

import com.restapi.wmsservice.dto.response.NotificationResponse;
import com.restapi.wmsservice.entity.Notification;
import com.restapi.wmsservice.event.RealtimeBusinessEvent;
import com.restapi.wmsservice.mapper.NotificationMapper;
import com.restapi.wmsservice.repository.NotificationRepository;
import com.restapi.wmsservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationMapper notificationMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<NotificationResponse> persist(RealtimeBusinessEvent event) {
        Set<String> recipients = new LinkedHashSet<>(event.directRecipients());
        for (String permission : event.recipientPermissions()) {
            recipients.addAll(userRepository.findActiveUsernamesByPermission(permission));
        }
        recipients.removeIf(username -> username == null || username.isBlank());

        List<Notification> notifications = recipients.stream()
                .map(username -> toNotification(event, username))
                .toList();
        return notificationRepository.saveAll(notifications).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    private Notification toNotification(RealtimeBusinessEvent event, String recipient) {
        Notification notification = new Notification();
        notification.setEventId(event.eventId());
        notification.setRecipientUsername(recipient);
        notification.setEventType(event.eventType());
        notification.setTitle(event.title());
        notification.setMessage(event.message());
        notification.setReferenceType(event.referenceType());
        notification.setReferenceId(event.referenceId());
        return notification;
    }
}
