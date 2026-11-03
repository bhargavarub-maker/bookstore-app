package com.vone8.bookstore.orders;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

/** The JSON body a client sends to place an order. */
@Schema(description = "Who is ordering and which books")
public record OrderRequest(
        @NotBlank @Schema(example = "Asha Rao") String customerName,
        @NotBlank @Email @Schema(example = "asha@vone8.com") String customerEmail,
        @NotEmpty List<@Valid Line> items) {

    @Schema(name = "OrderLineRequest", description = "A book id and how many copies")
    public record Line(
            @NotNull @Schema(example = "1") Long bookId,
            @NotNull @Positive @Schema(example = "2") Integer quantity) {
    }
}
