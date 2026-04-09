package com.gameoflife.gol;

import java.util.Arrays;

/**
 * An immutable pattern that can be placed onto a {@link GridState}.
 *
 * <p>Cells are stored as (row, col) offsets from the pattern's top-left corner.
 * A pattern's origin is at (0, 0); cells may use any non-negative offset.
 */
public final class Pattern {

    private final String   name;
    private final String   description;
    private final String   category;
    private final int[][]  cells;       // each int[] is {row, col}
    private final int      height;
    private final int      width;

    public Pattern(String name, String category, String description, int[][] cells) {
        this.name        = name;
        this.category    = category;
        this.description = description;
        this.cells       = cells.clone();

        int maxR = 0, maxC = 0;
        for (int[] cell : cells) {
            maxR = Math.max(maxR, cell[0]);
            maxC = Math.max(maxC, cell[1]);
        }
        this.height = maxR + 1;
        this.width  = maxC + 1;
    }

    public String  getName()        { return name; }
    public String  getCategory()    { return category; }
    public String  getDescription() { return description; }
    public int[][] getCells()       { return cells.clone(); }
    public int     getHeight()      { return height; }
    public int     getWidth()       { return width; }
    public int     getCellCount()   { return cells.length; }

    /** Returns an ASCII preview (up to 25×25). */
    public String toAscii() {
        int rows = Math.min(height, 25);
        int cols = Math.min(width,  25);
        char[][] grid = new char[rows][cols];
        for (char[] row : grid) Arrays.fill(row, '.');
        for (int[] cell : cells) {
            if (cell[0] < rows && cell[1] < cols) grid[cell[0]][cell[1]] = 'O';
        }
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            sb.append(new String(row)).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    @Override public String toString() { return name; }
}
