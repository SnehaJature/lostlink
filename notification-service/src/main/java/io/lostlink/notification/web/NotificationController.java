package io.lostlink.notification.web;

import io.lostlink.notification.dto.NotificationRequest;
import io.lostlink.notification.dto.NotificationResponse;
import io.lostlink.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;
    private final byte[] serviceKey;

    public NotificationController(NotificationService service, @Value("${INTERNAL_SERVICE_KEY}") String serviceKey) {
        this.service = service;
        this.serviceKey = serviceKey.getBytes(StandardCharsets.UTF_8);
    }

    @PostMapping("/internal")
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse createInternal(@Valid @RequestBody NotificationRequest request,
                                               @RequestHeader(name = "X-Internal-Service-Key", required = false) String key) {
        if (key == null || !MessageDigest.isEqual(serviceKey, key.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal service authentication required");
        }
        return service.create(request);
    }

    @GetMapping
    public List<NotificationResponse> mine() { return service.mine(); }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable UUID id) { return service.markRead(id); }

    @PatchMapping("/read-all")
    public ReadAllResponse markAllRead() { return new ReadAllResponse(service.markAllRead()); }

    public record ReadAllResponse(int updatedCount) {}
}
