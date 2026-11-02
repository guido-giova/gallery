package com.ggiova.gallery.components.file;

import java.util.List;

public sealed interface FileStorageResponse {
    record EmptyFile() implements FileStorageResponse {}
    record StoredFile(
            String hash,
            long sizeBytes,
            int width,
            int height,
            Long durationMs,
            List<StoredVariant> variants
    ) implements FileStorageResponse {}
    record UnsupportedMedia() implements FileStorageResponse {}
    record Error(String message) implements FileStorageResponse {}

    default <R> Matcher<R> mapTo(Class<R> target) {
        return new Matcher<>(this);
    }
}
