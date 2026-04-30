# 01 — Recursive Structures

Immutable, persistent data structures built purely from sealed interfaces and structural recursion — no arrays, no mutation, no `null`.

## What it demonstrates

- **`FList<A>`** — a singly-linked functional list (`Nil | Cons(head, tail)`)  
  `map`, `filter`, `foldRight`, `foldLeft`, `append`, `reverse`, `flatMap`, `contains`
- **`BTree<A>`** — a binary search tree (`Leaf | Branch(value, left, right)`)  
  `insert`, `contains`, `size`, `height`, `min`, `max`, `inOrder`, `map`, `fold`
- Every operation is expressed as a recursive case split over the sealed type — no loops anywhere

## Key Java features

| Feature | Where |
|---------|-------|
| `sealed interface` + `record` components | `FList.java`, `BTree.java` |
| `switch` pattern matching | Every recursive method |
| Generic type bounds (`<A extends Comparable<A>>`) | `BTree.java` |
| Default interface methods | `show()`, `head()`, `isEmpty()` |

## Why not `toString()`?

Interface default methods cannot override `Object.toString()` — the compiler silently ignores the override and `Object.toString()` wins. The list representation method is therefore named `show()`.

## Run

```bash
mvn exec:java   # prints example FList and BTree operations
mvn test        # 20+ unit tests
```
