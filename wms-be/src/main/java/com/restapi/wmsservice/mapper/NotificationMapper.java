package com.restapi.wmsservice.mapper;

import com.restapi.wmsservice.dto.response.NotificationResponse;
import com.restapi.wmsservice.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .eventId(notification.getEventId())
                .recipientUsername(notification.getRecipientUsername())
                .eventType(notification.getEventType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
