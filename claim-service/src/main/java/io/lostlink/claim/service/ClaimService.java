package io.lostlink.claim.service;

import feign.FeignException;
import io.lostlink.claim.client.InternalItemClient;
import io.lostlink.claim.client.MatchingClient;
import io.lostlink.claim.client.NotificationClient;
import io.lostlink.claim.domain.ClaimRecord;
import io.lostlink.claim.domain.ClaimStatus;
import io.lostlink.claim.dto.ClaimRequest;
import io.lostlink.claim.dto.ClaimResponse;
import io.lostlink.claim.dto.ClaimStatusRequest;
import io.lostlink.claim.dto.ItemStatusUpdate;
import io.lostlink.claim.dto.MatchDetails;
import io.lostlink.claim.dto.MatchStatusUpdate;
import io.lostlink.claim.dto.NotificationRequest;
import io.lostlink.claim.repository.ClaimRepository;
import io.lostlink.common.security.CurrentUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class ClaimService {
    private static final Logger log = LoggerFactory.getLogger(ClaimService.class);
    private final ClaimRepository claims;
    private final MatchingClient matching;
    private final InternalItemClient items;
    private final NotificationClient notifications;

    public ClaimService(ClaimRepository claims, MatchingClient matching, InternalItemClient items,
                        NotificationClient notifications) {
        this.claims = claims;
        this.matching = matching;
        this.items = items;
        this.notifications = notifications;
    }

    @Transactional
    public ClaimResponse create(ClaimRequest request) {
        MatchDetails match = getMatch(request.matchId());
        UUID claimantId = CurrentUser.id();
        if (!claimantId.equals(match.lostItemOwnerId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the owner of the lost item can submit this claim");
        }
        if (!"SUGGESTED".equals(match.status()) && !"ACCEPTED".equals(match.status())
                && !"CLAIM_IN_PROGRESS".equals(match.status())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This match is not available for a new claim");
        }
        ClaimRecord saved = claims.save(new ClaimRecord(match.id(), match.foundItemId(), claimantId,
                match.foundItemOwnerId(), request.description().trim(), request.proofDetails().trim()));
        try {
            matching.updateStatus(match.id(), new MatchStatusUpdate("CLAIM_IN_PROGRESS"));
        } catch (FeignException exception) {
            log.error("Claim {} saved, but match {} could not be updated (HTTP {})", saved.getId(), match.id(), exception.status());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not submit the claim because matching coordination failed; retry after service recovery");
        }
        notifyUser(match.foundItemOwnerId(), saved.getId(), "CLAIM_SUBMITTED", "A claim was submitted",
                "A user submitted an ownership claim for an item you reported as found.");
        return ClaimResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public ClaimResponse get(UUID id) {
        ClaimRecord claim = findClaim(id);
        if (!authorized(claim)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to view this claim");
        }
        return ClaimResponse.from(claim);
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> mine() {
        return claims.findByClaimantIdOrderByCreatedAtDesc(CurrentUser.id()).stream().map(ClaimResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ClaimResponse> reviewQueue() {
        UUID userId = CurrentUser.id();
        List<ClaimRecord> reviewable = claims.findByFinderIdOrderByCreatedAtDesc(userId);
        if (CurrentUser.isAdmin()) return claims.findAll().stream().map(ClaimResponse::from).toList();
        return reviewable.stream().map(ClaimResponse::from).toList();
    }

    @Transactional
    public ClaimResponse changeStatus(UUID id, ClaimStatusRequest request) {
        ClaimRecord claim = findClaim(id);
        ClaimStatus target = request.status();
        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only pending claims can be updated");
        }
        if (target == ClaimStatus.CANCELLED) {
            if (!claim.getClaimantId().equals(CurrentUser.id()) && !CurrentUser.isAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the claimant or an administrator can cancel this claim");
            }
        } else if (target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED) {
            if (!claim.getFinderId().equals(CurrentUser.id()) && !CurrentUser.isAdmin()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the finder or an administrator can review this claim");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported claim status transition");
        }

        claim.setStatus(target);
        ClaimRecord saved = claims.save(claim);
        if (target == ClaimStatus.APPROVED) {
            completeReturn(claim);
        } else if (target == ClaimStatus.REJECTED) {
            updateMatch(claim.getMatchId(), "REJECTED");
        }
        if (target == ClaimStatus.APPROVED || target == ClaimStatus.REJECTED) {
            String result = target == ClaimStatus.APPROVED ? "approved" : "rejected";
            notifyUser(claim.getClaimantId(), claim.getId(), "CLAIM_" + target.name(),
                    "Your claim was " + result, "The finder has " + result + " your ownership claim.");
            notifyUser(claim.getFinderId(), claim.getId(), "CLAIM_" + target.name(),
                    "Claim review recorded", "You have " + result + " the ownership claim.");
            if (target == ClaimStatus.APPROVED) {
                notifyUser(claim.getClaimantId(), claim.getItemId(), "ITEM_RETURNED",
                        "Item marked as returned", "The found item and its lost-item report have been marked returned.");
                notifyUser(claim.getFinderId(), claim.getItemId(), "ITEM_RETURNED",
                        "Item marked as returned", "The found item and its lost-item report have been marked returned.");
            }
        }
        return ClaimResponse.from(saved);
    }

    private void completeReturn(ClaimRecord claim) {
        try {
            MatchDetails match = getMatch(claim.getMatchId());
            items.updateStatus(claim.getItemId(), new ItemStatusUpdate("RETURNED"));
            items.updateStatus(match.lostItemId(), new ItemStatusUpdate("RETURNED"));
            matching.updateStatus(claim.getMatchId(), new MatchStatusUpdate("COMPLETED"));
        } catch (FeignException exception) {
            log.error("Claim {} approved, but return status coordination failed (HTTP {})", claim.getId(), exception.status());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not complete return coordination; check downstream status before retrying");
        }
    }

    private void updateMatch(UUID matchId, String status) {
        try {
            matching.updateStatus(matchId, new MatchStatusUpdate(status));
        } catch (FeignException exception) {
            log.error("Claim status was saved but match {} could not be updated (HTTP {})", matchId, exception.status());
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not complete claim coordination; check downstream status before retrying");
        }
    }

    private MatchDetails getMatch(UUID matchId) {
        try {
            return matching.get(matchId);
        } catch (FeignException exception) {
            if (exception.status() == 404) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Potential match not found");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Matching service is unavailable");
        }
    }

    private ClaimRecord findClaim(UUID id) {
        return claims.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Claim not found"));
    }

    private boolean authorized(ClaimRecord claim) {
        return CurrentUser.isAdmin() || claim.getClaimantId().equals(CurrentUser.id())
                || claim.getFinderId().equals(CurrentUser.id());
    }

    private void notifyUser(UUID userId, UUID claimId, String type, String title, String message) {
        try {
            notifications.create(new NotificationRequest(userId, type, title, message, claimId));
        } catch (FeignException exception) {
            log.warn("Claim {} notification to {} failed with HTTP {}", claimId, userId, exception.status());
        }
    }
}
