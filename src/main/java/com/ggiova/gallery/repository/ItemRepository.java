package com.ggiova.gallery.repository;

import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.mapper.ItemMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ItemRepository {
    private static final String COLUMNS = """
            id, hash, storage_path, mime, size_bytes, width, height,
            duration_ms, title, description, is_favorite
            """;
    private static final String QUERY_INSERT = """
        INSERT INTO items (
            hash, storage_path, mime, size_bytes, width, height, duration_ms
        ) VALUES (
            :hash, :storagePath, :mime, :sizeBytes, :width, :height, :durationMs
        )
        RETURNING id;
    """;
    private static final String QUERY_LIST_ALL = "SELECT id, mime, is_favorite FROM items ORDER BY id";
    private static final String QUERY_DELETE_BY_ID = "DELETE FROM items WHERE id = :id";
    private static final RowMapper<Item> ITEM_ROW_MAPPER = ItemMapper::mapItem;

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
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM items WHERE id = :id")
                .param("id", id)
                .query(ITEM_ROW_MAPPER)
                .optional();
    }

    public boolean update(Item item) {
        return jdbcClient.sql("""
                        UPDATE items
                        SET hash = :hash,
                            storage_path = :storagePath,
                            mime = :mime,
                            size_bytes = :sizeBytes,
                            width = :width,
                            height = :height,
                            duration_ms = :durationMs,
                            title = :title,
                            description = :description,
                            is_favorite = :isFavorite
                        WHERE id = :id
                        """)
                .param("id", item.id())
                .param("hash", item.hash())
                .param("storagePath", item.storagePath())
                .param("mime", item.mime())
                .param("sizeBytes", item.sizeBytes())
                .param("width", item.width())
                .param("height", item.height())
                .param("durationMs", item.durationMs())
                .param("title", item.title())
                .param("description", item.description())
                .param("isFavorite", item.isFavorite())
                .update() > 0;
    }

    public boolean deleteById(long id) {
        return this.jdbcClient.sql(QUERY_DELETE_BY_ID)
                              .param("id", id)
                              .update() > 0;
    }
}
