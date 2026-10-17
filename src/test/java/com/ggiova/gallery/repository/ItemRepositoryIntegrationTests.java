package com.ggiova.gallery.repository;

import com.ggiova.gallery.data.Item;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class ItemRepositoryIntegrationTests {
    @Container
    private static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private ItemRepository itemRepository;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Test
    void insertAndFindById_returnsSavedItemWithGeneratedId() {
        Item inserted = itemRepository.insert(item("image-one", null));

        assertTrue(inserted.id() > 0);
        assertEquals(Optional.of(inserted), itemRepository.findById(inserted.id()));
    }

    @Test
    void findById_returnsEmptyWhenItemDoesNotExist() {
        assertTrue(itemRepository.findById(Long.MAX_VALUE).isEmpty());
    }

    @Test
    void findAll_returnsItemsInIdOrder() {
        Item first = itemRepository.insert(item("first", null));
        Item second = itemRepository.insert(item("second", 12_500L));

        assertEquals(List.of(first, second), itemRepository.findAll());
    }

    @Test
    void update_changesAnExistingItem() {
        Item inserted = itemRepository.insert(item("before", null));
        Item updated = new Item(
                inserted.id(),
                "after",
                "media/after.jpg",
                "image/jpeg",
                2_048,
                800,
                600,
                null,
                "Updated title",
                "Updated description",
                true
        );

        assertTrue(itemRepository.update(updated));
        assertEquals(Optional.of(updated), itemRepository.findById(updated.id()));
    }

    @Test
    void update_returnsFalseWhenItemDoesNotExist() {
        assertFalse(itemRepository.update(item("missing", null)));
    }

    @Test
    void deleteById_removesExistingItemAndReturnsFalseForMissingItem() {
        Item inserted = itemRepository.insert(item("to-delete", null));

        assertTrue(itemRepository.deleteById(inserted.id()));
        assertTrue(itemRepository.findById(inserted.id()).isEmpty());
        assertFalse(itemRepository.deleteById(inserted.id()));
    }

    private static Item item(String name, Long durationMs) {
        boolean video = durationMs != null;
        String extension = video ? ".mp4" : ".jpg";
        String mime = video ? "video/mp4" : "image/jpeg";
        return new Item(
                0,
                name,
                "media/" + name + extension,
                mime,
                1_024,
                640,
                480,
                durationMs,
                name,
                "Description for " + name,
                false
        );
    }
}
