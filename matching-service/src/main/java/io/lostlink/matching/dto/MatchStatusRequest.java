package io.lostlink.matching.dto;

import io.lostlink.matching.domain.MatchStatus;
import jakarta.validation.constraints.NotNull;

public record MatchStatusRequest(@NotNull MatchStatus status) {}
