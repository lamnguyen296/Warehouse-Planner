package com.restapi.wmsservice.event;

import com.restapi.wmsservice.dto.response.NotificationResponse;
import com.restapi.wmsservice.monitoring.WmsMetrics;
import com.restapi.wmsservice.service.impl.NotificationDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RealtimeBusinessEventListener {

    private final NotificationDeliveryService notificationDeliveryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WmsMetrics metrics;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onBusinessEvent(RealtimeBusinessEvent event) {
        metrics.recordBusinessEvent(event);
        try {
            List<NotificationResponse> notifications = notificationDeliveryService.persist(event);
            metrics.recordNotificationsPersisted(notifications.size());
            notifications.forEach(notification -> {
                messagingTemplate.convertAndSendToUser(
                        notification.getRecipientUsername(),
                        "/queue/notifications",
                        notification);
                metrics.recordWebSocketMessagePublished();
            });
        } catch (RuntimeException exception) {
            metrics.recordNotificationFailure();
            log.error("Unable to deliver notification [eventId={}, type={}]",
                    event.eventId(), event.eventType(), exception);
        }
    }
}
