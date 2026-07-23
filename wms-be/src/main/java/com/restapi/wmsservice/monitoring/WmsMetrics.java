package com.restapi.wmsservice.monitoring;

import com.restapi.wmsservice.event.RealtimeBusinessEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class WmsMetrics {

    private static final String BUSINESS_OPERATION = "wms.business.operation";
    private static final String BUSINESS_EVENTS = "wms.business.events";
    private static final String NOTIFICATIONS_PERSISTED = "wms.notifications.persisted";
    private static final String WEBSOCKET_MESSAGES_PUBLISHED = "wms.websocket.messages.published";
    private static final String NOTIFICATION_FAILURES = "wms.notifications.failures";

    private final MeterRegistry meterRegistry;

    public Timer.Sample startOperation() {
        return Timer.start(meterRegistry);
    }

    public void completeOperation(Timer.Sample sample, String operation, String outcome) {
        sample.stop(Timer.builder(BUSINESS_OPERATION)
                .description("Duration and outcome of critical WMS business operations")
                .tag("operation", operation)
                .tag("outcome", outcome)
                .register(meterRegistry));
    }

    public void recordBusinessEvent(RealtimeBusinessEvent event) {
        Counter.builder(BUSINESS_EVENTS)
                .description("Committed WMS state transition events")
                .tag("event_type", event.eventType().name().toLowerCase(Locale.ROOT))
                .tag("module", moduleName(event.referenceType()))
                .register(meterRegistry)
                .increment();
    }

    public void recordNotificationsPersisted(int count) {
        increment(NOTIFICATIONS_PERSISTED, "Notifications persisted after a business event", count);
    }

    public void recordWebSocketMessagePublished() {
        increment(WEBSOCKET_MESSAGES_PUBLISHED, "Messages published to the user WebSocket broker", 1);
    }

    public void recordNotificationFailure() {
        increment(NOTIFICATION_FAILURES, "Failures while persisting or publishing notifications", 1);
    }

    private void increment(String name, String description, int count) {
        if (count <= 0) {
            return;
        }
        Counter.builder(name)
                .description(description)
                .register(meterRegistry)
                .increment(count);
    }

    private String moduleName(String referenceType) {
        if (referenceType == null || referenceType.isBlank()) {
            return "unknown";
        }
        return referenceType.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
    }
}
