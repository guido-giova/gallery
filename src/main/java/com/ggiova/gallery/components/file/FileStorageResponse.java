package com.ggiova.gallery.components.file;

public sealed interface FileStorageResponse {
    record EmptyFile() implements FileStorageResponse {}
    record StoredFile(
            String hash,
            String storagePath,
            String mime,
            long sizeBytes,
            int width,
            int height,
            Long durationMs
    ) implements FileStorageResponse {}
    record UnsupportedMedia() implements FileStorageResponse {}
    record Error(String message) implements FileStorageResponse {}

    default <R> Matcher<R> mapTo(Class<R> target) {
        return new Matcher<>(this);
    }
}
