package com.functional.streams;

import java.time.LocalDate;
import java.util.List;

/** Sample sales dataset used by the demo and tests. */
public final class SalesData {

    private SalesData() {}

    public static List<Sale> sample() {
        return List.of(
            new Sale(1,  "Laptop Pro",     "Electronics", "North", "Alice", 3,  1299.99, LocalDate.of(2024,  1, 15)),
            new Sale(2,  "Wireless Mouse", "Electronics", "South", "Bob",   12,   29.99, LocalDate.of(2024,  1, 20)),
            new Sale(3,  "Desk Chair",     "Furniture",   "East",  "Carol",  2,  449.00, LocalDate.of(2024,  2,  5)),
            new Sale(4,  "Laptop Pro",     "Electronics", "West",  "Dave",   5, 1299.99, LocalDate.of(2024,  2, 12)),
            new Sale(5,  "Standing Desk",  "Furniture",   "North", "Alice",  1,  799.00, LocalDate.of(2024,  3,  1)),
            new Sale(6,  "Wireless Mouse", "Electronics", "East",  "Carol", 20,   29.99, LocalDate.of(2024,  3, 18)),
            new Sale(7,  "Monitor 4K",     "Electronics", "South", "Bob",    4,  599.00, LocalDate.of(2024,  4,  9)),
            new Sale(8,  "Desk Chair",     "Furniture",   "West",  "Dave",   6,  449.00, LocalDate.of(2024,  4, 22)),
            new Sale(9,  "Laptop Pro",     "Electronics", "North", "Alice",  2, 1299.99, LocalDate.of(2024,  5,  3)),
            new Sale(10, "Keyboard",       "Electronics", "South", "Bob",   15,   89.99, LocalDate.of(2024,  5, 17)),
            new Sale(11, "Bookshelf",      "Furniture",   "East",  "Carol",  3,  249.00, LocalDate.of(2024,  6, 10)),
            new Sale(12, "Monitor 4K",     "Electronics", "North", "Dave",   2,  599.00, LocalDate.of(2024,  6, 28))
        );
    }
}
