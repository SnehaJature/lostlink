package io.lostlink.claim.dto;

import java.util.UUID;

public record MatchDetails(UUID id, UUID lostItemId, UUID foundItemId, UUID lostItemOwnerId,
                           UUID foundItemOwnerId, String status) {}
