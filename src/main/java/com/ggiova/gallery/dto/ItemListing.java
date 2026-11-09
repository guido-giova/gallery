package com.ggiova.gallery.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record ItemListing(
        long id,
        boolean favorite,
        boolean reviewed,
        Instant createdAt,
        LocalDateTime takenAt
) {}
