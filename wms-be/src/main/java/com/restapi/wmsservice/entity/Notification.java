package com.restapi.wmsservice.entity;

import com.restapi.wmsservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_event_recipient",
                columnNames = {"event_id", "recipient_username"}))
@Getter
@Setter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "event_id", nullable = false, length = 36)
    String eventId;

    @Column(name = "recipient_username", nullable = false)
    String recipientUsername;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 64)
    NotificationType eventType;

    @Column(nullable = false, length = 150)
    String title;

    @Column(nullable = false, length = 500)
    String message;

    @Column(name = "reference_type", length = 50)
    String referenceType;

    @Column(name = "reference_id", length = 64)
    String referenceId;

    @Column(name = "read_at")
    LocalDateTime readAt;
}
