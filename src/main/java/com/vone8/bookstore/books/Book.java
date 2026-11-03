package com.vone8.bookstore.books;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** One row in the "books" table. JPA turns this class into SQL for us. */
@Entity
@JsonPropertyOrder({"id", "title", "author", "genre", "price", "stock"})
@Table(name = "books")
@Schema(description = "A book in the catalog")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(example = "1")
    private Long id;

    @Column(nullable = false)
    @Schema(example = "Dune")
    private String title;

    @Column(nullable = false)
    @Schema(example = "Frank Herbert")
    private String author;

    @Schema(example = "scifi")
    private String genre;

    @Column(nullable = false, precision = 10, scale = 2)
    @Schema(example = "9.99")
    private BigDecimal price;

    @Column(nullable = false)
    @Schema(description = "Copies available to order", example = "12")
    private int stock;

    protected Book() {
        // required by JPA
    }

    public Book(String title, String author, String genre, BigDecimal price, int stock) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.price = price;
        this.stock = stock;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setGenre(String genre) { this.genre = genre; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
}
