package com.ggiova.gallery.service;

import com.ggiova.gallery.components.file.FileStorage;
import com.ggiova.gallery.components.file.FileStorageResponse;
import com.ggiova.gallery.components.file.StoredVariant;
import com.ggiova.gallery.data.Variant;
import com.ggiova.gallery.dto.ItemUpdate;
import com.ggiova.gallery.dto.ItemListing;
import com.ggiova.gallery.dto.ReviewResult;
import com.ggiova.gallery.dto.UploadResult;
import com.ggiova.gallery.data.Item;
import com.ggiova.gallery.dto.internal.Content;
import com.ggiova.gallery.repository.ItemRepository;
import com.ggiova.gallery.repository.VariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer of the system. Manages the items' listing ({@link ItemService#listAll()}), content retrieval
 * ({@link ItemService#getContent(long)}), creation ({@link ItemService#create(MultipartFile)}), finding
 * ({@link ItemService#findById(long)}), review ({@link ItemService#review(long)}), update
 * ({@link ItemService#update(long, ItemUpdate)}), and delete ({@link ItemService#deleteById(long)}).
 */
@Service
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;
    private final VariantRepository variantRepository;
    private final FileStorage storage;

    /**
     * ItemService's constructor.
     *
     * @param itemRepository    Repository that stores the items.
     * @param variantRepository Repository for the variants of the items.
     * @param storage           Component that handles file storage.
     */
    public ItemService(
            ItemRepository itemRepository,
            VariantRepository variantRepository,
            FileStorage storage
    ) {
        this.itemRepository    = itemRepository;
        this.variantRepository = variantRepository;
        this.storage           = storage;
    }

    /**
     * Lists all items.
     *
     * @return all items.
     */
    public List<ItemListing> listAll() {
        return this.itemRepository.listAll();
    }

    public Optional<Content> getContent(long itemId, String variantKind) {
        return this.variantRepository.findByParentIdAndVariant(itemId, variantKind)
                                     .flatMap(variant -> storage.load(variant.storagePath())
                                     .map(res -> new Content(res, variant.mime())));
    }

    /**
     * Creates a new item based on the given content.
     *
     * @param mf content for which a file will be created.
     * @return An {@link UploadResult} that represents what happened.
     */
    public UploadResult create(MultipartFile mf) {
        return this.storage.store(mf)
                           .mapTo(UploadResult.class)
                           .onEmpty(UploadResult.EmptyFile::new)
                           .onStored(this::register)
                           .onNotSupported(ItemService::createUnsupportedMedia)
                           .onError(ItemService::createCouldNotStoreTheFile)
                           .resolve();
    }

    private static UploadResult.Failure createCouldNotStoreTheFile() {
        return new UploadResult.Failure("Could not store the file");
    }

    private static UploadResult.Failure createUnsupportedMedia() {
        return new UploadResult.Failure("Unsupported or unreadable file type");
    }

    private UploadResult register(FileStorageResponse.StoredFile stored) {
        Long id = null;
        try {
            id = this.itemRepository.insertNewFile(stored.hash(), stored.sizeBytes(), stored.width(), stored.height(), stored.durationMs());

            LOGGER.info("Registering file {} with {} variants", id, stored.variants().size());
            for (StoredVariant sv : stored.variants()) {
                long variantId = this.variantRepository.insertNewVariant(id, sv.kind(), sv.storagePath(), sv.mime());
                LOGGER.debug("Inserted variant {} ({}) for file {}", variantId, sv.kind(), id);
            }
            return new UploadResult.Success(id);
        } catch (RuntimeException e) {
            LOGGER.error("Failed to register file {}.", stored.hash(), e);
            if (id != null) {
                try { this.itemRepository.deleteById(id); } catch (RuntimeException ignored) {}
            }
            for (StoredVariant sv : stored.variants()) {
                try { this.storage.delete(sv.storagePath()); } catch (IOException ignored) {}
            }
            return new UploadResult.Failure("Could not store the file");
        }
    }

    /**
     * Finds the item based on the given id.
     *
     * @param id That will be used to search.
     * @return An {@link Optional} that contains the item if found, an empty optional otherwise.
     */
    public Optional<Item> findById(long id) {
        return this.itemRepository.findById(id);
    }

    /**
     * Makes an item count as viewed.
     *
     * @param id of the item that will be viewed.
     * @return A {@link ReviewResult} that informs if it's been reviewed or already has.
     */
    public ReviewResult review(long id) {
        Optional<Item> item = this.findById(id);
        if (item.isEmpty()) {
            return new ReviewResult.NotFound();
        }
        return this.itemRepository.review(id) ?
                  new ReviewResult.Reviewed() :
                  new ReviewResult.AlreadyReviewed();
    }

    /**
     * Updates the data
     *
     * @param id         of the item that will be updated.
     * @param itemUpdate New data.
     * @return A {@link Map} with information about the changes.
     */
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

    /**
     * Deletes the item of a given id.
     *
     * @param id of the item that will be deleted.
     * @return {@code true} if it was deleted, {@code false} otherwise.
     */
    public boolean deleteById(long id) {
        Optional<Item> found = this.itemRepository.findById(id);
        if (found.isEmpty()) { return false; }
        Item item = found.get();

        List<Variant> variants = this.variantRepository.findByParentId(id);

        for (Variant variant : variants) {
            try {
                this.storage.delete(variant.storagePath());
            } catch (IOException e) {
                LOGGER.error(
                        "Variant with ID {}, with item ID {}, and kind {} deleted successfully, but file ({}) could not be removed. Reason: {}.",
                        variant.id(),
                        variant.itemId(),
                        variant.kind(),
                        variant.storagePath(),
                        e.getLocalizedMessage(),
                        e
                );
            }
        }
        this.itemRepository.deleteById(id);
        return true;
    }
}
