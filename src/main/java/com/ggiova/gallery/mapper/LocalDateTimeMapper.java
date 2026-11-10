package com.ggiova.gallery.mapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;

public final class LocalDateTimeMapper {
    private LocalDateTimeMapper() {
        throw new UnsupportedOperationException("Don't instantiate!");
    }

    public static LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
