package io.lostlink.item.dto;

import io.lostlink.item.domain.ItemStatus;
import jakarta.validation.constraints.NotNull;

public record ItemStatusRequest(@NotNull ItemStatus status) {}
