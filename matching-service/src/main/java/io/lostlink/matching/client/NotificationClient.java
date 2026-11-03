package io.lostlink.matching.client;

import io.lostlink.common.client.InternalServiceAuthConfiguration;
import io.lostlink.matching.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", path = "/api/v1/notifications/internal",
        configuration = InternalServiceAuthConfiguration.class)
public interface NotificationClient {
    @PostMapping
    void create(@RequestBody NotificationRequest request);
}
