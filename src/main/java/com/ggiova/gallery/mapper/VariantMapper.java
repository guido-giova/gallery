package com.ggiova.gallery.mapper;

import com.ggiova.gallery.data.Variant;
import com.ggiova.gallery.data.VariantKind;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Mapper class for Variant
 *
 * @see Variant
 */
public final class VariantMapper {
    /**
     * Maps a resultSet from a query into a Variant object.
     *
     * @param resultSet The ResultSet to map
     * @param rowNumber The number of the current row
     * @return A Variant object
     * @throws SQLException If any error is encountered while getting the column values.
     * @see org.springframework.jdbc.core.RowMapper
     */
    public static Variant mapVariant(ResultSet resultSet, int rowNumber) throws SQLException {
        return new Variant(
                resultSet.getLong("id"),
                resultSet.getLong("item_id"),
                VariantKind.valueOf(resultSet.getString("kind")),
                resultSet.getString("storage_path"),
                resultSet.getString("mime")
        );
    }
}
