package com.ggiova.gallery.repository;

import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.mapper.ItemMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository layer. Handles searching for items, deleting, creating, updating, etc.
 */
@Repository
public class ItemRepository {
    private static final String QUERY_INSERT = """
        INSERT INTO items (
            hash, size_bytes, width, height, duration_ms
        ) VALUES (
            :hash, :sizeBytes, :width, :height, :durationMs
        )
        RETURNING id;
    """;
    private static final String QUERY_LIST_ALL = "SELECT id, favorite, reviewed, created_at, taken_at FROM items ORDER BY COALESCE(taken_at, created_at), created_at;";
    private static final String QUERY_FIND_BY_ID = "SELECT * FROM items WHERE id = :id";
    private static final String QUERY_UPDATE_IS_FAVORITE = "UPDATE items SET favorite = :favorite WHERE id = :id;";
    private static final String QUERY_UPDATE_TITLE = "UPDATE items SET title = :title WHERE id = :id;";
    private static final String QUERY_UPDATE_DESCRIPTION = "UPDATE items SET description = :description WHERE id = :id;";
    private static final String QUERY_UPDATE_REVIEWED = "UPDATE items SET reviewed = TRUE WHERE id = :id;";
    private static final String QUERY_DELETE_BY_ID = "DELETE FROM items WHERE id = :id";

    private final JdbcClient jdbcClient;

    /**
     * ItemRepository Constructor.
     *
     * @param jdbcClient Database client that will be used to make the queries.
     */
    public ItemRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Lists all items.
     *
     * @return List of all items.
     * @see ItemListing
     */
    public List<ItemListing> listAll() {
        return this.jdbcClient.sql(QUERY_LIST_ALL)
                              .query(ItemListing.class)
                              .list();
    }

    /**
     * Inserts the data of an item into the database.
     *
     * @param hash       of the file.
     * @param size       in bytes of the item.
     * @param width      of the item.
     * @param height     of the item.
     * @param durationMs Length of the video. Can be null if not a video.
     * @return The id of the new item.
     */
    public long insertNewFile(String hash, long size, int width, int height, Long durationMs) {
        return this.jdbcClient.sql(QUERY_INSERT)
                              .param("hash", hash)
                              .param("sizeBytes", size)
                              .param("width", width)
                              .param("height", height)
                              .param("durationMs", durationMs)
                              .query(Long.class)
                              .single();
    }

    /**
     * Finds an item by the given id.
     *
     * @param id of the item.
     * @return An {@link Optional} with the item. Returns an empty optional otherwise.
     */
    public Optional<Item> findById(long id) {
        return this.jdbcClient.sql(QUERY_FIND_BY_ID)
                              .param("id", id)
                              .query(ItemMapper::mapItem)
                              .optional();
    }

    /**
     * Updates an item to make it favorite or remove the favorite.
     *
     * @param id         of the item.
     * @param isFavorite if it's favorite or not.
     * @return {@code true} if it was updated successfully, {@code false} otherwise.
     */
    public boolean updateFavorite(long id, boolean isFavorite) {
        return this.jdbcClient.sql(QUERY_UPDATE_IS_FAVORITE)
                              .param("favorite", isFavorite)
                              .param("id", id)
                              .update() > 0;
    }

    /**
     * Updates the title of an item.
     *
     * @param id    of the item.
     * @param title New title.
     * @return {@code true} if it was updated successfully, {@code false} otherwise.
     */
    public boolean updateTitle(long id, String title) {
        return this.jdbcClient.sql(QUERY_UPDATE_TITLE)
                              .param("title", title)
                              .param("id", id)
                              .update() > 0;
    }

    /**
     * Updates the description of an item.
     *
     * @param id          of the item.
     * @param description New description.
     * @return {@code true} if it was updated successfully, {@code false} otherwise.
     */
    public boolean updateDescription(long id, String description) {
        return this.jdbcClient.sql(QUERY_UPDATE_DESCRIPTION)
                              .param("description", description)
                              .param("id", id)
                              .update() > 0;
    }

    /**
     * Updates an item to be reviewed.
     *
     * @param id of the item.
     * @return {@code true} if it was updated successfully, {@code false} otherwise.
     */
    public boolean review(long id) {
        return this.jdbcClient.sql(QUERY_UPDATE_REVIEWED)
                              .param("id", id)
                              .update() > 0;
    }

    /**
     * Deletes an item by a given id.
     *
     * @param id of the item.
     * @return {@code true} if it was deleted successfully, {@code false} otherwise.
     */
    public boolean deleteById(long id) {
        return this.jdbcClient.sql(QUERY_DELETE_BY_ID)
                              .param("id", id)
                              .update() > 0;
    }
}
