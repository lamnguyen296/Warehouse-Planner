package com.restapi.wmsservice.event;

import com.restapi.wmsservice.enums.NotificationType;

import java.util.Set;

public record RealtimeBusinessEvent(
        String eventId,
        NotificationType eventType,
        String title,
        String message,
        String referenceType,
        String referenceId,
        Set<String> directRecipients,
        Set<String> recipientPermissions) {

    public RealtimeBusinessEvent {
        directRecipients = directRecipients == null ? Set.of() : Set.copyOf(directRecipients);
        recipientPermissions = recipientPermissions == null ? Set.of() : Set.copyOf(recipientPermissions);
    }
}
