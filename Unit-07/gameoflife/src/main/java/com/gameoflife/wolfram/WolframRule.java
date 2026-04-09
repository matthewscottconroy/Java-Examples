package com.gameoflife.wolfram;

/**
 * An elementary cellular automaton rule (Wolfram's classification, 0–255).
 *
 * <h2>How Rules Work</h2>
 * <p>Each cell's next state is determined by its left neighbor, itself, and its
 * right neighbor — three cells yielding 8 possible neighborhoods (2³ = 8).
 * The rule number (0–255) is an 8-bit integer whose bits encode the output
 * for each neighborhood, ordered from neighborhood 111 (= 7) down to 000 (= 0).
 *
 * <pre>
 *   Neighborhood:  111  110  101  100  011  010  001  000
 *   Rule 110 bits:   0    1    1    0    1    1    1    0  = 0b01101110 = 110
 * </pre>
 *
 * <h2>Famous Rules</h2>
 * <ul>
 *   <li>Rule 30  — chaotic; used in Mathematica's PRNG</li>
 *   <li>Rule 90  — Sierpiński triangle (additive / XOR)</li>
 *   <li>Rule 110 — Turing-complete (Cook, 2004)</li>
 *   <li>Rule 184 — traffic flow model</li>
 *   <li>Rule 150 — additive rule; Pascal's triangle mod 2</li>
 * </ul>
 */
public record WolframRule(int number) {

    public WolframRule {
        if (number < 0 || number > 255)
            throw new IllegalArgumentException("Rule number must be 0–255, got: " + number);
    }

    /**
     * Applies the rule to a three-cell neighborhood.
     *
     * @param left   left neighbor
     * @param center the cell itself
     * @param right  right neighbor
     * @return next state of center
     */
    public boolean apply(boolean left, boolean center, boolean right) {
        int index = (left ? 4 : 0) | (center ? 2 : 0) | (right ? 1 : 0);
        return (number & (1 << index)) != 0;
    }

    /**
     * Returns a multi-line ASCII representation of the rule table.
     *
     * <pre>
     * Rule 110:
     *  111  110  101  100  011  010  001  000
     *   █    █    █    ░    █    █    █    ░
     *   0    1    1    0    1    1    1    0
     * </pre>
     */
    public String ruleTable() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Rule %d  (binary: %s)%n", number,
            String.format("%8s", Integer.toBinaryString(number)).replace(' ', '0')));
        String[] labels = {"111","110","101","100","011","010","001","000"};
        StringBuilder top = new StringBuilder();
        StringBuilder mid = new StringBuilder();
        StringBuilder bot = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            boolean out = (number & (1 << i)) != 0;
            top.append(String.format(" %s ", labels[7 - i]));
            mid.append(String.format("  %s  ", out ? "█" : "░"));
            bot.append(String.format("  %d  ", out ? 1 : 0));
        }
        return sb.append(top).append("\n")
                  .append(mid).append("\n")
                  .append(bot).toString();
    }

    /**
     * Returns the Wolfram class (1–4) for well-known rules, or 0 if unknown.
     *
     * <ul>
     *   <li>Class 1 — uniform (all cells same after finite steps)</li>
     *   <li>Class 2 — periodic / stable structures</li>
     *   <li>Class 3 — chaotic / aperiodic (e.g., Rule 30)</li>
     *   <li>Class 4 — complex / localized structures (e.g., Rule 110)</li>
     * </ul>
     */
    public int wolframClass() {
        return switch (number) {
            case 0, 8, 32, 40, 128, 136, 160, 168 -> 1;          // uniform
            case 4, 12, 13, 50, 51, 58, 77, 78, 204 -> 2;       // periodic
            case 18, 22, 26, 30, 45, 86, 89, 101, 149 -> 3;     // chaotic
            case 54, 106, 110 -> 4;                               // complex
            default -> 0;                                          // unclassified
        };
    }

    @Override public String toString() { return "Rule " + number; }
}
