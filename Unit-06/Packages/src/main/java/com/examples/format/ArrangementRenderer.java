package com.examples.format;

import com.examples.math.Arrangement;
import com.examples.math.Permutation;
import com.examples.math.StructureMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders arrangements, permutations, and structure maps as visual terminal output.
 *
 * <p>This class handles all the "show me what's happening" work so that
 * {@code Main} can stay focused on the math story.
 */
public final class ArrangementRenderer {

    private static final int DEFAULT_CELL_WIDTH = 8;

    private ArrangementRenderer() {}

    /** Prints a labeled section header. */
    public static void printHeader(String title) {
        String bar = "=".repeat(title.length() + 4);
        System.out.println();
        System.out.println(AnsiColor.BOLD + AnsiColor.BRIGHT_CYAN + "  " + bar + AnsiColor.RESET);
        System.out.println(AnsiColor.BOLD + AnsiColor.BRIGHT_CYAN + "  | " + title + " |" + AnsiColor.RESET);
        System.out.println(AnsiColor.BOLD + AnsiColor.BRIGHT_CYAN + "  " + bar + AnsiColor.RESET);
        System.out.println();
    }

    /** Prints a sub-section title in a quieter style. */
    public static void printSubHeader(String title) {
        System.out.println();
        System.out.println(AnsiColor.BOLD + AnsiColor.YELLOW + "  -- " + title + " --" + AnsiColor.RESET);
    }

    /**
     * Renders an arrangement as a labeled slot table.
     *
     * <pre>
     *   ┌──────────┬──────────┬──────────┬──────────┐
     *   │  Spot 1  │  Spot 2  │  Spot 3  │  Spot 4  │
     *   ├──────────┼──────────┼──────────┼──────────┤
     *   │  Alice   │   Bob    │  Carol   │   Dave   │
     *   └──────────┴──────────┴──────────┴──────────┘
     * </pre>
     *
     * @param label      a short description printed above the table
     * @param arrangement the arrangement to display
     * @param slotLabel   prefix for slot headers (e.g., "Spot")
     */
    public static <T> void renderArrangement(
            String label, Arrangement<T> arrangement, String slotLabel) {

        if (label != null && !label.isEmpty()) {
            System.out.println("  " + AnsiColor.DIM + label + AnsiColor.RESET);
        }

        List<String> headers = new ArrayList<>();
        List<String> values  = new ArrayList<>();
        for (int i = 0; i < arrangement.size(); i++) {
            headers.add(slotLabel + " " + (i + 1));
            values.add(arrangement.get(i).toString());
        }

        TablePrinter.printTwoRow(
                headers, values,
                DEFAULT_CELL_WIDTH,
                AnsiColor.DIM,
                AnsiColor.BRIGHT_WHITE);
    }

    /**
     * Renders a permutation as an arrow diagram showing where each
     * position draws its element from.
     *
     * <pre>
     *   Permutation [2, 0, 3, 1]:
     *
     *     pos 0 draws from index 2
     *     pos 1 draws from index 0
     *     pos 2 draws from index 3
     *     pos 3 draws from index 1
     *
     *   Cycle notation: (0 2 3 1)    Order: 4
     * </pre>
     */
    public static void renderPermutation(String label, Permutation p) {
        if (label != null && !label.isEmpty()) {
            System.out.println("  " + AnsiColor.BOLD + label + AnsiColor.RESET);
        }
        System.out.println("  " + AnsiColor.DIM + "Mapping: " + p + AnsiColor.RESET);
        System.out.println("  " + AnsiColor.DIM + "Cycles:  " + AnsiColor.RESET
                + AnsiColor.MAGENTA + p.toCycleNotation() + AnsiColor.RESET);
        System.out.println("  " + AnsiColor.DIM + "Order:   " + AnsiColor.RESET
                + AnsiColor.GREEN + p.order() + AnsiColor.RESET);
    }

    /**
     * Shows a before → after transformation under a permutation.
     *
     * <pre>
     *   Before:  [Alice, Bob, Carol, Dave]
     *   Permute: [2, 0, 3, 1]  (0 2 3 1)
     *   After:   [Carol, Alice, Dave, Bob]
     * </pre>
     */
    public static <T> void renderTransformation(
            String label,
            Arrangement<T> before,
            Permutation p,
            Arrangement<T> after) {

        System.out.println("  " + AnsiColor.BOLD + label + AnsiColor.RESET);
        System.out.print("  Before:  ");
        System.out.println(AnsiColor.BRIGHT_WHITE + before.toList() + AnsiColor.RESET);
        System.out.print("  Permute: ");
        System.out.println(AnsiColor.BRIGHT_YELLOW + p + "  " + p.toCycleNotation() + AnsiColor.RESET);
        System.out.print("  After:   ");
        System.out.println(AnsiColor.BRIGHT_GREEN + after.toList() + AnsiColor.RESET);
    }

    /**
     * Visually demonstrates the isomorphism property of a structure map for a
     * given permutation. Shows both sides of the equation and whether they match.
     *
     * <pre>
     *   Isomorphism check for permutation (0 1):
     *
     *   Left:  permute first, then relabel
     *     [Alice, Bob, Carol]  --permute-->  [Bob, Alice, Carol]  --label-->  [2, 1, 3]
     *
     *   Right: relabel first, then permute
     *     [Alice, Bob, Carol]  --label-->   [1, 2, 3]  --permute-->  [2, 1, 3]
     *
     *   Equal? YES
     * </pre>
     */
    public static <T, U> void renderIsomorphismCheck(
            String label,
            StructureMap<T, U> map,
            Permutation p) {

        System.out.println("  " + AnsiColor.BOLD + label + AnsiColor.RESET);
        System.out.println("  " + AnsiColor.DIM + "Permutation: " + p.toCycleNotation() + AnsiColor.RESET);

        Arrangement<T> source    = map.getSource();
        Arrangement<T> permuted  = source.permute(p);
        Arrangement<U> leftSide  = map.apply(permuted);
        Arrangement<U> mapped    = map.applyToSource();
        Arrangement<U> rightSide = mapped.permute(p);

        System.out.println("  " + AnsiColor.CYAN + "Left  (permute first, then relabel): "
                + source.toList() + " -> " + permuted.toList() + " -> " + leftSide.toList()
                + AnsiColor.RESET);
        System.out.println("  " + AnsiColor.MAGENTA + "Right (relabel first, then permute): "
                + source.toList() + " -> " + mapped.toList() + " -> " + rightSide.toList()
                + AnsiColor.RESET);

        boolean match = leftSide.equals(rightSide);
        String verdict = match
                ? AnsiColor.BRIGHT_GREEN + "YES — structure preserved!" + AnsiColor.RESET
                : AnsiColor.BRIGHT_RED   + "NO  — something went wrong." + AnsiColor.RESET;
        System.out.println("  Equal? " + verdict);
    }

    /** Prints an informational line with a bullet point. */
    public static void info(String message) {
        System.out.println("  " + AnsiColor.DIM + ">" + AnsiColor.RESET + " " + message);
    }

    /** Prints a result/conclusion line in green. */
    public static void result(String message) {
        System.out.println("  " + AnsiColor.BRIGHT_GREEN + message + AnsiColor.RESET);
    }

    /** Prints a plain line. */
    public static void line(String message) {
        System.out.println("  " + message);
    }
}
