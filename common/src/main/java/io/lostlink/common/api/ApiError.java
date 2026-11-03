package io.lostlink.common.api;

import java.time.Instant;
import java.util.Map;

public record ApiError(Instant timestamp, int status, String error, String message, Map<String, String> validationErrors) {
    public ApiError(int status, String error, String message) {
        this(Instant.now(), status, error, message, Map.of());
    }
}
