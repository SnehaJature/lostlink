package io.lostlink.claim.service;

import io.lostlink.claim.client.InternalItemClient;
import io.lostlink.claim.client.MatchingClient;
import io.lostlink.claim.client.NotificationClient;
import io.lostlink.claim.domain.ClaimRecord;
import io.lostlink.claim.domain.ClaimStatus;
import io.lostlink.claim.dto.ClaimStatusRequest;
import io.lostlink.claim.dto.MatchDetails;
import io.lostlink.claim.dto.MatchStatusUpdate;
import io.lostlink.claim.repository.ClaimRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ClaimServiceTest {
    private final ClaimRepository claims = mock(ClaimRepository.class);
    private final MatchingClient matching = mock(MatchingClient.class);
    private final InternalItemClient items = mock(InternalItemClient.class);
    private final NotificationClient notifications = mock(NotificationClient.class);
    private final ClaimService service = new ClaimService(claims, matching, items, notifications);
    private final UUID claimId = UUID.randomUUID();
    private final UUID matchId = UUID.randomUUID();
    private final UUID claimantId = UUID.randomUUID();
    private final UUID finderId = UUID.randomUUID();
    private final UUID foundItemId = UUID.randomUUID();
    private final UUID lostItemId = UUID.randomUUID();
    private final ClaimRecord claim = new ClaimRecord(matchId, foundItemId, claimantId, finderId,
            "I can identify the item", "Private purchase details");

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void finderApprovalCompletesMatchAndReturnsBothItems() {
        authenticate(finderId, "ROLE_USER");
        when(claims.findById(claimId)).thenReturn(Optional.of(claim));
        when(claims.save(claim)).thenReturn(claim);
        when(matching.get(matchId)).thenReturn(new MatchDetails(matchId, lostItemId, foundItemId,
                claimantId, finderId, "CLAIM_IN_PROGRESS"));

        var response = service.changeStatus(claimId, new ClaimStatusRequest(ClaimStatus.APPROVED));

        assertEquals("APPROVED", response.status());
        verify(items).updateStatus(eq(foundItemId), argThat(status -> status.status().equals("RETURNED")));
        verify(items).updateStatus(eq(lostItemId), argThat(status -> status.status().equals("RETURNED")));
        verify(matching).updateStatus(eq(matchId), argThat(status -> status.status().equals("COMPLETED")));
        verify(notifications, times(4)).create(any());
    }

    @Test
    void unrelatedUserCannotReadProofDetails() {
        authenticate(UUID.randomUUID(), "ROLE_USER");
        when(claims.findById(claimId)).thenReturn(Optional.of(claim));

        ResponseStatusException error = assertThrows(ResponseStatusException.class, () -> service.get(claimId));

        assertEquals(403, error.getStatusCode().value());
    }

    @Test
    void finderCanRejectPendingClaim() {
        authenticate(finderId, "ROLE_USER");
        when(claims.findById(claimId)).thenReturn(Optional.of(claim));
        when(claims.save(claim)).thenReturn(claim);

        var response = service.changeStatus(claimId, new ClaimStatusRequest(ClaimStatus.REJECTED));

        assertEquals("REJECTED", response.status());
        verify(matching).updateStatus(eq(matchId), argThat(status -> status.status().equals("REJECTED")));
        verify(notifications, times(2)).create(any());
    }

    private void authenticate(UUID id, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                id.toString(), null, List.of(new SimpleGrantedAuthority(role))));
    }
}
