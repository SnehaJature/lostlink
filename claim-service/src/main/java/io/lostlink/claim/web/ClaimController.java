package io.lostlink.claim.web;

import io.lostlink.claim.dto.ClaimRequest;
import io.lostlink.claim.dto.ClaimResponse;
import io.lostlink.claim.dto.ClaimStatusRequest;
import io.lostlink.claim.service.ClaimService;
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
@RequestMapping("/api/v1/claims")
public class ClaimController {
    private final ClaimService service;
    public ClaimController(ClaimService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClaimResponse create(@Valid @RequestBody ClaimRequest request) { return service.create(request); }

    @GetMapping("/{claimId}")
    public ClaimResponse get(@PathVariable UUID claimId) { return service.get(claimId); }

    @GetMapping("/my-claims")
    public List<ClaimResponse> mine() { return service.mine(); }

    @GetMapping("/review-queue")
    public List<ClaimResponse> reviewQueue() { return service.reviewQueue(); }

    @PatchMapping("/{claimId}/status")
    public ClaimResponse changeStatus(@PathVariable UUID claimId, @Valid @RequestBody ClaimStatusRequest request) {
        return service.changeStatus(claimId, request);
    }
}
