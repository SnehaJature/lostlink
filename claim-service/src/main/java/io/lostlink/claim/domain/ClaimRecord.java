package io.lostlink.claim.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "claims",
        uniqueConstraints = @UniqueConstraint(name = "uk_active_claimant_match", columnNames = "active_claim_key"),
        indexes = {@Index(name = "idx_claim_claimant", columnList = "claimant_id"),
                @Index(name = "idx_claim_finder", columnList = "finder_id")})
public class ClaimRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "match_id", nullable = false)
    private UUID matchId;
    @Column(name = "item_id", nullable = false)
    private UUID itemId;
    @Column(name = "claimant_id", nullable = false)
    private UUID claimantId;
    @Column(name = "finder_id", nullable = false)
    private UUID finderId;
    @Column(nullable = false, length = 2000)
    private String description;
    @Column(name = "proof_details", nullable = false, length = 5000)
    private String proofDetails;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ClaimStatus status = ClaimStatus.PENDING;
    @Column(name = "active_claim_key", unique = true)
    private String activeClaimKey;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    protected ClaimRecord() {}

    public ClaimRecord(UUID matchId, UUID itemId, UUID claimantId, UUID finderId, String description, String proofDetails) {
        this.matchId = matchId;
        this.itemId = itemId;
        this.claimantId = claimantId;
        this.finderId = finderId;
        this.description = description;
        this.proofDetails = proofDetails;
        this.activeClaimKey = matchId + ":" + claimantId;
    }

    public void setStatus(ClaimStatus status) {
        this.status = status;
        if (status == ClaimStatus.REJECTED || status == ClaimStatus.CANCELLED) activeClaimKey = null;
    }

    @PrePersist void onCreate() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate void onUpdate() { updatedAt = Instant.now(); }
    public UUID getId() { return id; }
    public UUID getMatchId() { return matchId; }
    public UUID getItemId() { return itemId; }
    public UUID getClaimantId() { return claimantId; }
    public UUID getFinderId() { return finderId; }
    public String getDescription() { return description; }
    public String getProofDetails() { return proofDetails; }
    public String getActiveClaimKey() { return activeClaimKey; }
    public ClaimStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
