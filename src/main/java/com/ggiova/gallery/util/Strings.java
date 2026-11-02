package com.ggiova.gallery.util;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public final class Strings {

    private Strings() {}

    public static String randomUUID() {
        return UUID.randomUUID().toString();
    }

    public static String randomUUIDWithTermination(String termination) {
        return randomUUID() + termination;
    }

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
