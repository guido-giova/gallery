package com.ggiova.gallery.components.file;

import com.ggiova.gallery.config.AppProperties;
import com.ggiova.gallery.data.VariantKind;
import com.ggiova.gallery.functional.Transcode;
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
    private static final int MAX_TRIES = 3;
    private static final String VARIANTS_DIRECTORY = ".variants";
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

        List<StoredVariant> variants = new java.util.ArrayList<>();
        variants.add(new StoredVariant(VariantKind.ORIGINAL, storedName, info.mime()));

        Optional<StoredVariant> storedThumbnail = this.storeThumbnail(info, target, isVideo);
        Optional<StoredVariant> storedLowRes = this.storeLowRes(info, target, isVideo);
        if (storedThumbnail.isEmpty() || storedLowRes.isEmpty()) {
            throw new RuntimeException("Could not create variants");
        }
        variants.add(storedThumbnail.get());
        variants.add(storedLowRes.get());

        return new FileStorageResponse.StoredFile(hash, mf.getSize(), info.width(), info.height(), info.durationMs(), variants);
    }

    private Optional<StoredVariant> storeThumbnail(MediaResponse.MediaInfo info, Path target, boolean isVideo) throws IOException {
        return this.storeVariant(
                target,
                VariantKind.THUMBNAIL,
                isVideo ? ".mp4" : ".jpeg",
                isVideo ? this.transcoder::createVideoThumbnail : this.transcoder::createImageThumbnail
        );
    }

    private Optional<StoredVariant> storeLowRes(MediaResponse.MediaInfo info, Path target, boolean isVideo) throws IOException {
        return this.storeVariant(
                target,
                VariantKind.LOW_RES,
                ".jpeg",
                isVideo ? this.transcoder::createVideoLowRes : this.transcoder::createImageLowRes
        );
    }

    private Optional<StoredVariant> storeVariant(
            Path target,
            VariantKind kind,
            String extension,
            Transcode transcode
    ) throws IOException {
        for (int tries = 0; tries < MAX_TRIES; tries++) {
            String fileRel = Strings.randomUUIDWithTerminationAndBeginning(VARIANTS_DIRECTORY + "/", extension);
            Path file = this.directory.resolve(fileRel);
            Files.createDirectories(file.getParent());

            Optional<String> fileType = transcode.apply(target, file);
            if (fileType.isPresent()) {
                return Optional.of(new StoredVariant(kind, fileRel, fileType.get()));
            }
            this.deleteQuietly(file);
        }
        return Optional.empty();
    }
}
