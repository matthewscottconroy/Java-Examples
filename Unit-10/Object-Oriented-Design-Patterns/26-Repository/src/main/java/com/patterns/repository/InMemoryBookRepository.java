package com.patterns.repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * In-memory implementation — stores books in a {@link LinkedHashMap}.
 *
 * <p>Used in tests and local development. A SQL or NoSQL implementation
 * would replace this without any change to {@link BookService}.
 */
public class InMemoryBookRepository implements BookRepository {

    private final Map<String, Book> store = new LinkedHashMap<>();

    @Override
    public void save(Book book) {
        store.put(book.isbn(), book);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(store.get(isbn));
    }

    @Override
    public List<Book> findByAuthor(String author) {
        String lower = author.toLowerCase();
        return store.values().stream()
                .filter(b -> b.author().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public List<Book> findByTitleContaining(String term) {
        String lower = term.toLowerCase();
        return store.values().stream()
                .filter(b -> b.title().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    @Override
    public void delete(String isbn) {
        store.remove(isbn);
    }

    @Override
    public int count() {
        return store.size();
    }
}
