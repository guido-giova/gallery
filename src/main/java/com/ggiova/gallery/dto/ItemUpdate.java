package com.ggiova.gallery.dto;

public record ItemUpdate(
        Boolean isFavorite,
        String title,
        String description
) {}
