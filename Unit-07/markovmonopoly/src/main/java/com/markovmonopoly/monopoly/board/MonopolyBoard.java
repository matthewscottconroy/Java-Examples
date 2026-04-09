package com.markovmonopoly.monopoly.board;

import java.util.List;

/**
 * The standard Monopoly board: 40 physical spaces (indices 0–39) plus one virtual
 * state, {@code IN_JAIL} at index 40, used to distinguish "in jail" from "just visiting".
 *
 * <h2>Markov State Space</h2>
 * <p>The Markov chain derived from Monopoly uses <b>41 states</b>:
 * <ul>
 *   <li>States 0–39: physical board positions</li>
 *   <li>State 40: IN_JAIL (logically distinct from "Just Visiting" at position 10)</li>
 * </ul>
 * Space 30 (Go To Jail) is never a resting position — landing on it immediately
 * transitions to state 40.
 *
 * <h2>Known Result</h2>
 * <p>Empirically and theoretically, Illinois Ave (24), B&amp;O Railroad (25), and
 * Go (0) are among the most frequently visited spaces. Jail (state 40) is the single
 * most-visited state, which is why it merits its own state in the model.
 */
public final class MonopolyBoard {

    public static final int BOARD_SIZE          = 40;
    public static final int TOTAL_STATES        = 41;   // 40 spaces + IN_JAIL
    public static final int IN_JAIL_STATE       = 40;
    public static final int JAIL_POSITION       = 10;   // physical "Just Visiting" square
    public static final int GO_TO_JAIL_POSITION = 30;
    public static final int GO_POSITION         = 0;

    public static final int[] RAILROAD_POSITIONS = {5, 15, 25, 35};
    public static final int[] UTILITY_POSITIONS  = {12, 28};
    public static final int[] CHANCE_POSITIONS   = {7, 22, 36};
    public static final int[] COMMUNITY_CHEST_POSITIONS = {2, 17, 33};

    private final List<BoardSpace> spaces;

    private MonopolyBoard(List<BoardSpace> spaces) {
        this.spaces = List.copyOf(spaces);
    }

