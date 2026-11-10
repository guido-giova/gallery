package com.ggiova.gallery.mapper;

import java.sql.Timestamp;
import java.time.Instant;

public final class InstantMapper {
    private InstantMapper() {
        throw new UnsupportedOperationException("Don't instantiate!");
    }

    public static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toInstant();
    }
}
