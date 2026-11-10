package com.ggiova.gallery.mapper;

import com.ggiova.gallery.data.Item;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Mapper class for Items
 */
public final class ItemMapper {
    /**
     * Maps a resultSet from a query into an Item object.
     *
     * @param resultSet The ResultSet to map
     * @param rowNumber The number of the current row
     * @return An Item object
     * @throws SQLException If any error is encountered while getting the column values.
     * @see org.springframework.jdbc.core.RowMapper
     */
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
                ItemMapper.getTakenAt(resultSet)
        );
    }

    private static LocalDateTime getTakenAt(ResultSet resultSet) throws SQLException {
        java.sql.Timestamp takenAt = resultSet.getTimestamp("taken_at");
        return (takenAt == null) ? null : takenAt.toLocalDateTime();
    }
}
