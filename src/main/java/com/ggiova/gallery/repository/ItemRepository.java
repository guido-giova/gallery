package com.ggiova.gallery.repository;

import com.ggiova.gallery.data.Item;
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

    private static final RowMapper<Item> ITEM_ROW_MAPPER = ItemMapper::mapItem;

    private final JdbcClient jdbcClient;

    public ItemRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Item insert(Item item) {
        long id = jdbcClient.sql("""
                        INSERT INTO items (
                            hash, storage_path, mime, size_bytes, width, height,
                            duration_ms, title, description, is_favorite
                        ) VALUES (
                            :hash, :storagePath, :mime, :sizeBytes, :width, :height,
                            :durationMs, :title, :description, :isFavorite
                        )
                        RETURNING id
                        """)
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
                .query(Long.class)
                .single();

        return withId(item, id);
    }

    public Optional<Item> findById(long id) {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM items WHERE id = :id")
                .param("id", id)
                .query(ITEM_ROW_MAPPER)
                .optional();
    }

    public List<Item> findAll() {
        return jdbcClient.sql("SELECT " + COLUMNS + " FROM items ORDER BY id")
                .query(ITEM_ROW_MAPPER)
                .list();
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
        return jdbcClient.sql("DELETE FROM items WHERE id = :id")
                .param("id", id)
                .update() > 0;
    }

    private static Item withId(Item item, long id) {
        return new Item(
                id,
                item.hash(),
                item.storagePath(),
                item.mime(),
                item.sizeBytes(),
                item.width(),
                item.height(),
                item.durationMs(),
                item.title(),
                item.description(),
                item.isFavorite()
        );
    }
}
