package io.lostlink.item.service;

import feign.FeignException;
import io.lostlink.common.dto.ItemSnapshot;
import io.lostlink.common.security.CurrentUser;
import io.lostlink.item.client.MatchingClient;
import io.lostlink.item.domain.ItemCategory;
import io.lostlink.item.domain.ItemReport;
import io.lostlink.item.domain.ItemStatus;
import io.lostlink.item.domain.ItemType;
import io.lostlink.item.dto.ItemRequest;
import io.lostlink.item.dto.ItemResponse;
import io.lostlink.item.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class ItemService {
    private static final Logger log = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository items;
    private final MatchingClient matchingClient;

    public ItemService(ItemRepository items, MatchingClient matchingClient) {
        this.items = items;
        this.matchingClient = matchingClient;
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        ItemReport item = items.save(new ItemReport(request.title().trim(), request.description().trim(),
                request.category(), request.itemType(), request.location().trim(), request.itemDate(),
                request.imageUrl(), CurrentUser.id()));
        ItemResponse response = ItemResponse.from(item);
        publish(response.snapshot());
        return response;
    }

    @Transactional(readOnly = true)
    public ItemResponse get(UUID id) {
        return items.findById(id).map(ItemResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> list(ItemCategory category, ItemType type, ItemStatus status, String location,
                                   String term, Pageable pageable) {
        return items.search(category, type, status, blankToNull(location), blankToNull(term), pageable)
                .map(ItemResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<ItemResponse> mine(Pageable pageable) {
        return items.findByReportedBy(CurrentUser.id(), pageable).map(ItemResponse::from);
    }

    @Transactional
    public ItemResponse update(UUID id, ItemRequest request) {
        ItemReport item = ownedItem(id);
        item.update(request.title().trim(), request.description().trim(), request.category(), request.itemType(),
                request.location().trim(), request.itemDate(), request.imageUrl());
        ItemReport saved = items.save(item);
        ItemResponse response = ItemResponse.from(saved);
        publish(response.snapshot());
        return response;
    }

    @Transactional
    public ItemResponse changeStatus(UUID id, ItemStatus status) {
        ItemReport item = ownedItem(id);
        item.setStatus(status);
        ItemReport saved = items.save(item);
        ItemResponse response = ItemResponse.from(saved);
        publish(response.snapshot());
        return response;
    }

    @Transactional
    public void changeStatusFromTrustedService(UUID id, ItemStatus status) {
        ItemReport item = items.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        item.setStatus(status);
        items.save(item);
    }

    @Transactional
    public void delete(UUID id) {
        ItemReport item = ownedItem(id);
        item.setStatus(ItemStatus.CLOSED);
        ItemReport saved = items.save(item);
        publish(ItemResponse.from(saved).snapshot());
        items.delete(saved);
    }

    private ItemReport ownedItem(UUID id) {
        ItemReport item = items.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));
        if (!CurrentUser.isAdmin() && !item.getReportedBy().equals(CurrentUser.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the item owner or an administrator can change this item");
        }
        return item;
    }

    private void publish(ItemSnapshot snapshot) {
        try {
            matchingClient.publishItem(snapshot);
        } catch (FeignException exception) {
            log.warn("Item {} was saved, but matching service could not process its update: {}",
                    snapshot.id(), exception.status());
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
