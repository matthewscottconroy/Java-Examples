package com.schelling.model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Two-dimensional grid of agent cells for the Schelling segregation model.
 *
 * <p>Each cell holds either an {@link AgentType} or {@code null} (empty).
 * The grid uses row-major addressing: {@code cells[row][col]}.
 *
 * <p>All neighbourhood, satisfaction, and segregation-index methods accept an
 * optional {@link NeighborhoodType} parameter.  The single-argument overloads
 * default to {@link NeighborhoodType#MOORE} for backward compatibility.
 */
public final class Grid {

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final int rows;
    private final int cols;
    private final AgentType[][] cells;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a grid with the given dimensions, all cells initialised to empty.
     *
     * @param rows number of rows, must be &ge; 1
     * @param cols number of columns, must be &ge; 1
     */
    public Grid(int rows, int cols) {
        if (rows < 1 || cols < 1)
            throw new IllegalArgumentException("Grid dimensions must be positive");
        this.rows  = rows;
        this.cols  = cols;
        this.cells = new AgentType[rows][cols];
    }

    // -------------------------------------------------------------------------
    // Cell access
    // -------------------------------------------------------------------------

    public AgentType getCell(int row, int col) {
        checkBounds(row, col);
        return cells[row][col];
    }

    public void setCell(int row, int col, AgentType type) {
        checkBounds(row, col);
        cells[row][col] = type;
    }

    public boolean isEmpty(int row, int col) {
        return getCell(row, col) == null;
    }

    // -------------------------------------------------------------------------
    // Grid dimensions
    // -------------------------------------------------------------------------

    public int getRows()       { return rows; }
    public int getCols()       { return cols; }
    public int getTotalCells() { return rows * cols; }

    // -------------------------------------------------------------------------
    // Cell enumeration
    // -------------------------------------------------------------------------

    public List<Point> getEmptyCells() {
        List<Point> result = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c] == null)
                    result.add(new Point(r, c));
        return Collections.unmodifiableList(result);
    }

    public List<Point> getOccupiedCells() {
        List<Point> result = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c] != null)
                    result.add(new Point(r, c));
        return Collections.unmodifiableList(result);
    }

    // -------------------------------------------------------------------------
    // Neighbourhood
    // -------------------------------------------------------------------------

    /**
     * Returns the non-empty agents in the given neighborhood of {@code (row, col)}.
     *
     * @param row row index of the focal cell
     * @param col column index of the focal cell
     * @param nt  neighborhood topology to use
     */
    public List<AgentType> getNeighbours(int row, int col, NeighborhoodType nt) {
        checkBounds(row, col);
        int[][] offsets = nt.offsets();
        List<AgentType> neighbours = new ArrayList<>(offsets.length);
        for (int[] off : offsets) {
            int nr = row + off[0], nc = col + off[1];
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                AgentType t = cells[nr][nc];
                if (t != null) neighbours.add(t);
            }
        }
        return Collections.unmodifiableList(neighbours);
    }

    /** Moore-neighborhood overload for backward compatibility. */
    public List<AgentType> getNeighbours(int row, int col) {
        return getNeighbours(row, col, NeighborhoodType.MOORE);
    }

    // -------------------------------------------------------------------------
    // Satisfaction
    // -------------------------------------------------------------------------

    /**
     * Computes the satisfaction ratio for the agent at {@code (row, col)}.
     * Returns 1.0 for empty or isolated cells.
     */
    public double getSatisfactionRatio(int row, int col, NeighborhoodType nt) {
        AgentType focal = getCell(row, col);
        if (focal == null) return 1.0;
        List<AgentType> neighbours = getNeighbours(row, col, nt);
        if (neighbours.isEmpty()) return 1.0;
        long sameCount = neighbours.stream().filter(t -> t == focal).count();
        return (double) sameCount / neighbours.size();
    }

    /** Moore-neighborhood overload for backward compatibility. */
    public double getSatisfactionRatio(int row, int col) {
        return getSatisfactionRatio(row, col, NeighborhoodType.MOORE);
    }

    /**
     * Returns {@code true} if the agent at {@code (row, col)} is satisfied.
     *
     * @param threshold minimum fraction of same-type neighbours required
     * @param nt        neighborhood topology
     */
    public boolean isSatisfied(int row, int col, double threshold, NeighborhoodType nt) {
        return getSatisfactionRatio(row, col, nt) >= threshold;
    }

    /** Moore-neighborhood overload for backward compatibility. */
    public boolean isSatisfied(int row, int col, double threshold) {
        return isSatisfied(row, col, threshold, NeighborhoodType.MOORE);
    }

    /**
     * Returns the fraction of occupied cells that are satisfied at the given
     * threshold (per-cell, uniform threshold).
     */
    public double getOverallSatisfactionRate(double threshold, NeighborhoodType nt) {
        List<Point> occupied = getOccupiedCells();
        if (occupied.isEmpty()) return 1.0;
        long satisfied = occupied.stream()
            .filter(p -> isSatisfied(p.x, p.y, threshold, nt))
            .count();
        return (double) satisfied / occupied.size();
    }

    /** Moore-neighborhood overload for backward compatibility. */
    public double getOverallSatisfactionRate(double threshold) {
        return getOverallSatisfactionRate(threshold, NeighborhoodType.MOORE);
    }

    // -------------------------------------------------------------------------
    // Segregation indices
    // -------------------------------------------------------------------------

    /**
     * Duncan &amp; Duncan (1955) <em>dissimilarity index</em>.
     *
     * <p>Computed at the cell level: {@code D = 0.5 × Σ |a_i/A − b_i/B|} where
     * {@code a_i = 1} if cell {@code i} is Type-A, {@code b_i = 1} if Type-B.
     * Ranges from 0 (perfect integration) to 1 (complete segregation).
     * Returns 0 if either group is absent.
     */
    public double getDissimilarityIndex() {
        long totalA = 0, totalB = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                if (cells[r][c] == AgentType.TYPE_A) totalA++;
                else if (cells[r][c] == AgentType.TYPE_B) totalB++;
            }
        if (totalA == 0 || totalB == 0) return 0.0;

        double sum = 0;
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                double ai = (cells[r][c] == AgentType.TYPE_A) ? 1.0 : 0.0;
                double bi = (cells[r][c] == AgentType.TYPE_B) ? 1.0 : 0.0;
                sum += Math.abs(ai / totalA - bi / totalB);
            }
        return 0.5 * sum;
    }

    /**
     * Bell (1954) <em>isolation index</em> P*<sub>tt</sub>.
     *
     * <p>The average fraction of a focal agent's occupied neighbours who share
     * its type.  Ranges from 0 (never surrounded by same type) to 1 (always
     * surrounded exclusively by same type).  Independent of total-group size.
     * Returns 0 for groups not present on the grid.
     *
     * @param type the focal agent type
     * @param nt   neighborhood topology to use
     */
    public double getIsolationIndex(AgentType type, NeighborhoodType nt) {
        List<Point> agents = new ArrayList<>();
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                if (cells[r][c] == type) agents.add(new Point(r, c));
        if (agents.isEmpty()) return 0.0;

        double sum = 0;
        for (Point p : agents) {
            List<AgentType> nbrs = getNeighbours(p.x, p.y, nt);
            if (nbrs.isEmpty()) continue;  // isolated → contributes 0
            long same = nbrs.stream().filter(t -> t == type).count();
            sum += (double) same / nbrs.size();
        }
        return sum / agents.size();
    }

    // -------------------------------------------------------------------------
    // Copy
    // -------------------------------------------------------------------------

    public Grid copy() {
        Grid copy = new Grid(rows, cols);
        for (int r = 0; r < rows; r++)
            System.arraycopy(cells[r], 0, copy.cells[r], 0, cols);
        return copy;
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private void checkBounds(int row, int col) {
        if (row < 0 || row >= rows || col < 0 || col >= cols)
            throw new IndexOutOfBoundsException(
                String.format("Cell (%d, %d) out of bounds for grid %dx%d", row, col, rows, cols));
    }

    @Override
    public String toString() {
        return String.format("Grid{%dx%d, occupied=%d, empty=%d}",
            rows, cols, getOccupiedCells().size(), getEmptyCells().size());
    }
}
