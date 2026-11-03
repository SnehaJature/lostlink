package io.lostlink.matching.service;

import io.lostlink.common.dto.ItemSnapshot;
import io.lostlink.matching.client.NotificationClient;
import io.lostlink.matching.domain.MatchRecord;
import io.lostlink.matching.domain.StoredItem;
import io.lostlink.matching.repository.MatchRepository;
import io.lostlink.matching.repository.StoredItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MatchingServiceTest {
    private final StoredItemRepository items = mock(StoredItemRepository.class);
    private final MatchRepository matches = mock(MatchRepository.class);
    private final NotificationClient notifications = mock(NotificationClient.class);
    private final MatchingService service = new MatchingService(items, matches,
            new MatchingScoreCalculator(), notifications);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void onlyCreatesHighScoringMatchesForOppositeItemTypesAndNotifiesParticipants() {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        ItemSnapshot event = item(UUID.randomUUID(), userId, "LOST", "Black headphones");
        StoredItem candidate = StoredItem.from(item(UUID.randomUUID(), UUID.randomUUID(), "FOUND", "Black headphones"));
        when(items.save(any(StoredItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(items.findByStatus("OPEN")).thenReturn(List.of(candidate));
        when(matches.existsByItemOneAndItemTwo(any(), any())).thenReturn(false);
        when(matches.save(any(MatchRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.processItem(event);

        verify(matches).save(argThat(match -> match.getScore() == 100));
        verify(notifications, times(2)).create(any());
    }

    @Test
    void avoidsSameTypeAndDuplicateMatchRecords() {
        UUID userId = UUID.randomUUID();
        authenticate(userId);
        ItemSnapshot event = item(UUID.randomUUID(), userId, "FOUND", "Black headphones");
        StoredItem sameType = StoredItem.from(item(UUID.randomUUID(), UUID.randomUUID(), "FOUND", "Black headphones"));
        StoredItem alreadyMatched = StoredItem.from(item(UUID.randomUUID(), UUID.randomUUID(), "LOST", "Black headphones"));
        when(items.save(any(StoredItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(items.findByStatus("OPEN")).thenReturn(List.of(sameType, alreadyMatched));
        when(matches.existsByItemOneAndItemTwo(any(), any())).thenReturn(true);

        service.processItem(event);

        verify(matches, never()).save(any());
        verifyNoInteractions(notifications);
    }

    private void authenticate(UUID userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userId.toString(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER"))));
    }

    private ItemSnapshot item(UUID id, UUID owner, String type, String title) {
        return new ItemSnapshot(id, title, "Black wireless headphones with identifying details",
                "ELECTRONICS", type, "Office Cafeteria", LocalDate.of(2026, 11, 1), null, "OPEN", owner);
    }
}
