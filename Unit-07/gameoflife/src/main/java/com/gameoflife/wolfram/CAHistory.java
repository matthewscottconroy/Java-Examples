package com.gameoflife.wolfram;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Stores the complete space-time history of a 1D cellular automaton.
 *
 * <p>Row 0 is the initial condition. Each subsequent row is computed by
 * applying the rule to the row above it. The history is unbounded forward
 * and fully rewindable (rows are simply removed when stepping back).
 *
 * <h2>Boundary Condition</h2>
 * <p>The grid is treated as <em>toroidal</em> by default — the left neighbor
 * of the leftmost cell is the rightmost cell, and vice versa. This makes the
 * rule purely local with no edge artifacts.
 */
public final class CAHistory {

    public enum InitCondition {
        SINGLE_CENTER       ("Single center cell"),
        TWO_CELLS           ("Two cells (center±10)"),
        ALTERNATING         ("Alternating 01010…"),
        RANDOM_SPARSE       ("Random (density ~0.1)"),
        RANDOM_MEDIUM       ("Random (density ~0.3)"),
        RANDOM_DENSE        ("Random (density ~0.5)"),
        ALL_ONE             ("All cells alive");

        public final String label;
        InitCondition(String label) { this.label = label; }
    }

    private final List<boolean[]> rows = new ArrayList<>();
    private final int width;
    private WolframRule rule;

    public CAHistory(int width, WolframRule rule, InitCondition init) {
        this.width = width;
        this.rule  = rule;
        rows.add(makeInitialRow(width, init, 0L));
    }

    public CAHistory(int width, WolframRule rule, InitCondition init, long seed) {
        this.width = width;
        this.rule  = rule;
        rows.add(makeInitialRow(width, init, seed));
    }

    /** Constructs from a custom initial row. */
    public CAHistory(boolean[] initialRow, WolframRule rule) {
        this.width = initialRow.length;
        this.rule  = rule;
        rows.add(initialRow.clone());
    }

    // -------------------------------------------------------------------------
    // Simulation
    // -------------------------------------------------------------------------

    /** Computes and appends the next generation. */
    public void step() {
        boolean[] prev = rows.get(rows.size() - 1);
        boolean[] next = new boolean[width];
        for (int i = 0; i < width; i++) {
            boolean l = prev[(i - 1 + width) % width];
            boolean c = prev[i];
            boolean r = prev[(i + 1) % width];
            next[i] = rule.apply(l, c, r);
        }
        rows.add(next);
    }

    /** Computes {@code n} additional generations. */
    public void step(int n) {
        for (int i = 0; i < n; i++) step();
    }

    /** Removes the most recent generation (undo). Does nothing if only one row remains. */
    public void stepBack() {
        if (rows.size() > 1) rows.remove(rows.size() - 1);
    }

    /** Removes all rows except the initial condition. */
    public void rewind() {
        while (rows.size() > 1) rows.remove(rows.size() - 1);
    }

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    public int width()          { return width; }
    public int generations()    { return rows.size(); }
    public WolframRule getRule() { return rule; }

    /** Returns row {@code gen} (0 = initial condition). */
    public boolean[] getRow(int gen) {
        return rows.get(gen).clone();
    }

    /** Returns all rows as a list (live reference, do not mutate). */
    public List<boolean[]> allRows() { return List.copyOf(rows); }

    public boolean canGoBack() { return rows.size() > 1; }

    /** Density of the last row (fraction of live cells). */
    public double currentDensity() {
        boolean[] last = rows.get(rows.size() - 1);
        int alive = 0;
        for (boolean b : last) if (b) alive++;
        return (double) alive / width;
    }

    // -------------------------------------------------------------------------
    // Rule / initial condition change
    // -------------------------------------------------------------------------

    public void setRule(WolframRule newRule) {
        this.rule = newRule;
    }

    /** Resets to a new initial condition with the current rule and width. */
    public void reset(InitCondition init, long seed) {
        rows.clear();
        rows.add(makeInitialRow(width, init, seed));
    }

    // -------------------------------------------------------------------------
    // Save/Load support
    // -------------------------------------------------------------------------

    /** Serializes the entire history to a text block. */
    public String toSaveString() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Wolfram 1D CA History\n");
        sb.append("rule: ").append(rule.number()).append("\n");
        sb.append("width: ").append(width).append("\n");
        sb.append("generations: ").append(rows.size()).append("\n");
        sb.append("rows:\n");
        for (boolean[] row : rows) {
            for (boolean b : row) sb.append(b ? '1' : '0');
            sb.append('\n');
        }
        return sb.toString();
    }

    /** Parses a save string. Returns null on error. */
    public static CAHistory fromSaveString(String text) {
        int ruleNum = 110, width = 200;
        List<boolean[]> parsedRows = new ArrayList<>();
        boolean inRows = false;
        for (String line : text.lines().toList()) {
            String t = line.trim();
            if (t.startsWith("#")) continue;
            if (t.equals("rows:")) { inRows = true; continue; }
            if (inRows) {
                boolean[] row = new boolean[t.length()];
                for (int i = 0; i < t.length(); i++) row[i] = t.charAt(i) == '1';
                parsedRows.add(row);
                continue;
            }
            int colon = t.indexOf(':');
            if (colon < 0) continue;
            String key = t.substring(0, colon).trim();
            String val = t.substring(colon + 1).trim();
            switch (key) {
                case "rule"  -> { try { ruleNum = Integer.parseInt(val); } catch (NumberFormatException e) {} }
                case "width" -> { try { width = Integer.parseInt(val); }   catch (NumberFormatException e) {} }
            }
        }
        if (parsedRows.isEmpty()) return null;
        CAHistory h = new CAHistory(parsedRows.get(0), new WolframRule(ruleNum));
        for (int i = 1; i < parsedRows.size(); i++) h.rows.add(parsedRows.get(i));
        return h;
    }

    // -------------------------------------------------------------------------
    // Initial condition generators
    // -------------------------------------------------------------------------

    private static boolean[] makeInitialRow(int width, InitCondition init, long seed) {
        boolean[] row = new boolean[width];
        int center = width / 2;
        switch (init) {
            case SINGLE_CENTER -> row[center] = true;
            case TWO_CELLS     -> { row[center - 10] = true; row[center + 10] = true; }
            case ALTERNATING   -> { for (int i = 0; i < width; i++) row[i] = (i % 2 == 0); }
            case ALL_ONE       -> { for (int i = 0; i < width; i++) row[i] = true; }
            case RANDOM_SPARSE -> fillRandom(row, 0.10, seed);
            case RANDOM_MEDIUM -> fillRandom(row, 0.30, seed);
            case RANDOM_DENSE  -> fillRandom(row, 0.50, seed);
        }
        return row;
    }

    private static void fillRandom(boolean[] row, double density, long seed) {
        Random rng = (seed == 0) ? new Random() : new Random(seed);
        for (int i = 0; i < row.length; i++) row[i] = rng.nextDouble() < density;
    }
}
