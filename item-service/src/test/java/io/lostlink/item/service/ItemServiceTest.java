package io.lostlink.item.service;

import io.lostlink.common.security.CurrentUser;
import io.lostlink.item.client.MatchingClient;
import io.lostlink.item.domain.ItemCategory;
import io.lostlink.item.domain.ItemReport;
import io.lostlink.item.domain.ItemType;
import io.lostlink.item.dto.ItemRequest;
import io.lostlink.item.repository.ItemRepository;
import jakarta.validation.Validation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ItemServiceTest {
    private final ItemRepository items = mock(ItemRepository.class);
    private final MatchingClient matching = mock(MatchingClient.class);
    private final ItemService service = new ItemService(items, matching);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void preventsAnotherUserFromUpdatingAnItem() {
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        authenticate(other, "ROLE_USER");
        when(items.findById(itemId)).thenReturn(Optional.of(item(owner)));

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.update(itemId, validRequest()));

        assertEquals(403, error.getStatusCode().value());
        verify(items, never()).save(any());
    }

    @Test
    void reportsTheAuthenticatedUserAsTheItemOwner() {
        UUID owner = UUID.randomUUID();
        authenticate(owner, "ROLE_USER");
        when(items.save(any(ItemReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var created = service.create(validRequest());

        assertEquals(owner, created.reportedBy());
        verify(matching).publishItem(any());
    }

    @Test
    void rejectsInvalidItemRequestFields() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        ItemRequest invalid = new ItemRequest("", "", null, null, "", null, null);

        assertFalse(validator.validate(invalid).isEmpty());
    }

    private void authenticate(UUID id, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                id.toString(), null, List.of(new SimpleGrantedAuthority(role))));
    }

    private ItemReport item(UUID owner) {
        return new ItemReport("Headphones", "Black headphones", ItemCategory.ELECTRONICS,
                ItemType.LOST, "Office", LocalDate.now(), null, owner);
    }

    private ItemRequest validRequest() {
        return new ItemRequest("Headphones", "Black headphones", ItemCategory.ELECTRONICS,
                ItemType.LOST, "Office", LocalDate.now(), null);
    }
}
