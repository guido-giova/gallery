package com.ggiova.gallery.components.file;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class Matcher<R> {
    private final FileStorageResponse result;
    private Supplier<? extends R> onEmpty;
    private Function<? super FileStorageResponse.StoredFile, ? extends R> onStored;
    private Supplier<? extends R> onNotSupported;
    private Supplier<? extends R> onError;

    Matcher(FileStorageResponse result) {
        this.result = result;
    }

    public Matcher<R> onEmpty(Supplier<? extends R> f) {
        this.onEmpty = f;
        return this;
    }

    public Matcher<R> onStored(Function<? super FileStorageResponse.StoredFile, ? extends R> f) {
        this.onStored = f;
        return this;
    }

    public Matcher<R> onNotSupported(Supplier<? extends R> f) {
        this.onNotSupported = f;
        return this;
    }

    public Matcher<R> onError(Supplier<? extends R> f) {
        this.onError = f;
        return this;
    }

    public R resolve() {
        Objects.requireNonNull(this.onEmpty, "onEmpty handler not set");
        Objects.requireNonNull(this.onStored, "onStored handler not set");
        Objects.requireNonNull(this.onNotSupported, "onNotSupported handler not set");
        Objects.requireNonNull(this.onError, "onError handler not set");

        return switch (this.result) {
            case FileStorageResponse.EmptyFile        f -> this.onEmpty.get();
            case FileStorageResponse.StoredFile       f -> this.onStored.apply(f);
            case FileStorageResponse.UnsupportedMedia f -> this.onNotSupported.get();
            case FileStorageResponse.Error            f -> this.onError.get();
        };
    }
}
