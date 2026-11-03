package io.lostlink.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record NotificationRequest(@NotNull UUID userId, @NotBlank @Size(max = 40) String type,
                                 @NotBlank @Size(max = 200) String title,
                                 @NotBlank @Size(max = 2000) String message,
                                 UUID relatedEntityId) {}
