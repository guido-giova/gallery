package com.ggiova.gallery.service;

import com.ggiova.gallery.components.file.FileStorage;
import com.ggiova.gallery.components.file.FileStorageResponse;
import com.ggiova.gallery.dto.ItemUpdate;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.dto.ReviewResult;
import com.ggiova.gallery.dto.UploadResult;
import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.dto.internal.Content;
import com.ggiova.gallery.repository.ItemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final FileStorage storage;

    public ItemService(
            ItemRepository itemRepository,
            FileStorage storage
    ) {
        this.itemRepository = itemRepository;
        this.storage        = storage;
    }

    public List<ItemListing> listAll() {
        return this.itemRepository.listAll();
    }

    public Optional<Content> getContent(long id) {
        return itemRepository.findById(id)
                             .flatMap(item -> storage.load(item.storagePath())
                             .map(res -> new Content(res, item.mime())));
    }

    public UploadResult create(MultipartFile mf) {
        return this.storage.store(mf)
                           .mapTo(UploadResult.class)
                           .onEmpty(UploadResult.EmptyFile::new)
                           .onStored(this::register)
                           .onError(ItemService::createCouldNotStoreTheFile)
                           .resolve();
    }

    private static UploadResult.Failure createCouldNotStoreTheFile() {
        return new UploadResult.Failure("Could not store the file");
    }

    private UploadResult register(FileStorageResponse.StoredFile stored) {
        try {
            long id = this.itemRepository.insertNewFile(
                    stored.hash(),
                    stored.storagePath(),
                    stored.mime(),
                    stored.sizeBytes(),
                    stored.width(),
                    stored.height(),
                    stored.durationMs()
            );
            return new UploadResult.Success(id);
        } catch (RuntimeException e) {
            System.err.printf("Failed to register file %s.%n", stored.storagePath());
            try {
                this.storage.delete(stored.storagePath());
            } catch (IOException ignored) {
            }
            return new UploadResult.Failure("Could not store the file");
        }
    }

    public Optional<Item> findById(long id) {
        return itemRepository.findById(id);
    }

    public ReviewResult review(long id) {
        Optional<Item> item = this.findById(id);
        if (item.isEmpty()) {
            return new ReviewResult.NotFound();
        }
        return this.itemRepository.review(id) ?
                  new ReviewResult.Reviewed() :
                  new ReviewResult.AlreadyReviewed();
    }

    public Optional<Map<String, String>> update(long id, ItemUpdate itemUpdate) {
        Map<String, String> response = new java.util.HashMap<>();

        ReviewResult view = this.review(id);
        if (view instanceof ReviewResult.NotFound()) {
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
        if (found.isEmpty()) { return false; }
        Item item = found.get();

        this.itemRepository.deleteById(id);

        try {
            storage.delete(item.storagePath());
        } catch (IOException | InvalidPathException e) {
            LOGGER.error("Item {} deleted but file {} could not be removed. Reason {}.", id, item.storagePath(), e.getLocalizedMessage(), e);
        }
        return true;
    }
}
