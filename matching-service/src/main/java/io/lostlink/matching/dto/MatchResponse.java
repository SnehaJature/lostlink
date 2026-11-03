package io.lostlink.matching.dto;

import io.lostlink.matching.domain.MatchRecord;
import io.lostlink.matching.domain.StoredItem;

import java.time.Instant;
import java.util.UUID;

public record MatchResponse(UUID id, UUID itemOne, UUID itemTwo, UUID lostItemId, UUID foundItemId,
                            UUID lostItemOwnerId, UUID foundItemOwnerId, int score, String status,
                            Instant createdAt, Instant updatedAt) {
    public static MatchResponse from(MatchRecord match, StoredItem first, StoredItem second) {
        StoredItem lost = first.getItemType().equals("LOST") ? first : second;
        StoredItem found = first.getItemType().equals("FOUND") ? first : second;
        return new MatchResponse(match.getId(), match.getItemOne(), match.getItemTwo(), lost.getId(), found.getId(),
                lost.getReportedBy(), found.getReportedBy(), match.getScore(), match.getStatus().name(),
                match.getCreatedAt(), match.getUpdatedAt());
    }
}
