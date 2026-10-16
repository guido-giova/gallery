package com.ggiova.gallery.data;

public record Item(
        long id,                     // non nullable
        String hash,
        String storagePath,
        String mime,
        long sizeBytes,              // non nullable
        int width,                   // non nullable
        int height,                  // non nullable
        Long durationMs,
        String title,
        String description,
        boolean isFavorite
) {}
