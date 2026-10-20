package com.ggiova.gallery.dto;

import org.springframework.http.ResponseEntity;

public sealed interface ReviewResult extends Responser<String> {
    record NotFound() implements ReviewResult {}
    record AlreadyReviewed() implements ReviewResult {}
    record Reviewed() implements ReviewResult {}

    @Override
    default ResponseEntity<String> toResponseEntity() {
        return switch (this) {
            case ReviewResult.AlreadyReviewed() -> ResponseEntity.ok("already-reviewed");
            case ReviewResult.NotFound() -> ResponseEntity.notFound().build();
            case ReviewResult.Reviewed() -> ResponseEntity.ok("reviewed");
        };
    }
}
