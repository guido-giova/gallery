package com.ggiova.gallery.service;

import com.ggiova.gallery.dto.ItemUpdate;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.dto.UploadResult;
import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.dto.internal.Content;
import com.ggiova.gallery.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;

@Service
public class ItemService {
    private final Path uploadDir;
    private final ItemRepository itemRepository;

    public ItemService(
            ItemRepository itemRepository,
            @Value("${app.upload-dir:uploads}") String dir
    ) throws IOException {
        this.itemRepository = itemRepository;

        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadDir);
    }

    public List<ItemListing> listAll() {
        return this.itemRepository.listAll();
    }

    public Optional<Content> getContent(long id) {
        return this.itemRepository.findById(id)
                                  .flatMap(this::getContent);
    }

    private Optional<Content> getContent(Item item) {
        try {
            Path path = this.uploadDir.resolve(item.storagePath()).normalize();

            if (!path.startsWith(this.uploadDir) || !Files.isRegularFile(path)) {
                return Optional.empty();
            }
            return Optional.of(new Content(new FileSystemResource(path), item.mime()));
        } catch (InvalidPathException e) {
            return Optional.empty();
        }
    }

    public UploadResult create(MultipartFile mf) {
        String original = StringUtils.cleanPath(Objects.requireNonNull(mf.getOriginalFilename(), "Null file name"));
        String baseName = Paths.get(original).getFileName().toString();

        if (mf.isEmpty()) {
            return new UploadResult.EmptyFile();
        }

        String storedName = UUID.randomUUID() + "_" + baseName;
        Path target = uploadDir.resolve(storedName).normalize();
        if(! target.startsWith(uploadDir)) {
            return new UploadResult.InvalidFilename();
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream in = new DigestInputStream(mf.getInputStream(), md)) {
                Files.copy(in, target);
            }
            String hash = HexFormat.of().formatHex(md.digest());

            String mime = Optional.ofNullable(Files.probeContentType(target))
                                  .orElse(mf.getContentType());

            int width, height;
            Long durationMs = null;

            if (mime != null && mime.startsWith("image/")) {
                BufferedImage img = ImageIO.read(target.toFile());
                if (img == null) {
                    throw new IllegalArgumentException("Unreadable or unsupported image format");
                }
                width = img.getWidth();
                height = img.getHeight();
            } else if (mime != null && mime.startsWith("video/")) {
                throw new UnsupportedOperationException("Video not supported yet");
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + mime);
            }

            long id = this.itemRepository.insertNewFile(hash, storedName, mime, mf.getSize(), width, height, durationMs);
            return new UploadResult.Success(id);
        } catch (Exception e) {
            try {
                Files.deleteIfExists(target);
            } catch (IOException ignored) {}
            return new UploadResult.Failure(e.getLocalizedMessage());
        }
    }

    public Optional<Item> findById(long id) {
        return itemRepository.findById(id);
    }

    public Optional<Map<String, String>> update(long id, ItemUpdate itemUpdate) {
        Map<String, String> response = new HashMap<>();

        Optional<Item> found = this.itemRepository.findById(id);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        if (itemUpdate.isFavorite() != null) {
            boolean isSuccess = this.itemRepository.updateFavorite(id, itemUpdate.isFavorite());
            if (isSuccess) {
                response.put("favorite", "success");
            } else {
                response.put("favorite", "failure");
            }
        }

        if (itemUpdate.title() != null) {
            boolean isSuccess = this.itemRepository.updateTitle(id, itemUpdate.title());
            if (isSuccess) {
                response.put("title", "success");
            } else {
                response.put("title", "failure");
            }
        }

        if (itemUpdate.description() != null) {
            boolean isSuccess = this.itemRepository.updateDescription(id, itemUpdate.description());
            if (isSuccess) {
                response.put("description", "success");
            } else {
                response.put("description", "failure");
            }
        }

        return Optional.of(response);
    }

    public boolean deleteById(long id) {
        Optional<Item> found = this.itemRepository.findById(id);
        if (found.isEmpty()) {return false;}
        Item item = found.get();

        boolean hasDeletedInDB = this.itemRepository.deleteById(id);

        try {
            Path path = uploadDir.resolve(item.storagePath()).normalize();
            if (path.startsWith(uploadDir)) {
                Files.deleteIfExists(path);
            }
        } catch (IOException | InvalidPathException e) {
            System.err.printf("Item %d deleted but file %s could not be removed. Reason %s%n", id, item.storagePath(), e.getLocalizedMessage());
        }
        return true;
    }
}
