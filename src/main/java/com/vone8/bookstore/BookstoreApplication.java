package com.vone8.bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. Running this class starts a web server on http://localhost:8080
 * that serves the Books API (/v1/books), the Orders API (/v1/orders),
 * Swagger UI (/swagger-ui.html) and a small demo frontend (/).
 */
@SpringBootApplication
public class BookstoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookstoreApplication.class, args);
    }
}
