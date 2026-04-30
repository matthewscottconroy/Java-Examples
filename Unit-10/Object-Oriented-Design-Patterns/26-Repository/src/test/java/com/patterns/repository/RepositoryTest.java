package com.patterns.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RepositoryTest {

    private BookRepository repo;
    private BookService    service;

    private static final Book EFFECTIVE_JAVA = new Book(
            "9780134685991", "Effective Java", "Joshua Bloch", 4999, 10);
    private static final Book CLEAN_CODE = new Book(
            "9780132350884", "Clean Code", "Robert Martin", 3999, 5);
    private static final Book DESIGN_PATTERNS = new Book(
            "9780201633610", "Design Patterns", "Erich Gamma", 5499, 3);

    @BeforeEach
    void setUp() {
        repo    = new InMemoryBookRepository();
        service = new BookService(repo);
        service.addBook(EFFECTIVE_JAVA);
        service.addBook(CLEAN_CODE);
        service.addBook(DESIGN_PATTERNS);
    }

    @Test
    @DisplayName("findByIsbn returns the correct book")
    void findByIsbn() {
        Optional<Book> result = service.findByIsbn("9780134685991");
        assertTrue(result.isPresent());
        assertEquals("Effective Java", result.get().title());
    }

    @Test
    @DisplayName("findByIsbn returns empty for unknown ISBN")
    void findByIsbnUnknown() {
        assertTrue(service.findByIsbn("0000000000000").isEmpty());
    }

    @Test
    @DisplayName("findByAuthor returns all books by that author")
    void findByAuthor() {
        List<Book> books = service.findByAuthor("Bloch");
        assertEquals(1, books.size());
        assertEquals("Effective Java", books.get(0).title());
    }

    @Test
    @DisplayName("search returns books whose title contains the term")
    void searchByTitle() {
        service.addBook(new Book("ISBN1", "Design Patterns Advanced", "Author X", 1000, 1));
        List<Book> results = service.search("Design");
        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("sell decrements stock by one")
    void sellDecreasesStock() {
        service.sell("9780134685991");
        int newStock = service.findByIsbn("9780134685991").get().stock();
        assertEquals(9, newStock);
    }

    @Test
    @DisplayName("selling out of stock throws")
    void sellOutOfStock() {
        Book noStock = new Book("NOSTK", "Empty Book", "Author", 100, 0);
        service.addBook(noStock);
        assertThrows(IllegalStateException.class, () -> service.sell("NOSTK"));
    }

    @Test
    @DisplayName("remove deletes the book from the repository")
    void remove() {
        service.remove("9780132350884");
        assertTrue(service.findByIsbn("9780132350884").isEmpty());
        assertEquals(2, service.catalogueSize());
    }

    @Test
    @DisplayName("catalogueSize reflects the number of books saved")
    void catalogueSize() {
        assertEquals(3, service.catalogueSize());
    }
}
