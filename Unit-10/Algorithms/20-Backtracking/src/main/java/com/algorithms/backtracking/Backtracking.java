package com.algorithms.backtracking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Backtracking examples: N-Queens, Sudoku solver, and word search on a grid.
 *
 * <p>Backtracking is a systematic way to search a solution space. It builds
 * candidates incrementally and abandons a branch as soon as it detects that
 * the current partial solution cannot lead to a valid complete solution
 * ("pruning"). This avoids exploring the entire exponential search space.
 *
 * <p>Template:
 * <pre>
 * void backtrack(state) {
 *     if (isComplete(state)) { record(state); return; }
 *     for (choice : choices(state)) {
 *         if (isValid(state, choice)) {
 *             apply(state, choice);
 *             backtrack(state);
 *             undo(state, choice);     ← key step: restore state
 *         }
 *     }
 * }
 * </pre>
 */
public final class Backtracking {

    private Backtracking() {}

    // ─── N-Queens ─────────────────────────────────────────────────────────────

    /**
     * Returns all solutions to the N-Queens problem: place N queens on an N×N
     * board so that no two queens attack each other (same row, column, or diagonal).
     * Each solution is a list of column indices (solution[row] = col).
     */
    public static List<int[]> nQueens(int n) {
        List<int[]> solutions = new ArrayList<>();
        int[] queens = new int[n];
        Arrays.fill(queens, -1);
        solveNQueens(0, queens, n, solutions);
        return solutions;
    }

    private static void solveNQueens(int row, int[] queens, int n, List<int[]> solutions) {
        if (row == n) { solutions.add(queens.clone()); return; }
        for (int col = 0; col < n; col++) {
            if (isSafeQueen(queens, row, col)) {
                queens[row] = col;
                solveNQueens(row + 1, queens, n, solutions);
                queens[row] = -1;
            }
        }
    }

    private static boolean isSafeQueen(int[] queens, int row, int col) {
        for (int r = 0; r < row; r++) {
            if (queens[r] == col) return false;                       // same column
            if (Math.abs(queens[r] - col) == Math.abs(r - row)) return false;  // diagonal
        }
        return true;
    }

    // ─── Sudoku Solver ────────────────────────────────────────────────────────

    /**
     * Solves a 9×9 Sudoku puzzle in-place.
     * Empty cells are represented by 0.
     * Returns true if the puzzle was solved; false if no solution exists.
     */
    public static boolean solveSudoku(int[][] board) {
        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] == 0) {
                    for (int num = 1; num <= 9; num++) {
                        if (isSafeSudoku(board, row, col, num)) {
                            board[row][col] = num;
                            if (solveSudoku(board)) return true;
                            board[row][col] = 0;  // backtrack
                        }
                    }
                    return false;  // no valid number found — backtrack further
                }
            }
        }
        return true;  // all cells filled
    }

    private static boolean isSafeSudoku(int[][] board, int row, int col, int num) {
        for (int i = 0; i < 9; i++) {
            if (board[row][i] == num) return false;  // row conflict
            if (board[i][col] == num) return false;  // column conflict
        }
        int boxRow = (row / 3) * 3, boxCol = (col / 3) * 3;
        for (int r = boxRow; r < boxRow + 3; r++)
            for (int c = boxCol; c < boxCol + 3; c++)
                if (board[r][c] == num) return false;  // 3×3 box conflict
        return true;
    }

    // ─── Word Search on Grid ──────────────────────────────────────────────────

    /**
     * Returns true if {@code word} can be found in the grid by traversing
     * adjacent cells (up, down, left, right), not reusing any cell in a single path.
     */
    public static boolean wordSearch(char[][] grid, String word) {
        int rows = grid.length, cols = grid[0].length;
        boolean[][] visited = new boolean[rows][cols];
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (dfsWord(grid, visited, r, c, word, 0)) return true;
        return false;
    }

    private static final int[][] DIRS = {{-1,0},{1,0},{0,-1},{0,1}};

    private static boolean dfsWord(char[][] grid, boolean[][] visited,
                                    int r, int c, String word, int idx) {
        if (idx == word.length()) return true;
        if (r < 0 || r >= grid.length || c < 0 || c >= grid[0].length) return false;
        if (visited[r][c] || grid[r][c] != word.charAt(idx)) return false;
        visited[r][c] = true;
        for (int[] d : DIRS)
            if (dfsWord(grid, visited, r + d[0], c + d[1], word, idx + 1)) {
                visited[r][c] = false;  // restore for other paths
                return true;
            }
        visited[r][c] = false;  // backtrack
        return false;
    }
}
