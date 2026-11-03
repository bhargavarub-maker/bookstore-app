package com.vone8.bookstore.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

/** The JSON body returned for every error, so clients always see the same shape. */
@Schema(description = "Error details")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        @Schema(example = "404") int status,
        @Schema(example = "Not Found") String error,
        @Schema(example = "Book 99 not found") String message,
        @Schema(example = "/v1/books/99") String path,
        @Schema(description = "Field problems, only for validation errors") List<String> details) {
}
