package com.ggiova.gallery.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = UploadResult.Success.class, name = "success"),
        @JsonSubTypes.Type(value = UploadResult.EmptyFile.class, name = "empty-file"),
        @JsonSubTypes.Type(value = UploadResult.InvalidFilename.class, name = "invalid-filename"),
        @JsonSubTypes.Type(value = UploadResult.Failure.class, name = "failure")
})
public sealed interface UploadResult {
    record Success(long id) implements UploadResult {}
    record EmptyFile() implements UploadResult {}
    record InvalidFilename() implements UploadResult {}
    record Failure(String reason) implements UploadResult {}
}
