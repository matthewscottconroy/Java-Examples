package com.patterns.repository;

/**
 * Domain entity — a book in the inventory.
 *
 * @param isbn      13-digit ISBN (primary key)
 * @param title     full title
 * @param author    primary author's name
 * @param priceCents price in cents
 * @param stock     units in stock
 */
public record Book(String isbn, String title, String author, int priceCents, int stock) {

    public Book {
        if (isbn == null || isbn.isBlank())
            throw new IllegalArgumentException("ISBN is required");
        if (priceCents < 0)
            throw new IllegalArgumentException("price cannot be negative");
        if (stock < 0)
            throw new IllegalArgumentException("stock cannot be negative");
    }

    /** Return a copy with updated stock. */
    public Book withStock(int newStock) {
        return new Book(isbn, title, author, priceCents, newStock);
    }
}
