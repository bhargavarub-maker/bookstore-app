package com.vone8.bookstore.books;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

/** The JSON body a client sends to create or update a book. */
@Schema(description = "Fields needed to create or update a book")
public record BookRequest(
        @NotBlank @Schema(example = "Project Hail Mary") String title,
        @NotBlank @Schema(example = "Andy Weir") String author,
        @Schema(example = "scifi") String genre,
        @NotNull @DecimalMin("0.00") @Schema(example = "14.50") BigDecimal price,
        @NotNull @PositiveOrZero @Schema(example = "5") Integer stock) {
}
