# 15 — Iterator: The Social Media Feed

## The Story

You open your social media app. The same set of posts can be presented three ways:
**Chronological** (newest first — the default), **Top Posts** (most-liked first),
or filtered to **one author's content only**.

The underlying collection of posts doesn't change. The data isn't re-fetched.
What changes is the **traversal strategy** — an Iterator that walks the collection
in a different order, presenting a different view of the same data.

This is the **Iterator** pattern: provide a sequential access interface to the
elements of a collection, without exposing the collection's internal structure.

---

## The Problem It Solves

- Clients should not need to know whether a collection is an array, a linked list,
  a tree, or a database result set.
- Multiple traversal strategies over the same data should be possible simultaneously.
- Adding a new traversal (e.g., "pinned posts first") should not require changing the collection class or any client code.

---

## Structure

```
SocialFeed          ← Aggregate (createIterator methods)
Post                ← Element

chronologicalIterator() → Iterator<Post>  (newest first)
engagementIterator()    → Iterator<Post>  (most liked first)
authorIterator(name)    → Iterator<Post>  (filtered)
```

Java's built-in `Iterator<T>` interface is used directly.

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Traverse a collection without revealing its structure | Database cursor, file reader |
| Support multiple traversal algorithms | Sorted, filtered, reversed |
| Unified traversal over different collection types | Java `for-each` works on arrays, lists, sets, custom types |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
