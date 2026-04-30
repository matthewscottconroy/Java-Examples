# 20 — Backtracking: N-Queens, Sudoku, Word Search

## The Story

A Sudoku puzzle has 81 cells, each with up to 9 choices. Trying every combination is 9⁸¹ — impossible. Backtracking places a number in the first empty cell, checks if it violates any constraint, and if not, recurses. When a cell has no valid number, it backtracks to the previous cell and tries the next option. Most branches are pruned early; a typical Sudoku is solved in milliseconds.

---

## The Backtracking Template

```java
void backtrack(state) {
    if (isComplete(state)) {
        record(state);
        return;
    }
    for (choice : choices(state)) {
        if (isValid(state, choice)) {
            apply(state, choice);     // make the move
            backtrack(state);         // recurse
            undo(state, choice);      // restore state (key step)
        }
    }
}
```

The `undo` step is what distinguishes backtracking from plain recursion. By restoring state before trying the next choice, the algorithm can explore all branches without side effects between them.

---

## Three Problems in This Module

### N-Queens
Place N queens on an N×N board so no two attack each other. Solution space: N! column permutations. Pruning: check column and diagonal conflicts as each queen is placed — never place a queen in an attacking position.

| N | Solutions |
|---|----------|
| 4 | 2 |
| 6 | 4 |
| 8 | 92 |

### Sudoku Solver
Fill a 9×9 grid so each row, column, and 3×3 box contains 1–9 exactly once. Backtrack when a cell has no valid digit.

### Word Search
Find a word in a grid by traversing adjacent cells (no reuse). Mark cells as visited when entering, unmark when backtracking — this allows the same cell to be used in different paths.

---

## Pruning is Everything

Backtracking without pruning is just brute force. Effective pruning (checking constraints before recursing) is what makes backtracking practical. For N-Queens:
- Without pruning: O(N^N) calls
- With pruning (column + diagonal check): O(N!) in the worst case, but actual calls are much fewer

More aggressive pruning (constraint propagation, as in Sudoku solvers like Peter Norvig's) reduces the search space further.

---

## Commands

```bash
mvn compile exec:java
mvn test
```
