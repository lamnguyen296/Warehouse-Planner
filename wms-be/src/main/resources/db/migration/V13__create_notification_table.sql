CREATE TABLE notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id VARCHAR(36) NOT NULL,
    recipient_username VARCHAR(255) NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_type VARCHAR(50),
    reference_id VARCHAR(64),
    read_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6),
    CONSTRAINT uk_notification_event_recipient UNIQUE (event_id, recipient_username),
    INDEX idx_notification_recipient_created (recipient_username, created_at),
    INDEX idx_notification_recipient_read (recipient_username, read_at)
);
