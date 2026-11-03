package io.lostlink.claim.client;

import io.lostlink.claim.dto.MatchDetails;
import io.lostlink.claim.dto.MatchStatusUpdate;
import io.lostlink.common.client.AuthForwardingConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "matching-service", path = "/api/v1/matches",
        configuration = AuthForwardingConfiguration.class)
public interface MatchingClient {
    @GetMapping("/{matchId}")
    MatchDetails get(@PathVariable UUID matchId);
    @PatchMapping("/{matchId}/status")
    void updateStatus(@PathVariable UUID matchId, @RequestBody MatchStatusUpdate request);
}
