package io.lostlink.matching.web;

import io.lostlink.common.dto.ItemSnapshot;
import io.lostlink.matching.domain.MatchStatus;
import io.lostlink.matching.dto.MatchResponse;
import io.lostlink.matching.dto.MatchStatusRequest;
import io.lostlink.matching.service.MatchingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matches")
public class MatchingController {
    private final MatchingService service;
    public MatchingController(MatchingService service) { this.service = service; }

    @PostMapping("/internal/item-event")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void itemEvent(@RequestBody ItemSnapshot snapshot) { service.processItem(snapshot); }

    @GetMapping("/item/{itemId}")
    public List<MatchResponse> forItem(@PathVariable UUID itemId) { return service.forItem(itemId); }

    @GetMapping("/my-matches")
    public List<MatchResponse> mine() { return service.mine(); }

    @GetMapping("/{matchId}")
    public MatchResponse get(@PathVariable UUID matchId) { return service.get(matchId); }

    @PatchMapping("/{matchId}/status")
    public MatchResponse changeStatus(@PathVariable UUID matchId, @Valid @RequestBody MatchStatusRequest request) {
        return service.changeStatus(matchId, request.status());
    }
}
