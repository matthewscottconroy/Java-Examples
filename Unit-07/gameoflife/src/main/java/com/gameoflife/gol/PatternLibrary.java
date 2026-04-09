package com.gameoflife.gol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Library of famous Game of Life patterns, grouped by category.
 *
 * <p>Categories:
 * <ul>
 *   <li>Still Lifes — patterns that never change</li>
 *   <li>Oscillators — patterns that return to their initial state after a fixed period</li>
 *   <li>Spaceships — patterns that translate across the grid</li>
 *   <li>Guns — patterns that emit spaceships indefinitely</li>
 *   <li>Methuselahs — small patterns with long, complex evolution before stabilizing</li>
 *   <li>Other — infinite growth, replicators, and curiosities</li>
 * </ul>
 */
public final class PatternLibrary {

    public static final String CAT_STILL     = "Still Lifes";
    public static final String CAT_OSC       = "Oscillators";
    public static final String CAT_SHIP      = "Spaceships";
    public static final String CAT_GUN       = "Guns";
    public static final String CAT_META      = "Methuselahs";
    public static final String CAT_OTHER     = "Other";

    private static final Map<String, List<Pattern>> BY_CATEGORY = new LinkedHashMap<>();
    private static final List<Pattern> ALL = new ArrayList<>();

    static {
        add(stillLife("Block",
            "2×2 square — the simplest still life.",
            new int[][]{{0,0},{0,1},{1,0},{1,1}}));

        add(stillLife("Beehive",
            "Six-cell oval still life. Very common in random soups.",
            new int[][]{{0,1},{0,2},{1,0},{1,3},{2,1},{2,2}}));

        add(stillLife("Loaf",
            "Seven-cell still life.",
            new int[][]{{0,1},{0,2},{1,0},{1,3},{2,1},{2,3},{3,2}}));

        add(stillLife("Boat",
            "Five-cell still life resembling a boat.",
            new int[][]{{0,0},{0,1},{1,0},{1,2},{2,1}}));

        add(stillLife("Tub",
            "Four-cell diamond still life.",
            new int[][]{{0,1},{1,0},{1,2},{2,1}}));

        add(osc("Blinker", "Period 2 — the most common oscillator in random soups.",
            new int[][]{{0,0},{0,1},{0,2}}));

        add(osc("Toad", "Period 2 — two offset rows of three cells.",
            new int[][]{{0,1},{0,2},{0,3},{1,0},{1,1},{1,2}}));

        add(osc("Beacon", "Period 2 — two touching diagonal blocks.",
            new int[][]{{0,0},{0,1},{1,0},{1,1},{2,2},{2,3},{3,2},{3,3}}));

        add(osc("Clock", "Period 2 — four cells arranged like clock hands.",
            new int[][]{{0,1},{1,3},{2,0},{3,2}}));

        add(osc("Pulsar", "Period 3 — the most common period-3 oscillator. Large and symmetric.",
            new int[][]{
                {0,2},{0,3},{0,4},{0,8},{0,9},{0,10},
                {2,0},{2,5},{2,7},{2,12},
                {3,0},{3,5},{3,7},{3,12},
                {4,0},{4,5},{4,7},{4,12},
                {5,2},{5,3},{5,4},{5,8},{5,9},{5,10},
                {7,2},{7,3},{7,4},{7,8},{7,9},{7,10},
                {8,0},{8,5},{8,7},{8,12},
                {9,0},{9,5},{9,7},{9,12},
                {10,0},{10,5},{10,7},{10,12},
                {12,2},{12,3},{12,4},{12,8},{12,9},{12,10}
            }));

        add(osc("Pentadecathlon", "Period 15 — a row of 10 cells oscillates with period 15.",
            new int[][]{
                {0,1},{1,1},{2,0},{2,2},{3,1},{4,1},{5,1},{6,1},{7,0},{7,2},{8,1},{9,1}
            }));

        add(osc("Figure Eight", "Period 8 — a symmetric figure-eight shaped oscillator.",
            new int[][]{
                {0,0},{0,1},{0,2},{1,0},{1,1},{1,2},{2,0},{2,1},{2,2},
                {3,3},{3,4},{3,5},{4,3},{4,4},{4,5},{5,3},{5,4},{5,5}
            }));

        add(ship("Glider",
            "Period 4 — the most famous pattern. Moves diagonally and was " +
            "the first discovered spaceship. Symbol of the hacker culture.",
            new int[][]{{0,1},{1,2},{2,0},{2,1},{2,2}}));

        add(ship("Lightweight Spaceship (LWSS)",
            "Period 4 horizontal spaceship. The smallest orthogonal spaceship.",
            new int[][]{{0,1},{0,4},{1,0},{2,0},{2,4},{3,0},{3,1},{3,2},{3,3}}));

        add(ship("Middleweight Spaceship (MWSS)",
            "Period 4 horizontal spaceship. One cell wider than the LWSS.",
            new int[][]{{0,2},{1,0},{1,4},{2,5},{3,0},{3,5},{4,1},{4,2},{4,3},{4,4},{4,5}}));

        add(ship("Heavyweight Spaceship (HWSS)",
            "Period 4 horizontal spaceship — the largest of the classic spaceships.",
            new int[][]{{0,2},{0,3},{1,0},{1,5},{2,6},{3,0},{3,6},{4,1},{4,2},{4,3},{4,4},{4,5},{4,6}}));

        add(gun("Gosper Glider Gun",
            "Discovered by Bill Gosper in 1970. The first pattern with unbounded growth. " +
            "Emits a glider every 30 generations.",
            new int[][]{
                {0,24},
                {1,22},{1,24},
                {2,12},{2,13},{2,20},{2,21},{2,34},{2,35},
                {3,11},{3,15},{3,20},{3,21},{3,34},{3,35},
                {4,0},{4,1},{4,10},{4,16},{4,20},{4,21},
                {5,0},{5,1},{5,10},{5,14},{5,16},{5,17},{5,22},{5,24},
                {6,10},{6,16},{6,24},
                {7,11},{7,15},
                {8,12},{8,13}
            }));

        add(gun("Simkin Glider Gun",
            "Discovered by Michael Simkin in 2015. The smallest known glider gun " +
            "(33 cells). Emits a glider every 120 generations.",
            new int[][]{
                {0,0},{0,1},{0,7},{0,8},
                {1,0},{1,1},{1,7},{1,8},
                {3,4},{3,5},
                {4,4},{4,5},
                {10,2},{10,3},
                {11,1},{11,5},{11,8},{11,9},
                {12,0},{12,6},{12,8},{12,9},
                {13,1},{13,5},{13,9},{13,10},
                {14,2},{14,3},
                {15,10},{15,11},
                {16,10},{16,11}
            }));

        add(meta("R-pentomino",
            "Only 5 cells but takes 1,103 generations to stabilize. " +
            "Produces 116 live cells (8 gliders, 4 LWSSes, many still lifes).",
            new int[][]{{0,1},{0,2},{1,0},{1,1},{2,1}}));

        add(meta("Diehard",
            "7 cells that completely disappear after 130 generations. " +
            "The longest-lived pattern for its cell count.",
            new int[][]{{0,6},{1,0},{1,1},{2,1},{2,5},{2,6},{2,7}}));

        add(meta("Acorn",
            "7 cells that take 5,206 generations to stabilize, producing " +
            "633 live cells including 13 escaped gliders.",
            new int[][]{{0,1},{1,3},{2,0},{2,1},{2,4},{2,5},{2,6}}));

        add(meta("Pi Heptomino",
            "7-cell pattern that stabilizes at generation 173 via two blocks and " +
            "a ship, with some escaped gliders.",
            new int[][]{{0,0},{0,1},{0,2},{1,0},{1,2},{2,0},{2,1},{2,2}}));

        add(meta("Thunderbird",
            "7-cell pattern that stabilizes at generation 243.",
            new int[][]{{0,0},{0,1},{0,2},{1,1},{2,1},{3,1},{4,1}}));

        add(other("Infinite Growth (3-cell seed)",
            "A 3-cell row with a specific neighbor arrangement triggers unbounded population growth.",
            new int[][]{{0,0},{0,1},{0,2},{1,2},{2,1}}));

        add(other("Blinker Fuse",
            "A row of blinkers that cascade outward from a fuse.",
            new int[][]{
                {0,0},{0,1},{0,2},
                {0,4},{0,5},{0,6},
                {0,8},{0,9},{0,10}
            }));

        add(other("Cross",
            "A simple cross shape — interesting chaotic evolution.",
            new int[][]{{0,2},{1,2},{2,0},{2,1},{2,2},{2,3},{2,4},{3,2},{4,2}}));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** All patterns in the library. */
    public static List<Pattern> all() { return Collections.unmodifiableList(ALL); }

    /** All category names in display order. */
    public static List<String> categories() {
        return new ArrayList<>(BY_CATEGORY.keySet());
    }

    /** All patterns in the given category. */
    public static List<Pattern> inCategory(String category) {
        return Collections.unmodifiableList(
            BY_CATEGORY.getOrDefault(category, List.of()));
    }

    /** Finds a pattern by name (case-insensitive), or null. */
    public static Pattern find(String name) {
        for (Pattern p : ALL)
            if (p.getName().equalsIgnoreCase(name)) return p;
        return null;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Pattern stillLife(String n, String d, int[][] c) {
        return new Pattern(n, CAT_STILL, d, c);
    }
    private static Pattern osc(String n, String d, int[][] c) {
        return new Pattern(n, CAT_OSC, d, c);
    }
    private static Pattern ship(String n, String d, int[][] c) {
        return new Pattern(n, CAT_SHIP, d, c);
    }
    private static Pattern gun(String n, String d, int[][] c) {
        return new Pattern(n, CAT_GUN, d, c);
    }
    private static Pattern meta(String n, String d, int[][] c) {
        return new Pattern(n, CAT_META, d, c);
    }
    private static Pattern other(String n, String d, int[][] c) {
        return new Pattern(n, CAT_OTHER, d, c);
    }

    private static void add(Pattern p) {
        ALL.add(p);
        BY_CATEGORY.computeIfAbsent(p.getCategory(), k -> new ArrayList<>()).add(p);
    }

    private PatternLibrary() {}
}
