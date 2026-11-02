package com.ggiova.gallery.mapper;

import com.ggiova.gallery.data.Variant;
import com.ggiova.gallery.data.VariantKind;

import java.sql.ResultSet;
import java.sql.SQLException;

public final class VariantMapper {
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
