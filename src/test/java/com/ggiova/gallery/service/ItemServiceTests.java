package com.ggiova.gallery.service;

import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.repository.ItemRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ItemServiceTests {
    @Test
    void update_usesTheIdFromTheRequestPath() {
        ItemRepository repository = mock(ItemRepository.class);
        ItemService service = new ItemService(repository);
        Item request = new Item(
                999,
                "updated",
                "media/updated.jpg",
                "image/jpeg",
                2_048,
                800,
                600,
                null,
                "Updated title",
                "Updated description",
                true
        );
        Item expected = new Item(
                5,
                request.hash(),
                request.storagePath(),
                request.mime(),
                request.sizeBytes(),
                request.width(),
                request.height(),
                request.durationMs(),
                request.title(),
                request.description(),
                request.isFavorite()
        );
        when(repository.update(expected)).thenReturn(true);

        Optional<Item> result = service.update(5, request);
        assertEquals(Optional.of(expected), result);
        verify(repository).update(argThat(item -> item.id() == 5));
        verify(repository).update(argThat(item -> item.id() == 5));
    }
}
