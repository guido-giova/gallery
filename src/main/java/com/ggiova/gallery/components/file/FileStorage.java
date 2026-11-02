package com.ggiova.gallery.components.file;

import com.ggiova.gallery.config.AppProperties;
import com.ggiova.gallery.data.VariantKind;
import com.ggiova.gallery.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

@Component
public class FileStorage {
    private static final String THUMB_DIR = ".variants";
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorage.class);

    private final Path directory;
    private final MediaInspector mediaInspector;
    private final MediaTranscoder transcoder;

    public FileStorage(AppProperties aps, MediaInspector mediaInspector, MediaTranscoder transcoder) throws IOException {
        this.directory = FileStorage.setDirectory(aps.path());
        this.mediaInspector = mediaInspector;
        this.transcoder = transcoder;
    }

    private static Path setDirectory(Path path) throws IOException {
        Files.createDirectories(path);
        return path;
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
        LOGGER.info("Trying to store file {}.", mf.getName());
        return this.storeChecked(mf);
    }

    private FileStorageResponse storeChecked(MultipartFile mf) {
        String storedName = Strings.randomUUID(mf);
        Path target = this.directory.resolve(storedName);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(mf.getInputStream(), md)) {
                Files.copy(in, target);
            }
            String hash = HexFormat.of().formatHex(md.digest());

            return switch (this.mediaInspector.inspectMedia(target)) {
                case MediaResponse.MediaInfo    i -> this.storeMedia(mf, i, storedName, target, hash);
                case MediaResponse.UnknownMedia u -> this.unknown(target);
                case MediaResponse.Failed       f -> this.failure(f, storedName, target);
            };
        } catch (IOException | NoSuchAlgorithmException | RuntimeException e) {
            LOGGER.error("Failed to stored uploaded file {}", storedName, e);
            this.deleteQuietly(target);
            return new FileStorageResponse.Error(e.getMessage());
        }
    }

    private FileStorageResponse.Error failure(MediaResponse.Failed f, String storedName, Path target) {
        LOGGER.error("Could not inspect {}: {}", storedName, f.message());
        this.deleteQuietly(target);
        return new FileStorageResponse.Error(f.message());
    }

    private FileStorageResponse.UnsupportedMedia unknown(Path target) {
        this.deleteQuietly(target);
        return new FileStorageResponse.UnsupportedMedia();
    }

    private FileStorageResponse.StoredFile storeMedia(MultipartFile mf, MediaResponse.MediaInfo info, String storedName, Path target, String hash) throws IOException {
        boolean isVideo = info.mime().startsWith("video/");
        String thumbRel = getThumbnailPath(isVideo);
        Path thumb = this.directory.resolve(thumbRel);
        Files.createDirectories(thumb.getParent());

        boolean thumbOk = isVideo
                        ? this.transcoder.createVideoThumbnail(target, thumb)
                        : this.transcoder.createImageThumbnail(target, thumb);

        List<StoredVariant> variants = new java.util.ArrayList<>();
        variants.add(new StoredVariant(VariantKind.ORIGINAL, storedName, info.mime()));
        if (thumbOk) {
            variants.add(new StoredVariant(VariantKind.THUMBNAIL, thumbRel, isVideo ? "video/mp4" : "image/jpeg"));
        } else {
            this.deleteQuietly(thumb);
        }
        return new FileStorageResponse.StoredFile(hash, mf.getSize(), info.width(), info.height(), info.durationMs(), variants);
    }

    private static String getThumbnailPath(boolean video) {
        return THUMB_DIR + "/" + Strings.randomUUID() + (video ? ".mp4" : ".jpg");
    }
}
