package io.lostlink.matching.dto;

import java.util.UUID;

public record NotificationRequest(UUID userId, String type, String title, String message, UUID relatedEntityId) {}
