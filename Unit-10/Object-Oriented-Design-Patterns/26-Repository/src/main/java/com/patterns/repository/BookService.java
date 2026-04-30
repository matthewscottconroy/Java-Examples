package com.patterns.repository;

import java.util.List;
import java.util.Optional;

/**
 * Application service — the domain layer that the UI or API layer calls.
 *
 * <p>Depends only on {@link BookRepository}; knows nothing about SQL,
 * file paths, or any other storage detail.
 */
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    /** Add a new book to the catalogue. */
    public void addBook(Book book) {
        repository.save(book);
        System.out.println("[Service] Added: " + book.title() + " (" + book.isbn() + ")");
    }

    /**
     * Sell one copy; reduce stock by one.
     *
     * @throws IllegalStateException if the book is not found or out of stock
     */
    public void sell(String isbn) {
        Book book = repository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalStateException("Book not found: " + isbn));
        if (book.stock() == 0)
            throw new IllegalStateException("Out of stock: " + book.title());
        repository.save(book.withStock(book.stock() - 1));
        System.out.println("[Service] Sold: " + book.title()
                + " (stock now " + (book.stock() - 1) + ")");
    }

    public Optional<Book> findByIsbn(String isbn)               { return repository.findByIsbn(isbn); }
    public List<Book>     findByAuthor(String author)           { return repository.findByAuthor(author); }
    public List<Book>     search(String term)                   { return repository.findByTitleContaining(term); }
    public void           remove(String isbn)                   { repository.delete(isbn); }
    public int            catalogueSize()                       { return repository.count(); }
}
