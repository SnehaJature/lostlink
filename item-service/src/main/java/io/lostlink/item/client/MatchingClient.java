package io.lostlink.item.client;

import io.lostlink.common.client.AuthForwardingConfiguration;
import io.lostlink.common.dto.ItemSnapshot;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "matching-service", path = "/api/v1/matches/internal",
        configuration = AuthForwardingConfiguration.class)
public interface MatchingClient {
    @PostMapping("/item-event")
    void publishItem(@RequestBody ItemSnapshot snapshot);
}
