# 26 — Repository: The Book Inventory

## The Story

A bookstore management app needs to find books by ISBN, search by author, and
update stock when a sale happens. The business logic is straightforward.

But where are the books *stored*? In a PostgreSQL table today. Maybe in a
MongoDB collection after the migration next quarter. Definitely in a plain
`HashMap` during unit tests.

Without Repository, the service class is littered with SQL queries or ORM calls.
Swapping databases means editing business logic. Testing means spinning up a
database.

With Repository, the service calls `repository.findByIsbn("9780134685991")` and
knows nothing else. The repository interface is part of the *domain*. The SQL
implementation is an infrastructure detail. Tests substitute an in-memory
implementation with a single constructor call.

---

## The Problem It Solves

Direct database access from service classes:

- Couples business logic to a specific storage technology
- Makes unit testing require a live database
- Duplicates query logic across multiple callers

Repository gives domain objects a collection-like API for persistence, hiding
everything below the interface line.

---

## Structure

```
BookRepository              ← Interface (domain layer)
  └── InMemoryBookRepository← Concrete implementation (infrastructure layer)

Book                        ← Domain entity (record)
BookService                 ← Application service (depends only on interface)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Domain logic must be decoupled from storage | Swap SQL → NoSQL → in-memory |
| Unit tests must run without a database | Any service with DB access |
| Query logic needs a single home | Avoid scattered `SELECT *` across call sites |

Spring Data's `JpaRepository` and `MongoRepository` are Repository implementations.
The interface you extend is the Repository contract; Spring generates the
implementation at runtime.

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
