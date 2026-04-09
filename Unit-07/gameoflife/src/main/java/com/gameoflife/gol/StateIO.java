package com.gameoflife.gol;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain-text save/load for Game of Life states.
 *
 * <h2>File Format</h2>
 * <pre>
 * # Conway's Game of Life — State File
 * # Format: CGOL v1
 * rule: B3/S23
 * generation: 42
 * rows: 80
 * cols: 100
 * toroidal: false
 * cells:
 * 3,4
 * 3,5
 * 10,22
 * ...
 * </pre>
 *
 * Lines starting with {@code #} are comments. Key/value pairs precede the
 * {@code cells:} marker. After the marker, each line is a {@code row,col} pair.
 */
public final class StateIO {

    private StateIO() {}

    // -------------------------------------------------------------------------
    // Save
    // -------------------------------------------------------------------------

    /**
     * Saves the current game state to a file.
     *
     * @param path       destination file path
     * @param state      grid state to save
     * @param ruleSet    rule set in use
     * @param generation current generation number
     */
    public static void save(Path path, GridState state, RuleSet ruleSet, int generation)
            throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(path)) {
            w.write("# Conway's Game of Life — State File\n");
            w.write("# Format: CGOL v1\n");
            w.write("rule: "       + ruleSet.toNotation() + "\n");
            w.write("generation: " + generation           + "\n");
            w.write("rows: "       + state.rows()         + "\n");
            w.write("cols: "       + state.cols()         + "\n");
            w.write("toroidal: "   + state.isToroidal()   + "\n");
            w.write("population: " + state.population()   + "\n");
            w.write("cells:\n");
            for (int r = 0; r < state.rows(); r++)
                for (int c = 0; c < state.cols(); c++)
                    if (state.isAlive(r, c))
                        w.write(r + "," + c + "\n");
        }
    }

    // -------------------------------------------------------------------------
    // Load
    // -------------------------------------------------------------------------

    /** Result of loading a state file. */
    public record LoadResult(
        GridState state,
        RuleSet   ruleSet,
        int       generation,
        String    error     // null on success
    ) {
        public boolean ok() { return error == null; }
    }

    /** Loads a state from file. Always returns a non-null LoadResult; check {@code ok()}. */
    public static LoadResult load(Path path) {
        try {
            return parse(Files.readString(path));
        } catch (IOException e) {
            return new LoadResult(null, null, 0, "Cannot read file: " + e.getMessage());
        }
    }

    /** Parses a state from a string (for testing or clipboard import). */
    public static LoadResult parse(String text) {
        String   ruleNotation = "B3/S23";
        int      generation   = 0;
        int      rows         = 0, cols = 0;
        boolean  toroidal     = false;
        boolean  inCells      = false;
        List<int[]> liveCells = new ArrayList<>();

        for (String line : text.lines().toList()) {
            String t = line.trim();
            if (t.isEmpty() || t.startsWith("#")) continue;

            if (t.equals("cells:")) { inCells = true; continue; }

            if (inCells) {
                String[] parts = t.split(",");
                if (parts.length < 2) continue;
                try {
                    liveCells.add(new int[]{Integer.parseInt(parts[0].trim()),
                                            Integer.parseInt(parts[1].trim())});
                } catch (NumberFormatException ignored) {}
                continue;
            }

            // Key: value pairs
            int colon = t.indexOf(':');
            if (colon < 0) continue;
            String key = t.substring(0, colon).trim().toLowerCase();
            String val = t.substring(colon + 1).trim();

            switch (key) {
                case "rule"       -> ruleNotation = val;
                case "generation" -> generation   = parseInt(val, 0);
                case "rows"       -> rows          = parseInt(val, 0);
                case "cols"       -> cols          = parseInt(val, 0);
                case "toroidal"   -> toroidal      = Boolean.parseBoolean(val);
            }
        }

        if (rows <= 0 || cols <= 0)
            return new LoadResult(null, null, 0, "Invalid or missing rows/cols in file.");

        RuleSet ruleSet;
        try {
            ruleSet = RuleSet.fromNotation(ruleNotation);
        } catch (IllegalArgumentException e) {
            return new LoadResult(null, null, 0, "Invalid rule: " + e.getMessage());
        }

        GridState state = GridState.empty(rows, cols, toroidal);
        for (int[] cell : liveCells) state = state.withAlive(cell[0], cell[1]);

        return new LoadResult(state, ruleSet, generation, null);
    }

    // -------------------------------------------------------------------------
    // RLE import (subset — handles simple Golly patterns)
    // -------------------------------------------------------------------------

    /**
     * Loads a pattern from an RLE-format string (as used by Golly and LifeWiki).
     * Only supports the basic {@code b}/{@code o}/{@code $}/{@code !} tokens.
     *
     * @return a {@link Pattern}, or null with an error message if parsing failed
     */
    public record RLEResult(Pattern pattern, String error) {
        public boolean ok() { return error == null; }
    }

    public static RLEResult importRLE(String rle, String name, String category) {
        List<int[]> cells = new ArrayList<>();
        int row = 0, col = 0;
        int count = 0;
        boolean done = false;

        for (char ch : rle.toCharArray()) {
            if (done) break;
            if (Character.isDigit(ch)) {
                count = count * 10 + (ch - '0');
            } else {
                int n = Math.max(1, count);
                count = 0;
                switch (ch) {
                    case 'b' -> col += n;      // dead cells
                    case 'o' -> {              // alive cells
                        for (int i = 0; i < n; i++) cells.add(new int[]{row, col++});
                    }
                    case '$' -> { row += n; col = 0; }   // next row(s)
                    case '!' -> done = true;
                }
            }
        }
        if (cells.isEmpty()) return new RLEResult(null, "No alive cells found in RLE data.");
        return new RLEResult(new Pattern(name, category, "Imported from RLE", cells.toArray(int[][]::new)), null);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return def; }
    }
}
