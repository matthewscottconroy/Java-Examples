package com.examples.format;

import com.examples.math.Quantity;
import com.examples.math.Unit;

import java.util.List;

/**
 * Renders unit conversions as formatted terminal output.
 *
 * <p>The main entry point is {@link #printTable}, which displays a source
 * quantity and a list of target conversions in a labeled box-drawing table:
 *
 * <pre>
 *   A marathon
 *   ┌──────────────────┬──────────────┐
 *   │ Unit             │ Value        │
 *   ├──────────────────┼──────────────┤
 *   │ miles            │ 26.2188      │
 *   │ kilometers       │ 42.1950      │
 *   │ meters           │ 42195.0000   │
 *   │ feet             │ 138,435.0000 │
 *   └──────────────────┴──────────────┘
 * </pre>
 */
public final class ConversionPrinter {

    // ANSI codes
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String CYAN   = "\u001B[96m";
    private static final String YELLOW = "\u001B[93m";
    private static final String GREEN  = "\u001B[92m";

    // Box-drawing characters
    private static final String TL = "\u250C"; // ┌
    private static final String TR = "\u2510"; // ┐
    private static final String BL = "\u2514"; // └
    private static final String BR = "\u2518"; // ┘
    private static final String H  = "\u2500"; // ─
    private static final String V  = "\u2502"; // │
    private static final String ML = "\u251C"; // ├
    private static final String MR = "\u2524"; // ┤
    private static final String TM = "\u252C"; // ┬
    private static final String BM = "\u2534"; // ┴
    private static final String CR = "\u253C"; // ┼

    private static final int UNIT_COL  = 18;
    private static final int VALUE_COL = 16;

    private ConversionPrinter() {}

    /**
     * Prints a labeled conversion table for the given source quantity and targets.
     *
     * <p>The source quantity's unit is listed first, highlighted in bold, followed
     * by each converted value in {@code targets}.
     *
     * @param label   a title displayed above the table, e.g., {@code "A marathon"}
     * @param source  the original quantity to convert from
     * @param targets the units to convert into (must be the same category as source)
     * @param <U>     the unit category
     */
    public static <U extends Unit> void printTable(
            String label,
            Quantity<U> source,
            List<U> targets) {

        System.out.println();
        System.out.println("  " + BOLD + CYAN + label + RESET);
        printTopBorder();
        printHeaderRow();
        printDivider();
        printRow(source.getUnit().fullName(), source.getValue(), true);
        for (U target : targets) {
            if (target.equals(source.getUnit())) continue;
            Quantity<U> converted = source.convertTo(target);
            printRow(converted.getUnit().fullName(), converted.getValue(), false);
        }
        printBottomBorder();
    }

    /**
     * Prints a simple section header.
     *
     * @param title the header text
     */
    public static void printHeader(String title) {
        String bar = "=".repeat(title.length() + 4);
        System.out.println();
        System.out.println(BOLD + YELLOW + "  " + bar + RESET);
        System.out.println(BOLD + YELLOW + "  | " + title + " |" + RESET);
        System.out.println(BOLD + YELLOW + "  " + bar + RESET);
    }

    // --- private helpers ---

    private static void printTopBorder() {
        System.out.println("  " + TL + H.repeat(UNIT_COL) + TM + H.repeat(VALUE_COL) + TR);
    }

    private static void printDivider() {
        System.out.println("  " + ML + H.repeat(UNIT_COL) + CR + H.repeat(VALUE_COL) + MR);
    }

    private static void printBottomBorder() {
        System.out.println("  " + BL + H.repeat(UNIT_COL) + BM + H.repeat(VALUE_COL) + BR);
    }

    private static void printHeaderRow() {
        String unitCol  = pad("Unit", UNIT_COL);
        String valueCol = pad("Value", VALUE_COL);
        System.out.println("  " + V + DIM + unitCol + RESET + V + DIM + valueCol + RESET + V);
    }

    private static void printRow(String unitName, double value, boolean isSource) {
        String color = isSource ? BOLD : GREEN;
        String unitCol  = pad(unitName, UNIT_COL);
        String valueCol = pad(String.format("%,.4f", value), VALUE_COL);
        System.out.println("  " + V + color + unitCol + RESET + V + color + valueCol + RESET + V);
    }

    private static String pad(String text, int width) {
        if (text.length() >= width) return text.substring(0, width);
        return " " + text + " ".repeat(width - text.length() - 1);
    }
}
