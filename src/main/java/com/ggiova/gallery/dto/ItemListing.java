package com.ggiova.gallery.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record ItemListing(
        long id,
        boolean isFavorite,
        boolean wasReviewed,
        Instant createdAt,
        LocalDateTime takenAt
) {}
