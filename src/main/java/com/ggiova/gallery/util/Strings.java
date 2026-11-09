package com.ggiova.gallery.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * Utility class for managing {@link String}s.
 */
public final class Strings {
    private Strings() {
        throw new UnsupportedOperationException("Don't instantiate!");
    }

    /**
     * Generates a random UUID String.
     *
     * @return a random UUID String.
     */
    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Generates a random UUID String with the given termination value.
     *
     * @param termination Value that will be appended.
     * @return a random UUID String with {@code termination} at the end.
     */
    public static String randomUUIDWithTermination(String termination) {
        return randomUUID() + termination;
    }

    /**
     * Generates a UUID String with the extension of the {@see MultipartFile}.
     *
     * @param mf File from which the extension will be taken.
     * @return A UUID String with an extension.
     */
    public static String randomUUID(MultipartFile mf) {
        String originalFilename = mf.getOriginalFilename();

        if (originalFilename == null || !originalFilename.contains(".")) {
            return randomUUID();
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1);

        if (extension.isBlank()) {
            return randomUUID();
        }

        return randomUUID() + "." + extension;
    }
}
