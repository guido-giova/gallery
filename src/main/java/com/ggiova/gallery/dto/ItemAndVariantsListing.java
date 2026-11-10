package com.ggiova.gallery.dto;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record ItemAndVariantsListing(
        long id,
        boolean favorite,
        boolean reviewed,
        Instant createdAt,
        LocalDateTime takenAt,
        List<VariantListing> variants
) {}
