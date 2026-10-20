package com.ggiova.gallery.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

public final class Responses {
    private Responses() {}

    public static <T> ResponseEntity<Object> toResponse(Result<T> result) {
        return switch (result) {
            case Result.Ok<T>(var value) -> ResponseEntity.ok(value);
            case Result.Created<T>(var value, var location) -> ResponseEntity.created(location).body(value);
            case Result.NotFound<T>(var detail) -> problem(HttpStatus.NOT_FOUND, detail);
            case Result.Conflict<T>(var detail) -> problem(HttpStatus.CONFLICT, detail);
            case Result.Invalid<T>(var errors) -> {
                var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
                pd.setProperty("errors", errors);
                yield ResponseEntity.of(pd).build();
            }
        };
    }

    private static ResponseEntity<Object> problem(HttpStatus status, String detail) {
        return ResponseEntity.of(ProblemDetail.forStatusAndDetail(status, detail)).build();
    }
}