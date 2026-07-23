package com.restapi.wmsservice.dto.response;

import com.restapi.wmsservice.enums.NotificationType;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder
public class NotificationResponse {
    Long id;
    String eventId;
    String recipientUsername;
    NotificationType eventType;
    String title;
    String message;
    String referenceType;
    String referenceId;
    LocalDateTime readAt;
    LocalDateTime createdAt;
}
