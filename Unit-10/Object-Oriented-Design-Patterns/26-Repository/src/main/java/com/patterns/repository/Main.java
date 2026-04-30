package com.patterns.repository;

import java.util.List;

/**
 * Demonstrates the Repository pattern with a bookstore inventory system.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Book Inventory (Repository Pattern) ===\n");

        BookRepository repo    = new InMemoryBookRepository();
        BookService    service = new BookService(repo);

        service.addBook(new Book("9780134685991", "Effective Java",        "Joshua Bloch",   4999, 12));
        service.addBook(new Book("9780596007126", "Head First Design Patterns", "Eric Freeman", 4499, 8));
        service.addBook(new Book("9780132350884", "Clean Code",            "Robert Martin",  3999,  5));
        service.addBook(new Book("9780201633610", "Design Patterns (GoF)", "Erich Gamma",    5499,  3));

        System.out.println("\n--- Find by author ---");
        List<Book> byBloch = service.findByAuthor("Bloch");
        byBloch.forEach(b -> System.out.printf("  %s — %s%n", b.isbn(), b.title()));

        System.out.println("\n--- Search by title keyword ---");
        List<Book> designBooks = service.search("Design");
        designBooks.forEach(b -> System.out.printf("  %s — %s%n", b.isbn(), b.title()));

        System.out.println("\n--- Sell two copies of Effective Java ---");
        service.sell("9780134685991");
        service.sell("9780134685991");
        service.findByIsbn("9780134685991")
               .ifPresent(b -> System.out.println("  Stock remaining: " + b.stock()));

        System.out.println("\nTotal books in catalogue: " + service.catalogueSize());
    }
}
