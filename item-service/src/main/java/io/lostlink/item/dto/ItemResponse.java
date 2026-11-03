package io.lostlink.item.dto;

import io.lostlink.common.dto.ItemSnapshot;
import io.lostlink.item.domain.ItemReport;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ItemResponse(UUID id, String title, String description, String category, String itemType,
                          String location, LocalDate itemDate, String imageUrl, String status,
                          UUID reportedBy, Instant createdAt, Instant updatedAt) {
    public static ItemResponse from(ItemReport item) {
        return new ItemResponse(item.getId(), item.getTitle(), item.getDescription(), item.getCategory().name(),
                item.getItemType().name(), item.getLocation(), item.getItemDate(), item.getImageUrl(),
                item.getStatus().name(), item.getReportedBy(), item.getCreatedAt(), item.getUpdatedAt());
    }
    public ItemSnapshot snapshot() {
        return new ItemSnapshot(id, title, description, category, itemType, location, itemDate, imageUrl, status, reportedBy);
    }
}
