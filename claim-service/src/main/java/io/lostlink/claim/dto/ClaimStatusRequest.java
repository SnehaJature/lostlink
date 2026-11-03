package io.lostlink.claim.dto;

import io.lostlink.claim.domain.ClaimStatus;
import jakarta.validation.constraints.NotNull;

public record ClaimStatusRequest(@NotNull ClaimStatus status) {}
