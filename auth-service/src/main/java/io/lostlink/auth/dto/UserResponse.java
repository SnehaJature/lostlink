package io.lostlink.auth.dto;

import io.lostlink.auth.domain.UserAccount;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String fullName, String email, String role, Instant createdAt, Instant updatedAt) {
    public static UserResponse from(UserAccount user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
