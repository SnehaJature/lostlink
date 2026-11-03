package io.lostlink.notification.dto;

import io.lostlink.notification.domain.NotificationRecord;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, String type, String title, String message,
                                   UUID relatedEntityId, boolean read, Instant createdAt) {
    public static NotificationResponse from(NotificationRecord notification) {
        return new NotificationResponse(notification.getId(), notification.getType(), notification.getTitle(),
                notification.getMessage(), notification.getRelatedEntityId(), notification.isRead(),
                notification.getCreatedAt());
    }
}
