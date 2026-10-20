package com.ggiova.gallery.dto.internal;

import org.springframework.core.io.Resource;

public record Content(
        Resource resource,
        String mime
) {}
