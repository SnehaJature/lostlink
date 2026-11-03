package io.lostlink.claim.client;

import io.lostlink.claim.dto.ItemStatusUpdate;
import io.lostlink.common.client.InternalServiceAuthConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@FeignClient(name = "item-service", path = "/api/v1/items/internal",
        configuration = InternalServiceAuthConfiguration.class)
public interface InternalItemClient {
    @PatchMapping("/{itemId}/status")
    void updateStatus(@PathVariable UUID itemId, @RequestBody ItemStatusUpdate request);
}
