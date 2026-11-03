package io.lostlink.matching.service;

import feign.FeignException;
import io.lostlink.common.dto.ItemSnapshot;
import io.lostlink.common.security.CurrentUser;
import io.lostlink.matching.client.NotificationClient;
import io.lostlink.matching.domain.MatchRecord;
import io.lostlink.matching.domain.MatchStatus;
import io.lostlink.matching.domain.StoredItem;
import io.lostlink.matching.dto.MatchResponse;
import io.lostlink.matching.dto.NotificationRequest;
import io.lostlink.matching.repository.MatchRepository;
import io.lostlink.matching.repository.StoredItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class MatchingService {
    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);
    private final StoredItemRepository items;
    private final MatchRepository matches;
    private final MatchingScoreCalculator calculator;
    private final NotificationClient notifications;

    public MatchingService(StoredItemRepository items, MatchRepository matches, MatchingScoreCalculator calculator,
                           NotificationClient notifications) {
        this.items = items;
        this.matches = matches;
        this.calculator = calculator;
        this.notifications = notifications;
    }

    @Transactional
    public void processItem(ItemSnapshot snapshot) {
        if (!CurrentUser.isAdmin() && !CurrentUser.id().equals(snapshot.reportedBy())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the reporting user can publish an item update");
        }
        StoredItem stored = items.save(StoredItem.from(snapshot));
        refreshExistingSuggestions(stored);
        if (!"OPEN".equals(snapshot.status())) return;
        for (StoredItem candidate : items.findByStatus("OPEN")) {
            if (candidate.getId().equals(stored.getId()) || candidate.getItemType().equals(stored.getItemType())) continue;
            ItemSnapshot candidateSnapshot = snapshot(candidate);
            int score = calculator.calculate(snapshot, candidateSnapshot);
            if (score < 50) continue;
            UUID first = stored.getId().compareTo(candidate.getId()) < 0 ? stored.getId() : candidate.getId();
            UUID second = first.equals(stored.getId()) ? candidate.getId() : stored.getId();
            if (matches.existsByItemOneAndItemTwo(first, second)) continue;
            MatchRecord match = matches.save(new MatchRecord(first, second, score));
            notifyUser(stored.getReportedBy(), match.getId());
            if (!stored.getReportedBy().equals(candidate.getReportedBy())) {
                notifyUser(candidate.getReportedBy(), match.getId());
            }
        }
    }

    private void refreshExistingSuggestions(StoredItem changedItem) {
        List<MatchRecord> existingMatches = matches
                .findByItemOneOrItemTwoOrderByScoreDesc(changedItem.getId(), changedItem.getId());
        for (MatchRecord match : existingMatches) {
            if (match.getStatus() != MatchStatus.SUGGESTED) continue;
            UUID otherId = match.getItemOne().equals(changedItem.getId()) ? match.getItemTwo() : match.getItemOne();
            StoredItem other = items.findById(otherId).orElse(null);
            if (other == null || !"OPEN".equals(changedItem.getStatus()) || !"OPEN".equals(other.getStatus())
                    || changedItem.getItemType().equals(other.getItemType())) {
                matches.delete(match);
                continue;
            }
            int revisedScore = calculator.calculate(snapshot(changedItem), snapshot(other));
            if (revisedScore < 50) {
                matches.delete(match);
            } else if (revisedScore != match.getScore()) {
                match.updateScore(revisedScore);
                matches.save(match);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> forItem(UUID itemId) {
        return matches.findByItemOneOrItemTwoOrderByScoreDesc(itemId, itemId).stream()
                .map(this::response)
                .filter(this::visibleToCurrentUser)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MatchResponse> mine() {
        UUID userId = CurrentUser.id();
        return matches.findAll().stream()
                .filter(match -> participant(match, userId))
                .sorted(Comparator.comparingInt(MatchRecord::getScore).reversed())
                .map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public MatchResponse get(UUID matchId) {
        MatchResponse response = response(matches.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found")));
        if (!visibleToCurrentUser(response)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this match");
        }
        return response;
    }

    @Transactional
    public MatchResponse changeStatus(UUID matchId, MatchStatus status) {
        MatchRecord match = matches.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Match not found"));
        MatchResponse current = response(match);
        if (!visibleToCurrentUser(current)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not a participant in this match");
        }
        if (match.getStatus() == status) return current;
        if (match.getStatus() == MatchStatus.REJECTED || match.getStatus() == MatchStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A rejected or completed match cannot be changed");
        }
        match.setStatus(status);
        return response(matches.save(match));
    }

    private MatchResponse response(MatchRecord match) {
        StoredItem first = items.findById(match.getItemOne())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matched item snapshot not found"));
        StoredItem second = items.findById(match.getItemTwo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Matched item snapshot not found"));
        return MatchResponse.from(match, first, second);
    }

    private boolean visibleToCurrentUser(MatchResponse match) {
        return CurrentUser.isAdmin() || CurrentUser.id().equals(match.lostItemOwnerId())
                || CurrentUser.id().equals(match.foundItemOwnerId());
    }

    private boolean participant(MatchRecord match, UUID userId) {
        StoredItem first = items.findById(match.getItemOne()).orElse(null);
        StoredItem second = items.findById(match.getItemTwo()).orElse(null);
        return first != null && second != null
                && (first.getReportedBy().equals(userId) || second.getReportedBy().equals(userId));
    }

    private ItemSnapshot snapshot(StoredItem item) {
        return new ItemSnapshot(item.getId(), item.getTitle(), item.getDescription(), item.getCategory(),
                item.getItemType(), item.getLocation(), item.getItemDate(), item.getImageUrl(), item.getStatus(),
                item.getReportedBy());
    }

    private void notifyUser(UUID userId, UUID matchId) {
        try {
            notifications.create(new NotificationRequest(userId, "POSSIBLE_MATCH", "A possible item match was found",
                    "A new item report may match one of your items.", matchId));
        } catch (FeignException exception) {
            log.warn("Match {} was saved, but notification delivery failed with status {}", matchId, exception.status());
        }
    }
}