    /** Constructs the standard Monopoly board. */
    public static MonopolyBoard standard() {
        BoardSpace[] s = new BoardSpace[BOARD_SIZE + 1];  // +1 for IN_JAIL virtual state

        s[0]  = new BoardSpace(0,  "Go",                      SpaceType.GO);
        s[1]  = new BoardSpace(1,  "Mediterranean Ave",       SpaceType.PROPERTY,  "Purple",      60);
        s[2]  = new BoardSpace(2,  "Community Chest",         SpaceType.COMMUNITY_CHEST);
        s[3]  = new BoardSpace(3,  "Baltic Ave",              SpaceType.PROPERTY,  "Purple",      60);
        s[4]  = new BoardSpace(4,  "Income Tax",              SpaceType.TAX);
        s[5]  = new BoardSpace(5,  "Reading Railroad",        SpaceType.RAILROAD,  "",           200);
        s[6]  = new BoardSpace(6,  "Oriental Ave",            SpaceType.PROPERTY,  "LightBlue",  100);
        s[7]  = new BoardSpace(7,  "Chance",                  SpaceType.CHANCE);
        s[8]  = new BoardSpace(8,  "Vermont Ave",             SpaceType.PROPERTY,  "LightBlue",  100);
        s[9]  = new BoardSpace(9,  "Connecticut Ave",         SpaceType.PROPERTY,  "LightBlue",  120);
        s[10] = new BoardSpace(10, "Just Visiting / Jail",    SpaceType.JUST_VISITING);
        s[11] = new BoardSpace(11, "St. Charles Place",       SpaceType.PROPERTY,  "Pink",       140);
        s[12] = new BoardSpace(12, "Electric Company",        SpaceType.UTILITY,   "",           150);
        s[13] = new BoardSpace(13, "States Ave",              SpaceType.PROPERTY,  "Pink",       140);
        s[14] = new BoardSpace(14, "Virginia Ave",            SpaceType.PROPERTY,  "Pink",       160);
        s[15] = new BoardSpace(15, "Pennsylvania Railroad",   SpaceType.RAILROAD,  "",           200);
        s[16] = new BoardSpace(16, "St. James Place",         SpaceType.PROPERTY,  "Orange",     180);
        s[17] = new BoardSpace(17, "Community Chest",         SpaceType.COMMUNITY_CHEST);
        s[18] = new BoardSpace(18, "Tennessee Ave",           SpaceType.PROPERTY,  "Orange",     180);
        s[19] = new BoardSpace(19, "New York Ave",            SpaceType.PROPERTY,  "Orange",     200);
        s[20] = new BoardSpace(20, "Free Parking",            SpaceType.FREE_PARKING);
        s[21] = new BoardSpace(21, "Kentucky Ave",            SpaceType.PROPERTY,  "Red",        220);
        s[22] = new BoardSpace(22, "Chance",                  SpaceType.CHANCE);
        s[23] = new BoardSpace(23, "Indiana Ave",             SpaceType.PROPERTY,  "Red",        220);
        s[24] = new BoardSpace(24, "Illinois Ave",            SpaceType.PROPERTY,  "Red",        240);
        s[25] = new BoardSpace(25, "B&O Railroad",            SpaceType.RAILROAD,  "",           200);
        s[26] = new BoardSpace(26, "Atlantic Ave",            SpaceType.PROPERTY,  "Yellow",     260);
        s[27] = new BoardSpace(27, "Ventnor Ave",             SpaceType.PROPERTY,  "Yellow",     260);
        s[28] = new BoardSpace(28, "Water Works",             SpaceType.UTILITY,   "",           150);
        s[29] = new BoardSpace(29, "Marvin Gardens",          SpaceType.PROPERTY,  "Yellow",     280);
        s[30] = new BoardSpace(30, "Go To Jail",              SpaceType.GO_TO_JAIL);
        s[31] = new BoardSpace(31, "Pacific Ave",             SpaceType.PROPERTY,  "Green",      300);
        s[32] = new BoardSpace(32, "North Carolina Ave",      SpaceType.PROPERTY,  "Green",      300);
        s[33] = new BoardSpace(33, "Community Chest",         SpaceType.COMMUNITY_CHEST);
        s[34] = new BoardSpace(34, "Pennsylvania Ave",        SpaceType.PROPERTY,  "Green",      320);
        s[35] = new BoardSpace(35, "Short Line Railroad",     SpaceType.RAILROAD,  "",           200);
        s[36] = new BoardSpace(36, "Chance",                  SpaceType.CHANCE);
        s[37] = new BoardSpace(37, "Park Place",              SpaceType.PROPERTY,  "DarkBlue",   350);
        s[38] = new BoardSpace(38, "Luxury Tax",              SpaceType.TAX);
        s[39] = new BoardSpace(39, "Boardwalk",               SpaceType.PROPERTY,  "DarkBlue",   400);
        s[40] = new BoardSpace(40, "In Jail",                 SpaceType.IN_JAIL);

        return new MonopolyBoard(List.of(s));
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** Returns the space at the given index (0–40). */
    public BoardSpace getSpace(int index) {
        return spaces.get(index);
    }

    /** Returns the display name of the space at the given index. */
    public String getLabel(int index) {
        return spaces.get(index).name();
    }

    /** Returns the type of the space at the given index. */
    public SpaceType getType(int index) {
        return spaces.get(index).type();
    }

    /** Returns all 41 spaces (indices 0–40) as an unmodifiable list. */
    public List<BoardSpace> getAllSpaces() {
        return spaces;
    }

    // -------------------------------------------------------------------------
    // Navigation helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the board position after moving {@code steps} spaces forward from
     * {@code currentPosition}, wrapping around the board.
     */
    public int advance(int currentPosition, int steps) {
        return (currentPosition + steps) % BOARD_SIZE;
    }

    /**
     * Returns the nearest railroad position in the forward (clockwise) direction
     * from {@code currentPosition}.
     *
     * <p>Railroads are at positions 5 (Reading), 15 (Pennsylvania), 25 (B&amp;O),
     * 35 (Short Line). From Chance #1 (7) → 15; from Chance #2 (22) → 25;
     * from Chance #3 (36) → 5 (wraps around).
     */
    public int nearestRailroad(int currentPosition) {
        for (int offset = 1; offset <= BOARD_SIZE; offset++) {
            int candidate = (currentPosition + offset) % BOARD_SIZE;
            for (int rr : RAILROAD_POSITIONS) {
                if (candidate == rr) return rr;
            }
        }
        throw new IllegalStateException("No railroad found — board is misconfigured.");
    }

    /**
     * Returns the nearest utility position in the forward (clockwise) direction
     * from {@code currentPosition}.
     *
     * <p>Utilities are at 12 (Electric Company) and 28 (Water Works).
     * From Chance #1 (7) → 12; from Chance #2 (22) → 28; from Chance #3 (36) → 12.
     */
    public int nearestUtility(int currentPosition) {
        for (int offset = 1; offset <= BOARD_SIZE; offset++) {
            int candidate = (currentPosition + offset) % BOARD_SIZE;
            for (int u : UTILITY_POSITIONS) {
                if (candidate == u) return u;
            }
        }
        throw new IllegalStateException("No utility found — board is misconfigured.");
    }

    /**
     * Returns a short, fixed-width label suitable for Markov chain state naming.
     * Includes the index prefix for clarity.
     */
    public String shortLabel(int index) {
        String name = spaces.get(index).name();
        // Abbreviate common long names
        name = name.replace("Community Chest", "Comm.Chest")
                   .replace("Just Visiting / Jail", "Just Visiting")
                   .replace("Railroad", "RR")
                   .replace("Avenue", "Ave");
        return String.format("%2d:%-18s", index, name);
    }
}
