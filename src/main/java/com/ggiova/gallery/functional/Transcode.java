package com.ggiova.gallery.functional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

@FunctionalInterface
public interface Transcode {
    Optional<String> apply(Path target, Path file) throws IOException;
}
