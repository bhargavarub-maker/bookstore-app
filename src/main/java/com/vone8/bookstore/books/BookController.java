package com.vone8.bookstore.books;

import com.vone8.bookstore.common.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** The Books REST API: maps HTTP verbs + URLs to Java methods. */
@RestController
@RequestMapping("/v1/books")
@Validated
@Tag(name = "Books")
public class BookController {

    private final BookService service;

    public BookController(BookService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "listBooks", summary = "List books", description = "Optionally filter by genre or author, and cap the number of results.")
    public List<Book> list(
            @Parameter(description = "Exact genre, e.g. scifi") @RequestParam(required = false) String genre,
            @Parameter(description = "Part of the author's name") @RequestParam(required = false) String author,
            @Parameter(description = "Maximum results (1-100)") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return service.search(genre, author, limit);
    }

    @GetMapping("/{id}")
    @Operation(operationId = "getBook", summary = "Get one book")
    @ApiResponse(responseCode = "200", description = "The book")
    @ApiResponse(responseCode = "404", description = "No book with that id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public Book get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping
    @Operation(operationId = "createBook", summary = "Add a book")
    @ApiResponse(responseCode = "201", description = "Created; the Location header points to the new book")
    @ApiResponse(responseCode = "400", description = "Invalid body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public ResponseEntity<Book> create(@Valid @RequestBody BookRequest request) {
        Book created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(operationId = "updateBook", summary = "Replace a book's details")
    @ApiResponse(responseCode = "200", description = "Updated")
    @ApiResponse(responseCode = "400", description = "Invalid body",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    @ApiResponse(responseCode = "404", description = "No book with that id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public Book update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "deleteBook", summary = "Delete a book")
    @ApiResponse(responseCode = "204", description = "Deleted")
    @ApiResponse(responseCode = "404", description = "No book with that id",
            content = @Content(schema = @Schema(implementation = ApiError.class)))
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
