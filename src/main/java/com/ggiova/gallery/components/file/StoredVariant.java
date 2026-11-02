package com.ggiova.gallery.components.file;

import com.ggiova.gallery.data.VariantKind;

public record StoredVariant(
        VariantKind kind,
        String storagePath,
        String mime
) {}
