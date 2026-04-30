# 19 — Binary Search Tree: Stock Price Index

## The Story

A trading terminal needs to look up a stock's price by ticker symbol, iterate through all symbols in alphabetical order, and answer range queries like "what's the nearest ticker to ORACLE in the index?" A BST stores tickers as keys and prices as values, giving O(log n) lookup and sorted iteration for free.

---

## BST Invariant

For every node N:
- All keys in the **left** subtree are **less than** N.key
- All keys in the **right** subtree are **greater than** N.key

```
Insert: 5, 3, 7, 1, 4, 6, 8

         5
        / \
       3   7
      / \ / \
     1  4 6  8
```

This invariant means binary search works at every level: compare the target with the current node and go left or right.

---

## Operations

| Operation | Average | Worst (sorted input) |
|-----------|---------|---------------------|
| `put`     | O(log n) | O(n) |
| `get`     | O(log n) | O(n) |
| `inOrder` | O(n)    | O(n) |
| `min/max` | O(log n) | O(n) |
| `floor/ceiling` | O(log n) | O(n) |

Worst case occurs when keys are inserted in sorted order — the tree degenerates into a linked list.

---

## Floor and Ceiling

**Floor(k)** — largest key ≤ k. Useful for "what's the last price recorded before time T?"  
**Ceiling(k)** — smallest key ≥ k. Useful for "what's the next scheduled event after time T?"

These are harder to express with a HashMap but trivially efficient in a BST.

---

## In-Order Traversal

In-order traversal (left → node → right) visits keys in sorted order. This is the key property that makes BST different from a hash table: a BST maintains order, a HashMap does not.

---

## Self-Balancing Trees

The `java.util.TreeMap` is a **Red-Black Tree** — a self-balancing BST that guarantees O(log n) in the worst case by maintaining a height constraint. For production use, prefer `TreeMap` over a hand-rolled BST.

---

## Commands

```bash
mvn compile exec:java
mvn test
```
