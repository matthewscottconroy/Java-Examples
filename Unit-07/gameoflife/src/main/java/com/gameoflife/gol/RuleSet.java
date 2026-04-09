package com.gameoflife.gol;

import java.util.*;

/**
 * Birth/Survival rule specification in B/S notation.
 *
 * <p>A cell is born if it is dead and has a neighbor count in {@code birth}.
 * A cell survives if it is alive and has a neighbor count in {@code survival}.
 * All other cases result in (or remain) dead.
 *
 * <p>Examples:
 * <ul>
 *   <li>B3/S23 — Conway's Life (born with 3, survives with 2 or 3)</li>
 *   <li>B36/S23 — HighLife (also produces replicators)</li>
 *   <li>B3678/S34678 — Day &amp; Night (symmetric: dense/sparse behave similarly)</li>
 *   <li>B2/S — Seeds (every live cell dies; chaotic births)</li>
 *   <li>B1357/S1357 — Replicator</li>
 *   <li>B3/S12345 — Maze (creates winding corridors)</li>
 *   <li>B3/S012345678 — Life Without Death (monotone growth)</li>
 * </ul>
 */
public final class RuleSet {

    public static final RuleSet CONWAY           = parse("B3/S23");
    public static final RuleSet HIGH_LIFE        = parse("B36/S23");
    public static final RuleSet DAY_AND_NIGHT    = parse("B3678/S34678");
    public static final RuleSet SEEDS            = parse("B2/S");
    public static final RuleSet REPLICATOR       = parse("B1357/S1357");
    public static final RuleSet MAZE             = parse("B3/S12345");
    public static final RuleSet LIFE_WITHOUT_DEATH = parse("B3/S012345678");
    public static final RuleSet TWO_BY_TWO       = parse("B36/S125");
    public static final RuleSet ANNEAL           = parse("B4678/S35678");
    public static final RuleSet COAGULATIONS     = parse("B378/S235678");

    /** Named presets in display order. */
    public static final List<RuleSet> PRESETS = List.of(
        CONWAY, HIGH_LIFE, DAY_AND_NIGHT, SEEDS, REPLICATOR,
        MAZE, LIFE_WITHOUT_DEATH, TWO_BY_TWO, ANNEAL, COAGULATIONS
    );

    private final Set<Integer> birth;
    private final Set<Integer> survival;
    private final String name;

    private RuleSet(Set<Integer> birth, Set<Integer> survival, String name) {
        this.birth    = Collections.unmodifiableSet(new TreeSet<>(birth));
        this.survival = Collections.unmodifiableSet(new TreeSet<>(survival));
        this.name     = name;
    }

    // -------------------------------------------------------------------------
    // Core logic
    // -------------------------------------------------------------------------

    /** Returns true if a dead cell with {@code neighborCount} live neighbors is born. */
    public boolean born(int neighborCount) { return birth.contains(neighborCount); }

    /** Returns true if a live cell with {@code neighborCount} live neighbors survives. */
    public boolean survives(int neighborCount) { return survival.contains(neighborCount); }

    // -------------------------------------------------------------------------
    // Notation
    // -------------------------------------------------------------------------

    /** Returns the B/S notation string, e.g. "B3/S23". */
    public String toNotation() {
        StringBuilder sb = new StringBuilder("B");
        birth.forEach(n -> sb.append(n));
        sb.append("/S");
        survival.forEach(n -> sb.append(n));
        return sb.toString();
    }

    /** Display name (may differ from notation for named presets). */
    public String getName() { return name; }

    @Override
    public String toString() {
        return name.isEmpty() ? toNotation() : name + " (" + toNotation() + ")";
    }

    // -------------------------------------------------------------------------
    // Parsing
    // -------------------------------------------------------------------------

    /**
     * Parses a B/S notation string like "B3/S23".
     * The 'B' and 'S' prefixes are case-insensitive. The '/' separator is required.
     * Each digit after B or S represents a neighbor count.
     *
     * @throws IllegalArgumentException if the format is invalid
     */
    public static RuleSet fromNotation(String notation) {
        return parse(notation);
    }

    private static RuleSet parse(String notation) {
        return parseNamed(notation, "");
    }

    private static RuleSet parseNamed(String notation, String name) {
        String s = notation.trim().toUpperCase();
        int slash = s.indexOf('/');
        if (slash < 0 || !s.startsWith("B") || slash + 1 >= s.length()
                || s.charAt(slash + 1) != 'S') {
            throw new IllegalArgumentException("Invalid B/S notation: " + notation);
        }
        Set<Integer> birth    = parseDigits(s.substring(1, slash));
        Set<Integer> survival = parseDigits(s.substring(slash + 2));
        return new RuleSet(birth, survival, name.isEmpty() ? notation : name);
    }

    private static Set<Integer> parseDigits(String s) {
        Set<Integer> set = new TreeSet<>();
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '8') set.add(c - '0');
            else throw new IllegalArgumentException("Invalid neighbor count digit: " + c);
        }
        return set;
    }

    // Named preset builders (called during static field init)
    static {
        // Patch names after parse to avoid forward-reference
        try {
            java.lang.reflect.Field f = RuleSet.class.getDeclaredField("name");
            f.setAccessible(true);
        } catch (NoSuchFieldException ignored) {}
    }

    // Workaround: named version of each constant
    private static final Map<String, String> NOTATION_NAMES = Map.of(
        "B3/S23",           "Conway's Life",
        "B36/S23",          "HighLife",
        "B3678/S34678",     "Day & Night",
        "B2/S",             "Seeds",
        "B1357/S1357",      "Replicator",
        "B3/S12345",        "Maze",
        "B3/S012345678",    "Life Without Death",
        "B36/S125",         "2×2",
        "B4678/S35678",     "Anneal",
        "B378/S235678",     "Coagulations"
    );

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RuleSet other)) return false;
        return birth.equals(other.birth) && survival.equals(other.survival);
    }

    @Override
    public int hashCode() { return Objects.hash(birth, survival); }

    /** Returns the display name for a given notation, or the notation itself if not named. */
    public static String displayName(String notation) {
        return NOTATION_NAMES.getOrDefault(notation.toUpperCase(), notation);
    }
}
