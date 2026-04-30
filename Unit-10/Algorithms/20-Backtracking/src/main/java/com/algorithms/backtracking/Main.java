package com.algorithms.backtracking;

import java.util.List;

/**
 * Demonstrates backtracking on N-Queens, Sudoku, and word search.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Backtracking: N-Queens, Sudoku, Word Search ===\n");

        // --- N-Queens ---
        System.out.println("--- N-Queens ---");
        for (int n : new int[]{4, 6, 8}) {
            List<int[]> solutions = Backtracking.nQueens(n);
            System.out.printf("N=%d: %d solutions%n", n, solutions.size());
        }
        // Print one solution for N=8
        List<int[]> eightQueens = Backtracking.nQueens(8);
        int[] sol = eightQueens.get(0);
        System.out.println("\nOne solution for N=8:");
        for (int row = 0; row < 8; row++) {
            System.out.print("  ");
            for (int col = 0; col < 8; col++) System.out.print(col == sol[row] ? "Q " : ". ");
            System.out.println();
        }

        // --- Sudoku ---
        System.out.println("\n--- Sudoku Solver ---");
        int[][] puzzle = {
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
        System.out.println("Puzzle:");
        printBoard(puzzle);
        boolean solved = Backtracking.solveSudoku(puzzle);
        System.out.println("Solved: " + solved);
        if (solved) { System.out.println("Solution:"); printBoard(puzzle); }

        // --- Word Search ---
        System.out.println("\n--- Word Search ---");
        char[][] grid = {
            {'A', 'B', 'C', 'E'},
            {'S', 'F', 'C', 'S'},
            {'A', 'D', 'E', 'E'}
        };
        System.out.println("Grid:");
        for (char[] row : grid) {
            System.out.print("  ");
            for (char c : row) System.out.print(c + " ");
            System.out.println();
        }
        String[] words = {"ABCCED", "SEE", "ABCB", "SFCS"};
        for (String word : words) {
            System.out.printf("  Search \"%s\": %b%n", word, Backtracking.wordSearch(grid, word));
        }
    }

    private static void printBoard(int[][] board) {
        for (int r = 0; r < 9; r++) {
            if (r % 3 == 0) System.out.println("  +-------+-------+-------+");
            System.out.print("  | ");
            for (int c = 0; c < 9; c++) {
                System.out.print(board[r][c] == 0 ? "." : board[r][c]);
                System.out.print(c % 3 == 2 ? " | " : " ");
            }
            System.out.println();
        }
        System.out.println("  +-------+-------+-------+");
    }
}
