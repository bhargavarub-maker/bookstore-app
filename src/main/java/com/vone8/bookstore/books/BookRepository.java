package com.vone8.bookstore.books;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data writes the SQL for findAll, findById, save, delete, ... automatically. */
public interface BookRepository extends JpaRepository<Book, Long> {
}
