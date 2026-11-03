package io.lostlink.common.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ItemSnapshot(UUID id, String title, String description, String category, String itemType,
                           String location, LocalDate itemDate, String imageUrl, String status,
                           UUID reportedBy) {}
