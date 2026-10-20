package com.ggiova.gallery.dto;

public interface Responser<T> {
    org.springframework.http.ResponseEntity<T> toResponseEntity();
}
