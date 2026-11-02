package com.ggiova.gallery.dto;

import com.ggiova.gallery.data.VariantKind;

public record VariantListing(
    long id,
    VariantKind kind,
    String mime
) {}
