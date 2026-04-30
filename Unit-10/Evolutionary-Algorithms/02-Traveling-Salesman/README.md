# 02 — Traveling Salesman: Delivery Route Optimizer

## The Story

A courier company starts each day from a warehouse and must visit 15 delivery stops before returning. The number of possible routes is 14! ≈ 87 billion — exhaustive search is impossible. A GA with a permutation chromosome and problem-aware crossover finds a near-optimal route in seconds.

---

## Why Binary Chromosomes Don't Work Here

A binary string can't represent a route: bit-flip mutation and single-point crossover produce sequences with repeated or missing cities. The TSP requires a **permutation representation** where every city appears exactly once.

Operators must be redesigned to respect the permutation constraint.

---

## Ordered Crossover (OX)

OX preserves the relative order of cities from each parent:

```
Parent 1:  1  2  3 | 4  5  6 | 7  8  9
Parent 2:  5  7  4 | 9  1  3 | 8  2  6
               segment copied from P1
                     ↓
Child:     _  _  _   4  5  6   _  _  _
                     fill from P2 in order, skipping 4,5,6:
→ P2 from position after segment: 8,2,9,1,3 (skip 4,5,6 → keeps all)
Wait, let's trace:
P2 order starting after cut:  8,2,6, 5,7,4, 9,1,3 → skip 4,5,6 → 8,2,9,1,3,7
Child:     8  2  9   4  5  6   1  3  7
```

The segment from parent1 is preserved verbatim; the remaining positions are filled from parent2 in order, wrapping around, skipping already-placed cities.

---

## Swap Mutation

Instead of bit-flip, choose two random positions and swap the cities there. This always produces a valid permutation.

```
Before: [3, 1, 4, 2, 5, 0]
Swap positions 1 and 4:
After:  [3, 5, 4, 2, 1, 0]
```

---

## The NP-Hard Reality

TSP is NP-hard: no polynomial-time exact algorithm is known. For 15 cities a GA finds near-optimal routes; for 1000+ cities, specialised heuristics (Lin-Kernighan, Concorde) are needed. The GA demonstrates the general approach — not the state of the art.

---

## Commands

```bash
mvn compile exec:java
mvn test
```
