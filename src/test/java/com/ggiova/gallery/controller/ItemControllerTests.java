package com.ggiova.gallery.controller;

import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.service.ItemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @Test
    void findAll_returnsItems() throws Exception {
        when(itemService.findAll()).thenReturn(List.of(item(1, "one")));

        mockMvc.perform(get("/api/items").with(user("gallery-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].hash").value("one"));
    }

    @Test
    void findById_returnsItemWhenItExists() throws Exception {
        when(itemService.findById(1)).thenReturn(Optional.of(item(1, "one")));

        mockMvc.perform(get("/api/items/1").with(user("gallery-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.hash").value("one"));
    }

    @Test
    void findById_returnsNotFoundWhenItemDoesNotExist() throws Exception {
        when(itemService.findById(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/99").with(user("gallery-user")))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsCreatedItemAndLocation() throws Exception {
        when(itemService.create(any(Item.class))).thenReturn(item(4, "new-item"));

        mockMvc.perform(post("/api/items")
                        .with(user("gallery-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson(0, "new-item")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/items/4"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.hash").value("new-item"));
    }

    @Test
    void update_usesPathIdAndReturnsUpdatedItem() throws Exception {
        when(itemService.update(eq(5L), any(Item.class)))
                .thenReturn(Optional.of(item(5, "updated")));

        mockMvc.perform(put("/api/items/5")
                        .with(user("gallery-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson(999, "updated")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.hash").value("updated"));

        verify(itemService).update(eq(5L), any(Item.class));
    }

    @Test
    void update_returnsNotFoundWhenItemDoesNotExist() throws Exception {
        when(itemService.update(eq(99L), any(Item.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/api/items/99")
                        .with(user("gallery-user"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson(99, "missing")))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_returnsNoContentWhenItemIsDeleted() throws Exception {
        when(itemService.deleteById(1)).thenReturn(true);

        mockMvc.perform(delete("/api/items/1")
                        .with(user("gallery-user"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(itemService).deleteById(1);
    }

    @Test
    void delete_returnsNotFoundWhenItemDoesNotExist() throws Exception {
        when(itemService.deleteById(99)).thenReturn(false);

        mockMvc.perform(delete("/api/items/99")
                        .with(user("gallery-user"))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    private static Item item(long id, String hash) {
        return new Item(
                id,
                hash,
                "media/" + hash + ".jpg",
                "image/jpeg",
                1_024,
                640,
                480,
                null,
                hash,
                "Description for " + hash,
                false
        );
    }

    private static String itemJson(long id, String hash) {
        return """
                {
                  "id": %d,
                  "hash": "%s",
                  "storagePath": "media/%s.jpg",
                  "mime": "image/jpeg",
                  "sizeBytes": 1024,
                  "width": 640,
                  "height": 480,
                  "durationMs": null,
                  "title": "%s",
                  "description": "Description for %s",
                  "isFavorite": false
                }
                """.formatted(id, hash, hash, hash, hash);
    }
}
