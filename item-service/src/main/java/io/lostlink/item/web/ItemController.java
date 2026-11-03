package io.lostlink.item.web;

import io.lostlink.item.domain.ItemCategory;
import io.lostlink.item.domain.ItemStatus;
import io.lostlink.item.domain.ItemType;
import io.lostlink.item.dto.ItemRequest;
import io.lostlink.item.dto.ItemResponse;
import io.lostlink.item.dto.ItemStatusRequest;
import io.lostlink.item.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {
    private final ItemService service;
    private final byte[] serviceKey;
    public ItemController(ItemService service, @Value("${INTERNAL_SERVICE_KEY}") String serviceKey) {
        this.service = service;
        this.serviceKey = serviceKey.getBytes(StandardCharsets.UTF_8);
    }

    @PatchMapping("/internal/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeStatusInternal(@PathVariable UUID id, @Valid @RequestBody ItemStatusRequest request,
                                     @RequestHeader(name = "X-Internal-Service-Key", required = false) String key) {
        if (key == null || !MessageDigest.isEqual(serviceKey, key.getBytes(StandardCharsets.UTF_8))) {
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Internal service authentication required");
        }
        service.changeStatusFromTrustedService(id, request.status());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse create(@Valid @RequestBody ItemRequest request) { return service.create(request); }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable UUID id) { return service.get(id); }

    @GetMapping
    public Page<ItemResponse> list(@RequestParam(required = false) ItemCategory category,
                                   @RequestParam(required = false) ItemType itemType,
                                   @RequestParam(required = false) ItemStatus status,
                                   @RequestParam(required = false) String location,
                                   @RequestParam(required = false) String q,
                                   @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return service.list(category, itemType, status, location, q, pageable);
    }

    @GetMapping("/search")
    public Page<ItemResponse> search(@RequestParam(required = false) ItemCategory category,
                                     @RequestParam(required = false) ItemType itemType,
                                     @RequestParam(required = false) ItemStatus status,
                                     @RequestParam(required = false) String location,
                                     @RequestParam(required = false) String q,
                                     @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return service.list(category, itemType, status, location, q, pageable);
    }

    @GetMapping("/my-items")
    public Page<ItemResponse> mine(@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return service.mine(pageable);
    }

    @PutMapping("/{id}")
    public ItemResponse update(@PathVariable UUID id, @Valid @RequestBody ItemRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public ItemResponse changeStatus(@PathVariable UUID id, @Valid @RequestBody ItemStatusRequest request) {
        return service.changeStatus(id, request.status());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) { service.delete(id); }
}
