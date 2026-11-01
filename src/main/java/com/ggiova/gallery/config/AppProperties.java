package com.ggiova.gallery.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ConfigurationProperties(prefix = "app.storage")
public record AppProperties(Path path) {

    public AppProperties {
        if (path == null) {
            path = Path.of("uploads");
        }
        path = path.toAbsolutePath().normalize();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot create storage folder " + path, e);
        }
    }
}