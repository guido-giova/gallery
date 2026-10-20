package com.ggiova.gallery.repository;

import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.mapper.ItemMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository {
    private static final String QUERY_INSERT = """
        INSERT INTO items (
            hash, storage_path, mime, size_bytes, width, height, duration_ms
        ) VALUES (
            :hash, :storagePath, :mime, :sizeBytes, :width, :height, :durationMs
        )
        RETURNING id;
    """;
    private static final String QUERY_LIST_ALL = "SELECT id, mime, is_favorite FROM items ORDER BY id";
    private static final String QUERY_FIND_BY_ID = "SELECT id, hash, storage_path, mime, size_bytes, width, height, duration_ms, title, description, is_favorite FROM items WHERE id = :id";
    private static final String QUERY_UPDATE_IS_FAVORITE = "UPDATE items SET is_favorite = :isFavorite WHERE id = :id;";
    private static final String QUERY_UPDATE_TITLE = "UPDATE items SET title = :title WHERE id = :id;";
    private static final String QUERY_UPDATE_DESCRIPTION = "UPDATE items SET description = :description WHERE id = :id;";
    private static final String QUERY_DELETE_BY_ID = "DELETE FROM items WHERE id = :id";

    private final JdbcClient jdbcClient;

    public ItemRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<ItemListing> listAll() {
        return this.jdbcClient.sql(QUERY_LIST_ALL)
                              .query(ItemListing.class)
                              .list();
    }

    public long insertNewFile(String hash, String storedName, String mime, long size, int width, int height, Long durationMs) {
        return this.jdbcClient.sql(QUERY_INSERT)
                              .param("hash", hash)
                              .param("storagePath", storedName)
                              .param("mime", mime)
                              .param("sizeBytes", size)
                              .param("width", width)
                              .param("height", height)
                              .param("durationMs", durationMs)
                              .query(Long.class)
                              .single();
    }

    public Optional<Item> findById(long id) {
        return this.jdbcClient.sql(QUERY_FIND_BY_ID)
                              .param("id", id)
                              .query(ItemMapper::mapItem)
                              .optional();
    }

    public boolean updateFavorite(long id, boolean isFavorite) {
        return this.jdbcClient.sql(QUERY_UPDATE_IS_FAVORITE)
                              .param("isFavorite", isFavorite)
                              .param("id", id)
                              .update() > 0;
    }

    public boolean updateTitle(long id, String title) {
        return this.jdbcClient.sql(QUERY_UPDATE_TITLE)
                              .param("title", title)
                              .param("id", id)
                              .update() > 0;
    }

    public boolean updateDescription(long id, String description) {
        return this.jdbcClient.sql(QUERY_UPDATE_DESCRIPTION)
                              .param("description", description)
                              .param("id", id)
                              .update() > 0;
    }

    public boolean deleteById(long id) {
        return this.jdbcClient.sql(QUERY_DELETE_BY_ID)
                              .param("id", id)
                              .update() > 0;
    }
}
