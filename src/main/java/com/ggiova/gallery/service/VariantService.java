package com.ggiova.gallery.service;

import com.ggiova.gallery.components.file.FileStorage;
import com.ggiova.gallery.data.Variant;
import com.ggiova.gallery.dto.*;
import com.ggiova.gallery.dto.internal.Content;
import com.ggiova.gallery.repository.VariantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VariantService {
    private static final Logger LOGGER = LoggerFactory.getLogger(VariantService.class);

    private final VariantRepository variantRepository;
    private final FileStorage storage;

    public VariantService(
            VariantRepository variantRepository,
            FileStorage storage
    ) {
        this.variantRepository = variantRepository;
        this.storage           = storage;
    }

    public List<Variant> listAll(long itemId) {
        return this.variantRepository.findByParentId(itemId);
    }

    public Optional<Content> getContent(long id) {
        return this.variantRepository.findById(id)
                                     .flatMap(variant -> storage.load(variant.storagePath())
                                     .map(res -> new Content(res, variant.mime())));
    }
}
