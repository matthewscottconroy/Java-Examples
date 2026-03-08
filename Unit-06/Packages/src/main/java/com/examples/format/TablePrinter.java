package com.examples.format;

import java.util.List;

/**
 * Renders tabular data as a box-drawing ASCII table in the terminal.
 *
 * <pre>
 *   ┌──────────┬──────────┬──────────┐
 *   │  Slot 1  │  Slot 2  │  Slot 3  │
 *   ├──────────┼──────────┼──────────┤
 *   │  Alice   │   Bob    │  Carol   │
 *   └──────────┴──────────┴──────────┘
 * </pre>
 */
public final class TablePrinter {

    // Box-drawing characters
    private static final char TL = '\u250C'; // ┌
    private static final char TR = '\u2510'; // ┐
    private static final char BL = '\u2514'; // └
    private static final char BR = '\u2518'; // ┘
    private static final char H  = '\u2500'; // ─
    private static final char V  = '\u2502'; // │
    private static final char ML = '\u251C'; // ├
    private static final char MR = '\u2524'; // ┤
    private static final char TM = '\u252C'; // ┬
    private static final char BM = '\u2534'; // ┴
    private static final char CR = '\u253C'; // ┼

    private TablePrinter() {}

    /**
     * Prints a two-row table: a header row and a data row.
     *
     * @param headers    column header labels
     * @param values     row values (must be same length as headers)
     * @param cellWidth  minimum width of each cell (content is centered)
     * @param headerColor ANSI color for the header row (pass empty string for none)
     * @param valueColor  ANSI color for the value row
     */
    public static void printTwoRow(
            List<String> headers,
            List<String> values,
            int cellWidth,
            String headerColor,
            String valueColor) {

        if (headers.size() != values.size()) {
            throw new IllegalArgumentException("Header and value lists must have the same length.");
        }

        int cols = headers.size();
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            widths[i] = Math.max(cellWidth,
                    Math.max(headers.get(i).length(), values.get(i).length()) + 2);
        }

        printTopBorder(widths);
        printRow(headers, widths, headerColor);
        printMiddleBorder(widths);
        printRow(values, widths, valueColor);
        printBottomBorder(widths);
    }

    /**
     * Prints a single-row table (just a labeled value row, no header).
     *
     * @param values    the cells to display
     * @param cellWidth minimum cell width
     * @param color     ANSI color applied to all values
     */
    public static void printSingleRow(List<String> values, int cellWidth, String color) {
        int cols = values.size();
        int[] widths = new int[cols];
        for (int i = 0; i < cols; i++) {
            widths[i] = Math.max(cellWidth, values.get(i).length() + 2);
        }
        printTopBorder(widths);
        printRow(values, widths, color);
        printBottomBorder(widths);
    }

    // --- private helpers ---

    private static void printTopBorder(int[] widths) {
        System.out.print("  " + TL);
        for (int i = 0; i < widths.length; i++) {
            System.out.print(repeat(H, widths[i]));
            System.out.print(i < widths.length - 1 ? TM : TR);
        }
        System.out.println();
    }

    private static void printMiddleBorder(int[] widths) {
        System.out.print("  " + ML);
        for (int i = 0; i < widths.length; i++) {
            System.out.print(repeat(H, widths[i]));
            System.out.print(i < widths.length - 1 ? CR : MR);
        }
        System.out.println();
    }

    private static void printBottomBorder(int[] widths) {
        System.out.print("  " + BL);
        for (int i = 0; i < widths.length; i++) {
            System.out.print(repeat(H, widths[i]));
            System.out.print(i < widths.length - 1 ? BM : BR);
        }
        System.out.println();
    }

    private static void printRow(List<String> cells, int[] widths, String color) {
        System.out.print("  " + V);
        for (int i = 0; i < cells.size(); i++) {
            String content = color + center(cells.get(i), widths[i]) + AnsiColor.RESET;
            System.out.print(content + V);
        }
        System.out.println();
    }

    /** Centers a string within a field of the given width using spaces. */
    public static String center(String text, int width) {
        int padding = width - text.length();
        int left = padding / 2;
        int right = padding - left;
        return " ".repeat(left) + text + " ".repeat(right);
    }

    private static String repeat(char c, int times) {
        return String.valueOf(c).repeat(times);
    }
}
