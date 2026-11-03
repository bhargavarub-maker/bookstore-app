package com.vone8.bookstore.books;

import com.vone8.bookstore.common.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Business logic for books. Controllers call this; this calls the repository. */
@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> search(String genre, String author, int limit) {
        // Filtering in Java keeps the example easy to read; a large catalog would filter in SQL.
        return repository.findAll(Sort.by("id")).stream()
                .filter(b -> genre == null || genre.equalsIgnoreCase(b.getGenre()))
                .filter(b -> author == null || b.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .limit(limit)
                .toList();
    }

    public Book get(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book " + id + " not found"));
    }

    @Transactional
    public Book create(BookRequest request) {
        Book book = new Book(request.title(), request.author(), request.genre(), request.price(), request.stock());
        return repository.save(book);
    }

    @Transactional
    public Book update(Long id, BookRequest request) {
        Book book = get(id);
        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setGenre(request.genre());
        book.setPrice(request.price());
        book.setStock(request.stock());
        return book; // changes are saved when the transaction commits
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(get(id));
    }
}
