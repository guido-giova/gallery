package com.ggiova.gallery.repository;

import com.ggiova.gallery.components.file.StoredVariant;
import com.ggiova.gallery.data.Variant;
import com.ggiova.gallery.data.VariantKind;
import com.ggiova.gallery.mapper.VariantMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class VariantRepository {
    private static final String QUERY_INSERT = """
        INSERT INTO variants (
            item_id, kind, storage_path, mime
        ) VALUES (
            :itemId, :kind, :storagePath, :mime
        )
        RETURNING id;
    """;

    private static final String QUERY_FIND_BY_ID_AND_VARIANT = "SELECT * FROM variants WHERE item_id = :itemId AND kind = :kind";
    private static final String QUERY_FIND_BY_ITEM_ID = "SELECT * FROM variants WHERE item_id = :itemId";
    private static final String QUERY_FIND_BY_ID = "SELECT * FROM variants WHERE id = :id";
    private static final String QUERY_DELETE_BY_ID = "DELETE FROM variants WHERE id = :id";

    private final JdbcClient jdbcClient;

    public VariantRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Variant> findById(long id) {
        return this.jdbcClient.sql(QUERY_FIND_BY_ID)
                              .param("id", id)
                              .query(VariantMapper::mapVariant)
                              .optional();
    }

    public List<Variant> findByParentId(long itemId) {
        return this.jdbcClient.sql(QUERY_FIND_BY_ITEM_ID)
                              .param("itemId", itemId)
                              .query(VariantMapper::mapVariant)
                              .list();
    }

    public Optional<Variant> findByParentIdAndVariant(long itemId, String variant) {
        if (variant == null) { return Optional.empty(); }
        try {
            return this.findByParentIdAndVariant(itemId, VariantKind.valueOf(variant.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Optional<Variant> findByParentIdAndVariant(long itemId, VariantKind variant) {
        return this.jdbcClient.sql(QUERY_FIND_BY_ID_AND_VARIANT)
                              .param("itemId", itemId)
                              .param("kind", variant)
                              .query(VariantMapper::mapVariant)
                              .optional();
    }

    public long insertNewVariant(long itemId, VariantKind kind, String storagePath, String mime) {
        return this.jdbcClient.sql(QUERY_INSERT)
                              .param("itemId", itemId)
                              .param("kind", kind.toString())
                              .param("storagePath", storagePath)
                              .param("mime", mime)
                              .query(Long.class)
                              .single();
    }

    public boolean deleteById(long id) {
        return this.jdbcClient.sql(QUERY_DELETE_BY_ID)
                              .param("id", id)
                              .update() > 0;
    }
}
