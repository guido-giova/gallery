package com.ggiova.gallery.mapper;

import com.ggiova.gallery.data.Item;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class ItemMapper {
    public static Item mapItem(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Item(
                resultSet.getLong("id"),
                resultSet.getString("hash"),
                resultSet.getString("storage_path"),
                resultSet.getString("mime"),
                resultSet.getLong("size_bytes"),
                resultSet.getInt("width"),
                resultSet.getInt("height"),
                resultSet.getObject("duration_ms", Long.class),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getBoolean("is_favorite"),
                resultSet.getBoolean("was_reviewed")
        );
    }

    public static Item copyWithId(Item item, long id) {
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
                item.isFavorite(),
                item.wasReviewed()
        );
    }
}
