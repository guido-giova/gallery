package com.ggiova.gallery.mapper;

import com.ggiova.gallery.data.Item;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class ItemMapper {
    public static Item mapItem(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Item(
                resultSet.getLong("id"),
                resultSet.getString("hash"),
                resultSet.getLong("size_bytes"),
                resultSet.getInt("width"),
                resultSet.getInt("height"),
                resultSet.getObject("duration_ms", Long.class),
                resultSet.getString("title"),
                resultSet.getString("description"),
                resultSet.getBoolean("favorite"),
                resultSet.getBoolean("reviewed"),
                resultSet.getTimestamp("created_at").toInstant(),
                resultSet.getTimestamp("taken_at").toLocalDateTime()
        );
    }
}
