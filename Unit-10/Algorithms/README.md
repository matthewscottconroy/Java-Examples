# Algorithms

A collection of 20 self-contained Java modules covering the core algorithms every software engineer should understand. Each module is a standalone Maven project with a real-world story, working source code, and JUnit 5 tests.

---

## Why study algorithms?

Data structures and algorithms are the vocabulary of problem-solving. A developer who knows only language APIs is limited to the solutions the library designers anticipated. A developer who understands algorithms can recognise the *shape* of a problem — "this is essentially a shortest-path problem," "this has optimal substructure, so DP applies" — and reach for the right tool before the problem becomes a performance crisis.

Algorithms also teach you to reason about cost. "Will this work for 10,000 inputs? 10 million?" is a question you can only answer confidently when you understand O(n log n) vs O(n²) in concrete terms.

---

## What you gain

**Recognising problem shapes.** Most real problems are disguised versions of well-studied ones. Scheduling meetings in a conference room is the activity selection problem. Finding the quickest route across a city is Dijkstra. Autocomplete is trie search. Once you see the shape, the solution is known.

**Vocabulary to communicate with teammates.** "We need Union-Find here to track connected components" is more precise and useful than "I think we can avoid re-checking the whole graph."

**Reasoning about trade-offs.** Every algorithm involves trade-offs between time, space, simplicity, and correctness on edge cases. Understanding the theory lets you make those trade-offs deliberately.

---

## The forces that algorithms resolve

**Time complexity** — naïve solutions that work for 1,000 rows break at 1,000,000. The right algorithm often has a 100× or 1,000× real-world speed difference.

**Space complexity** — a full DP table may require O(n²) memory; rolling-row optimisation brings it to O(n). Understanding this prevents OOM errors.

**Correctness on edge cases** — off-by-one errors in binary search, degenerate input for quicksort, re-entrant memoisation — these only appear if you understand the algorithm's invariants.

---

## Module Index

### Sorting

| # | Module | Algorithm | Time | Story |
|---|--------|-----------|------|-------|
| 01 | [Elementary Sorts](01-Elementary-Sorts/) | Bubble, Selection, Insertion | O(n²) | Book title sorter with instrumentation |
| 02 | [Merge Sort](02-Merge-Sort/) | Merge sort (stable) | O(n log n) | Hospital patient triage queue |
| 03 | [Quick Sort](03-Quick-Sort/) | Three-way quicksort | O(n log n) avg | Product catalogue sorter |
| 04 | [Heap Sort](04-Heap-Sort/) | Heap sort + MinHeap | O(n log n) | Task scheduler (priority queue) |

### Search

| # | Module | Algorithm | Time | Story |
|---|--------|-----------|------|-------|
| 05 | [Binary Search](05-Binary-Search/) | Binary search variants | O(log n) | "First broken release" version finder |
| 06 | [Two-Pointer](06-Two-Pointer/) | Two-pointer, sliding window | O(n) | Network bandwidth monitor |

### Graphs

| # | Module | Algorithm | Time | Story |
|---|--------|-----------|------|-------|
| 07 | [BFS](07-BFS/) | Breadth-first search | O(V+E) | Office floor navigation |
| 08 | [DFS](08-DFS/) | Depth-first search + cycle detection | O(V+E) | Module dependency analyser |
| 09 | [Dijkstra](09-Dijkstra/) | Dijkstra's shortest path | O((V+E) log V) | City road navigation |
| 10 | [Topological Sort](10-Topological-Sort/) | Kahn's + DFS post-order | O(V+E) | Course prerequisite planner |
| 11 | [Union-Find](11-Union-Find/) | Union-Find (path compression) | O(α(n)) | Network connectivity + Kruskal's MST |

### Dynamic Programming

| # | Module | Algorithm | Time | Story |
|---|--------|-----------|------|-------|
| 12 | [DP Foundations](12-DP-Foundations/) | Fibonacci, Coin Change, LIS, Grid Paths | varies | Memoization vs tabulation |
| 13 | [LCS](13-Longest-Common-Subsequence/) | LCS + Edit Distance | O(mn) | Code diff tool / spell checker |
| 14 | [Knapsack](14-Knapsack/) | 0/1 Knapsack | O(nW) | Cloud service deployment optimizer |

### Greedy

| # | Module | Algorithm | Time | Story |
|---|--------|-----------|------|-------|
| 15 | [Activity Scheduling](15-Activity-Scheduling/) | Earliest-finish-time greedy | O(n log n) | Conference room booking |
| 16 | [Huffman Coding](16-Huffman-Coding/) | Huffman tree | O(n log n) | Log file compression |

### Strings

| # | Module | Algorithm | Time | Story |
|---|--------|-----------|------|-------|
| 17 | [KMP String Search](17-KMP-String-Search/) | Knuth-Morris-Pratt | O(n+m) | Log pattern finder |
| 18 | [Trie](18-Trie/) | Prefix tree | O(L) per op | Search autocomplete |

### Trees and Backtracking

| # | Module | Algorithm | Time | Story |
|---|--------|-----------|------|-------|
| 19 | [Binary Search Tree](19-Binary-Search-Tree/) | BST with floor/ceiling | O(log n) avg | Stock price index |
| 20 | [Backtracking](20-Backtracking/) | N-Queens, Sudoku, Word Search | exponential w/ pruning | Constraint satisfaction |

---

## Running any module

```bash
cd 05-Binary-Search
mvn compile exec:java   # run the demo
mvn test                # run the tests
```

All modules use Java 17 and JUnit Jupiter 5.10.2. No shared parent POM — each module is self-contained.

---

## A note on complexity notation

O(n) describes an upper bound on growth as input size n increases. The constants matter in practice — an O(n log n) algorithm with a large constant may be slower than an O(n²) algorithm for small n. Profile before optimising, but understand the asymptotic behaviour before writing the code.
