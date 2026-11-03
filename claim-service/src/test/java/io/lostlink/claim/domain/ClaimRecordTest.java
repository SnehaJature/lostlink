package io.lostlink.claim.domain;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClaimRecordTest {
    @Test
    void activeClaimsForTheSameUserAndMatchShareTheDatabaseUniqueKey() {
        UUID matchId = UUID.randomUUID();
        UUID claimantId = UUID.randomUUID();
        ClaimRecord first = new ClaimRecord(matchId, UUID.randomUUID(), claimantId,
                UUID.randomUUID(), "Description", "Proof");
        ClaimRecord duplicate = new ClaimRecord(matchId, UUID.randomUUID(), claimantId,
                UUID.randomUUID(), "Description", "Proof");

        assertEquals(first.getActiveClaimKey(), duplicate.getActiveClaimKey());

        first.setStatus(ClaimStatus.REJECTED);
        assertNull(first.getActiveClaimKey());
        assertNotNull(duplicate.getActiveClaimKey());
    }
}
