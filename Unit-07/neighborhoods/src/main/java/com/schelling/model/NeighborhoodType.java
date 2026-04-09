package com.schelling.model;

/**
 * The neighborhood topology used when deciding whether an agent is satisfied.
 *
 * <ul>
 *   <li>{@link #MOORE} — 8-connected (the standard Schelling neighborhood)</li>
 *   <li>{@link #VON_NEUMANN} — 4-connected (only cardinal directions)</li>
 *   <li>{@link #EXTENDED_MOORE} — all cells within a 2-cell radius (24 cells)</li>
 * </ul>
 */
public enum NeighborhoodType {

    MOORE("Moore (8)", new int[][]{
        {-1,-1},{-1,0},{-1,1},
        { 0,-1},       { 0,1},
        { 1,-1},{ 1,0},{ 1,1}
    }),

    VON_NEUMANN("Von Neumann (4)", new int[][]{
        {-1, 0},
        { 0,-1},{ 0,1},
        { 1, 0}
    }),

    EXTENDED_MOORE("Extended Moore (24)", buildExtended());

    // -------------------------------------------------------------------------

    private final String  displayName;
    private final int[][] offsets;

    NeighborhoodType(String displayName, int[][] offsets) {
        this.displayName = displayName;
        this.offsets     = offsets;
    }

    /** Returns the (dr, dc) offset pairs that define this neighborhood. */
    public int[][] offsets() { return offsets; }

    public String getDisplayName() { return displayName; }

    @Override
    public String toString() { return displayName; }

    // -------------------------------------------------------------------------

    private static int[][] buildExtended() {
        int[][] result = new int[24][2];
        int idx = 0;
        for (int dr = -2; dr <= 2; dr++)
            for (int dc = -2; dc <= 2; dc++)
                if (dr != 0 || dc != 0)
                    result[idx++] = new int[]{dr, dc};
        return result;
    }
}
