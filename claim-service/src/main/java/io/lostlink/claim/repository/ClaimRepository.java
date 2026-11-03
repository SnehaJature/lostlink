package io.lostlink.claim.repository;

import io.lostlink.claim.domain.ClaimRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClaimRepository extends JpaRepository<ClaimRecord, UUID> {
    List<ClaimRecord> findByClaimantIdOrderByCreatedAtDesc(UUID claimantId);
    List<ClaimRecord> findByFinderIdOrderByCreatedAtDesc(UUID finderId);
}
