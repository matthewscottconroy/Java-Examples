package com.algorithms.backtracking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BacktrackingTest {

    // ─── N-Queens ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("N-Queens: correct solution count for small N")
    void nQueensCount() {
        assertEquals(1,   Backtracking.nQueens(1).size());
        assertEquals(0,   Backtracking.nQueens(2).size());
        assertEquals(0,   Backtracking.nQueens(3).size());
        assertEquals(2,   Backtracking.nQueens(4).size());
        assertEquals(10,  Backtracking.nQueens(5).size());
        assertEquals(4,   Backtracking.nQueens(6).size());
        assertEquals(92,  Backtracking.nQueens(8).size());
    }

    @Test
    @DisplayName("N-Queens: every solution has no attacking queens")
    void nQueensNoAttacks() {
        for (int[] sol : Backtracking.nQueens(8)) {
            for (int r1 = 0; r1 < 8; r1++) {
                for (int r2 = r1 + 1; r2 < 8; r2++) {
                    assertNotEquals(sol[r1], sol[r2], "Same column");
                    assertNotEquals(Math.abs(sol[r1] - sol[r2]),
                        Math.abs(r1 - r2), "Same diagonal");
                }
            }
        }
    }

    // ─── Sudoku ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Sudoku solver solves a valid puzzle")
    void sudokuSolves() {
        int[][] board = {
            {5, 3, 0,  0, 7, 0,  0, 0, 0},
            {6, 0, 0,  1, 9, 5,  0, 0, 0},
            {0, 9, 8,  0, 0, 0,  0, 6, 0},
            {8, 0, 0,  0, 6, 0,  0, 0, 3},
            {4, 0, 0,  8, 0, 3,  0, 0, 1},
            {7, 0, 0,  0, 2, 0,  0, 0, 6},
            {0, 6, 0,  0, 0, 0,  2, 8, 0},
            {0, 0, 0,  4, 1, 9,  0, 0, 5},
            {0, 0, 0,  0, 8, 0,  0, 7, 9}
        };
        assertTrue(Backtracking.solveSudoku(board));
        // Verify no zeros remain
        for (int[] row : board)
            for (int cell : row)
                assertNotEquals(0, cell);
    }

    @Test
    @DisplayName("Sudoku: solved board satisfies all constraints")
    void sudokuConstraints() {
        int[][] board = {
            {5, 3, 0,  0, 7, 0,  0, 0, 0},
            {6, 0, 0,  1, 9, 5,  0, 0, 0},
            {0, 9, 8,  0, 0, 0,  0, 6, 0},
            {8, 0, 0,  0, 6, 0,  0, 0, 3},
            {4, 0, 0,  8, 0, 3,  0, 0, 1},
            {7, 0, 0,  0, 2, 0,  0, 0, 6},
            {0, 6, 0,  0, 0, 0,  2, 8, 0},
            {0, 0, 0,  4, 1, 9,  0, 0, 5},
            {0, 0, 0,  0, 8, 0,  0, 7, 9}
        };
        Backtracking.solveSudoku(board);
        // Each row has 1-9 exactly once
        for (int[] row : board) {
            boolean[] seen = new boolean[10];
            for (int v : row) { assertFalse(seen[v]); seen[v] = true; }
        }
        // Each column has 1-9 exactly once
        for (int c = 0; c < 9; c++) {
            boolean[] seen = new boolean[10];
            for (int r = 0; r < 9; r++) { assertFalse(seen[board[r][c]]); seen[board[r][c]] = true; }
        }
    }

    // ─── Word Search ───────────────────────────────────────────────────────

    @Test
    @DisplayName("wordSearch finds ABCCED in grid")
    void wordSearchFound() {
        char[][] grid = {
            {'A', 'B', 'C', 'E'},
            {'S', 'F', 'C', 'S'},
            {'A', 'D', 'E', 'E'}
        };
        assertTrue(Backtracking.wordSearch(grid, "ABCCED"));
        assertTrue(Backtracking.wordSearch(grid, "SEE"));
    }

    @Test
    @DisplayName("wordSearch returns false when word not present")
    void wordSearchNotFound() {
        char[][] grid = {
            {'A', 'B', 'C', 'E'},
            {'S', 'F', 'C', 'S'},
            {'A', 'D', 'E', 'E'}
        };
        assertFalse(Backtracking.wordSearch(grid, "ABCB"));  // would need to reuse B
    }

    @Test
    @DisplayName("wordSearch finds single-character word")
    void wordSearchSingleChar() {
        char[][] grid = {{'X', 'Y'}, {'Z', 'W'}};
        assertTrue(Backtracking.wordSearch(grid, "X"));
        assertFalse(Backtracking.wordSearch(grid, "Q"));
    }

    @Test
    @DisplayName("wordSearch does not reuse cells")
    void wordSearchNoReuse() {
        char[][] grid = {{'A', 'A'}, {'A', 'A'}};
        // "AAAA" can be found; "AAAAA" cannot (only 4 cells)
        assertTrue(Backtracking.wordSearch(grid, "AAAA"));
        assertFalse(Backtracking.wordSearch(grid, "AAAAA"));
    }
}
