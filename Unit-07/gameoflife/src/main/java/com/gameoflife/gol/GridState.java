package com.gameoflife.gol;

import java.util.Random;

/**
 * Immutable snapshot of a Game of Life grid.
 *
 * <p>All mutating operations return a new {@code GridState}, making history
 * management trivially safe (just keep references to past states).
 *
 * <p>Neighbors are counted over all 8 surrounding cells (Moore neighborhood).
 * When {@code toroidal} is true, edges wrap around.
 */
public final class GridState {

    private final boolean[][] cells;   // cells[row][col]
    private final int rows, cols;
    private final boolean toroidal;

    private GridState(boolean[][] cells, int rows, int cols, boolean toroidal) {
        this.cells    = cells;
        this.rows     = rows;
        this.cols     = cols;
        this.toroidal = toroidal;
    }

    // -------------------------------------------------------------------------
    // Factories
    // -------------------------------------------------------------------------

    /** Creates an empty grid of the given dimensions. */
    public static GridState empty(int rows, int cols, boolean toroidal) {
        return new GridState(new boolean[rows][cols], rows, cols, toroidal);
    }

    /**
     * Creates a grid with each cell randomly alive with the given probability.
     *
     * @param density fraction of cells that are alive (0.0 – 1.0)
     */
    public static GridState random(int rows, int cols, boolean toroidal, double density, long seed) {
        boolean[][] cells = new boolean[rows][cols];
        Random rng = new Random(seed);
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                cells[r][c] = rng.nextDouble() < density;
        return new GridState(cells, rows, cols, toroidal);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public int     rows()       { return rows; }
    public int     cols()       { return cols; }
    public boolean isToroidal() { return toroidal; }

    /** Returns true if cell (r, c) is alive. Out-of-bounds returns false (non-toroidal). */
    public boolean isAlive(int r, int c) {
        if (toroidal) {
            r = Math.floorMod(r, rows);
            c = Math.floorMod(c, cols);
        } else {
            if (r < 0 || r >= rows || c < 0 || c >= cols) return false;
        }
        return cells[r][c];
    }

    /** Counts the live cells in the grid. */
    public int population() {
        int count = 0;
        for (boolean[] row : cells)
            for (boolean cell : row)
                if (cell) count++;
        return count;
    }

    // -------------------------------------------------------------------------
    // Immutable modifications
    // -------------------------------------------------------------------------

    /** Returns a new state with cell (r, c) toggled. */
    public GridState withToggle(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return this;
        boolean[][] next = copyGrid();
        next[r][c] = !next[r][c];
        return new GridState(next, rows, cols, toroidal);
    }

    /** Returns a new state with cell (r, c) set to alive. */
    public GridState withAlive(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return this;
        if (cells[r][c]) return this;
        boolean[][] next = copyGrid();
        next[r][c] = true;
        return new GridState(next, rows, cols, toroidal);
    }

    /** Returns a new state with cell (r, c) set to dead. */
    public GridState withDead(int r, int c) {
        if (r < 0 || r >= rows || c < 0 || c >= cols) return this;
        if (!cells[r][c]) return this;
        boolean[][] next = copyGrid();
        next[r][c] = false;
        return new GridState(next, rows, cols, toroidal);
    }

    /**
     * Returns a new state with the given pattern placed so its top-left is at (row, col).
     *
     * @param centered if true, (row, col) is treated as the center of the pattern
     */
    public GridState withPattern(Pattern p, int row, int col, boolean centered) {
        if (centered) {
            row -= p.getHeight() / 2;
            col -= p.getWidth()  / 2;
        }
        boolean[][] next = copyGrid();
        for (int[] cell : p.getCells()) {
            int r = row + cell[0];
            int c = col + cell[1];
            if (toroidal) {
                r = Math.floorMod(r, rows);
                c = Math.floorMod(c, cols);
                next[r][c] = true;
            } else if (r >= 0 && r < rows && c >= 0 && c < cols) {
                next[r][c] = true;
            }
        }
        return new GridState(next, rows, cols, toroidal);
    }

    /** Returns a new state with all cells dead. */
    public GridState cleared() {
        return empty(rows, cols, toroidal);
    }

    /** Returns a new state with the same dimensions/toroidal but a different density fill. */
    public GridState randomFilled(double density, long seed) {
        return random(rows, cols, toroidal, density, seed);
    }

    // -------------------------------------------------------------------------
    // Simulation
    // -------------------------------------------------------------------------

    /**
     * Computes the next generation.
     *
     * @return [nextState, born count, died count]
     */
    public StepResult nextGeneration(RuleSet rules) {
        boolean[][] next = new boolean[rows][cols];
        int born = 0, died = 0;
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int n = countNeighbors(r, c);
                boolean alive = cells[r][c];
                boolean nextAlive = alive ? rules.survives(n) : rules.born(n);
                next[r][c] = nextAlive;
                if (!alive && nextAlive)  born++;
                if ( alive && !nextAlive) died++;
            }
        }
        return new StepResult(new GridState(next, rows, cols, toroidal), born, died);
    }

    /** Result of one simulation step. */
    public record StepResult(GridState state, int born, int died) {}

    private int countNeighbors(int r, int c) {
        int count = 0;
        for (int dr = -1; dr <= 1; dr++)
            for (int dc = -1; dc <= 1; dc++)
                if ((dr != 0 || dc != 0) && isAlive(r + dr, c + dc))
                    count++;
        return count;
    }

    // -------------------------------------------------------------------------
    // I/O support (raw cell export for StateIO)
    // -------------------------------------------------------------------------

    /** Returns a defensive copy of the raw cell array. */
    public boolean[][] rawCells() { return copyGrid(); }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean[][] copyGrid() {
        boolean[][] copy = new boolean[rows][cols];
        for (int r = 0; r < rows; r++) copy[r] = cells[r].clone();
        return copy;
    }
}
