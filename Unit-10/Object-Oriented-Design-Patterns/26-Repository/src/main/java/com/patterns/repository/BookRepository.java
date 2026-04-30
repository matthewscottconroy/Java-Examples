package com.patterns.repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface — the domain's view of book persistence.
 *
 * <p>Domain code depends only on this interface; it never knows whether
 * data comes from a SQL database, a flat file, or an in-memory map.
 */
public interface BookRepository {

    /** Persist a new book or replace an existing one with the same ISBN. */
    void save(Book book);

    /** Find a book by its ISBN, returning empty if it doesn't exist. */
    Optional<Book> findByIsbn(String isbn);

    /** Return all books written by the given author (case-insensitive). */
    List<Book> findByAuthor(String author);

    /** Return all books whose title contains the search term (case-insensitive). */
    List<Book> findByTitleContaining(String term);

    /** Remove a book by ISBN. No-op if the ISBN is not found. */
    void delete(String isbn);

    /** Total number of books in the repository. */
    int count();
}
