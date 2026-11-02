package com.ggiova.gallery.data;

/**
 * Defines the type of the variant. Used for searching different sizes.
 */
public enum VariantKind {
    /**
     * Original file.
     */
    ORIGINAL,
    /**
     * Smaller size file. 480p video.
     */
    THUMBNAIL,
    /**
     * Lowest resolution. Videos become images.
     */
    LOW_RES
}
