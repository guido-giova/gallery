package com.ggiova.gallery.components.file;

import com.ggiova.gallery.service.ItemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Component
public class FileStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorage.class);

    private final Path directory;
    private final MediaInspector mediaInspector;

    public FileStorage(
            @Value("${app.upload-dir:uploads}") String directory,
            MediaInspector mediaInspector
    ) throws IOException {
        this.directory = FileStorage.setDirectory(directory);
        this.mediaInspector = mediaInspector;
    }

    private static Path setDirectory(String directory) throws IOException {
        Path pathDirectory = Paths.get(directory).toAbsolutePath().normalize();
        Files.createDirectories(pathDirectory);
        return pathDirectory;
    }

    public Optional<Resource> load(String storagePath) {
        try {
            Path path = this.directory.resolve(storagePath).normalize();

            if (!path.startsWith(this.directory) || !Files.isRegularFile(path)) {
                return Optional.empty();
            }
            return Optional.of(new FileSystemResource(path));
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    public void delete(String storagePath) throws IOException {
        Path path = this.directory.resolve(storagePath).normalize();
        if (path.startsWith(this.directory)) {
            Files.deleteIfExists(path);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    public FileStorageResponse store(MultipartFile mf) {
        if (mf == null || mf.isEmpty()) {
            return new FileStorageResponse.EmptyFile();
        }
        return this.storeChecked(mf);
    }

    private FileStorageResponse storeChecked(MultipartFile mf) {
        String storedName = UUID.randomUUID().toString();
        Path target = this.directory.resolve(storedName);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(mf.getInputStream(), md)) {
                Files.copy(in, target);
            }
            String hash = HexFormat.of().formatHex(md.digest());

            return switch (this.mediaInspector.inspectMedia(target)) {
                case MediaResponse.MediaInfo info -> new FileStorageResponse.StoredFile(
                        hash, storedName, info.mime(), mf.getSize(),
                        info.width(), info.height(), info.durationMs());
                case MediaResponse.UnknownMedia u -> {
                    this.deleteQuietly(target);
                    yield new FileStorageResponse.UnsupportedMedia();
                }
                case MediaResponse.Failed f -> {
                    LOGGER.error("Could not inspect {}: {}", storedName, f.message());
                    this.deleteQuietly(target);
                    yield new FileStorageResponse.Error(f.message());
                }
            };
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            LOGGER.error("Failed to stored uploaded file {}", storedName, e);
            this.deleteQuietly(target);
            return new FileStorageResponse.Error(e.getMessage());
        }
    }
}
