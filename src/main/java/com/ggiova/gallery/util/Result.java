package com.ggiova.gallery.util;

import java.net.URI;
import java.util.List;

public sealed interface Result<T> {
    record Ok<T>(T value) implements Result<T> {}
    record Created<T>(T value, URI location) implements Result<T> {}
    record NotFound<T>(String detail) implements Result<T> {}
    record Conflict<T>(String detail) implements Result<T> {}
    record Invalid<T>(List<String> errors) implements Result<T> {}
}
