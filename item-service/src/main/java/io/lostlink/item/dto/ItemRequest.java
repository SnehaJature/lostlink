package io.lostlink.item.dto;

import io.lostlink.item.domain.ItemCategory;
import io.lostlink.item.domain.ItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ItemRequest(
        @NotBlank @Size(max = 160) String title,
        @NotBlank @Size(max = 5000) String description,
        @NotNull ItemCategory category,
        @NotNull ItemType itemType,
        @NotBlank @Size(max = 240) String location,
        @NotNull LocalDate itemDate,
        @Size(max = 2048) String imageUrl) {}
