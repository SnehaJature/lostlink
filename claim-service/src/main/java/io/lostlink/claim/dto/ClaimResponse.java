package io.lostlink.claim.dto;

import io.lostlink.claim.domain.ClaimRecord;

import java.time.Instant;
import java.util.UUID;

public record ClaimResponse(UUID id, UUID matchId, UUID itemId, UUID claimantId, UUID finderId,
                            String description, String proofDetails, String status,
                            Instant createdAt, Instant updatedAt) {
    public static ClaimResponse from(ClaimRecord claim) {
        return new ClaimResponse(claim.getId(), claim.getMatchId(), claim.getItemId(), claim.getClaimantId(),
                claim.getFinderId(), claim.getDescription(), claim.getProofDetails(), claim.getStatus().name(),
                claim.getCreatedAt(), claim.getUpdatedAt());
    }
}
