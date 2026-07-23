package com.restapi.wmsservice.service;

import com.restapi.wmsservice.enums.NotificationType;
import com.restapi.wmsservice.event.RealtimeBusinessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(NotificationType eventType,
                        String title,
                        String message,
                        String referenceType,
                        Object referenceId,
                        Set<String> directRecipients,
                        Set<String> recipientPermissions) {
        applicationEventPublisher.publishEvent(new RealtimeBusinessEvent(
                UUID.randomUUID().toString(),
                eventType,
                title,
                message,
                referenceType,
                referenceId == null ? null : referenceId.toString(),
                directRecipients,
                recipientPermissions));
    }
}
