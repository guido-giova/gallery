package com.ggiova.gallery.components.file;

public sealed interface MediaResponse {
    record MediaInfo(String mime, int width, int height, Long durationMs) implements MediaResponse {}
    record UnknownMedia() implements MediaResponse {}
    record Failed(String message) implements MediaResponse {}
}
