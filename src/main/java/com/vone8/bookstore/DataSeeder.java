package com.vone8.bookstore;

import com.vone8.bookstore.books.Book;
import com.vone8.bookstore.books.BookRepository;
import com.vone8.bookstore.orders.OrderRequest;
import com.vone8.bookstore.orders.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Fills an empty database with sample books and one order, so the API has data on first run. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final BookRepository books;
    private final OrderService orders;

    public DataSeeder(BookRepository books, OrderService orders) {
        this.books = books;
        this.orders = orders;
    }

    @Override
    public void run(String... args) {
        if (books.count() > 0) {
            return; // already has data (e.g. a PostgreSQL database from an earlier run)
        }
        List<Book> saved = books.saveAll(List.of(
                new Book("Dune", "Frank Herbert", "scifi", new BigDecimal("9.99"), 12),
                new Book("Project Hail Mary", "Andy Weir", "scifi", new BigDecimal("14.50"), 8),
                new Book("The Hobbit", "J.R.R. Tolkien", "fantasy", new BigDecimal("8.75"), 20),
                new Book("Clean Code", "Robert C. Martin", "programming", new BigDecimal("32.00"), 5),
                new Book("The Pragmatic Programmer", "Andrew Hunt, David Thomas", "programming", new BigDecimal("39.95"), 4),
                new Book("Designing Web APIs", "Brenda Jin, Saurabh Sahni, Amir Shevat", "programming", new BigDecimal("29.99"), 6),
                new Book("Pride and Prejudice", "Jane Austen", "classics", new BigDecimal("6.50"), 15),
                new Book("Sapiens", "Yuval Noah Harari", "history", new BigDecimal("18.00"), 3)));

        orders.place(new OrderRequest("Asha Rao", "asha@vone8.com", List.of(
                new OrderRequest.Line(saved.get(0).getId(), 1),
                new OrderRequest.Line(saved.get(3).getId(), 1))));
    }
}
