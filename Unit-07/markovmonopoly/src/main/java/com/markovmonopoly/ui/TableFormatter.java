package com.markovmonopoly.ui;

import java.util.List;

/**
 * Utilities for formatting tabular data, probability distributions, and
 * ASCII bar charts in the console.
 */
public final class TableFormatter {

    private TableFormatter() {}

    private static final int DEFAULT_BAR_WIDTH = 40;
    private static final char BAR_CHAR = '\u2588';  // full block ▓ → use █

    // -------------------------------------------------------------------------
    // Matrix formatting
    // -------------------------------------------------------------------------

    /**
     * Formats a square matrix with row and column headers.
     * Column labels are truncated to 8 characters for alignment.
     */
    public static String formatMatrix(double[][] matrix, List<String> labels) {
        int n = matrix.length;
        int labelWidth = maxLabelWidth(labels, 10);
        int colWidth = 8;

        StringBuilder sb = new StringBuilder();

        // Header row
        sb.append(padRight("", labelWidth)).append(" │");
        for (String label : labels) {
            sb.append(padLeft(truncate(label, colWidth), colWidth)).append(' ');
        }
        sb.append('\n');

        // Separator
        sb.append(repeat('─', labelWidth)).append("─┼");
        sb.append(repeat('─', (colWidth + 1) * n)).append('\n');

        // Data rows
        for (int i = 0; i < n; i++) {
            sb.append(padRight(truncate(labels.get(i), labelWidth), labelWidth)).append(" │");
            for (int j = 0; j < n; j++) {
                sb.append(padLeft(String.format("%.4f", matrix[i][j]), colWidth)).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Formats a (possibly non-square) matrix with separate row and column labels.
     */
    public static String formatMatrix(double[][] matrix,
                                       List<String> rowLabels, List<String> colLabels) {
        int rows = matrix.length;
        int cols = rows > 0 ? matrix[0].length : 0;
        int labelWidth = maxLabelWidth(rowLabels, 14);
        int colWidth = 8;

        StringBuilder sb = new StringBuilder();

        // Header
        sb.append(padRight("", labelWidth)).append(" │");
        for (String label : colLabels) {
            sb.append(padLeft(truncate(label, colWidth), colWidth)).append(' ');
        }
        sb.append('\n');

        // Separator
        sb.append(repeat('─', labelWidth)).append("─┼");
        sb.append(repeat('─', (colWidth + 1) * cols)).append('\n');

        // Rows
        for (int i = 0; i < rows; i++) {
            sb.append(padRight(truncate(rowLabels.get(i), labelWidth), labelWidth)).append(" │");
            for (int j = 0; j < cols; j++) {
                sb.append(padLeft(String.format("%.4f", matrix[i][j]), colWidth)).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Distribution formatting
    // -------------------------------------------------------------------------

    /**
     * Formats a probability distribution as a table with percentages and a bar chart.
     */
    public static String formatDistribution(double[] dist, List<String> labels) {
        return formatDistribution(dist, labels, DEFAULT_BAR_WIDTH);
    }

    /**
     * Formats a probability distribution as a table with percentages and a bar chart.
     */
    public static String formatDistribution(double[] dist, List<String> labels, int barWidth) {
        int labelWidth = maxLabelWidth(labels, 16);
        double max = 0;
        for (double v : dist) max = Math.max(max, v);

        StringBuilder sb = new StringBuilder();
        sb.append(padRight("State", labelWidth))
          .append("  Probability  Percentage  Distribution\n");
        sb.append(repeat('─', labelWidth + 2 + 12 + 10 + barWidth + 4)).append('\n');

        for (int i = 0; i < dist.length; i++) {
            sb.append(padRight(labels.get(i), labelWidth));
            sb.append(String.format("  %10.6f  %8.2f%%  ", dist[i], dist[i] * 100));
            int filled = (max > 0) ? (int) Math.round(dist[i] / max * barWidth) : 0;
            for (int b = 0; b < filled; b++) sb.append('█');
            sb.append('\n');
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Bar chart
    // -------------------------------------------------------------------------

    /**
     * Renders an ASCII horizontal bar chart.
     *
     * @param values   values to chart
     * @param labels   label for each bar
     * @param barWidth maximum bar width in characters
     */
    public static String asciiBarChart(double[] values, List<String> labels, int barWidth) {
        int labelWidth = maxLabelWidth(labels, 16);
        double max = 0;
        for (double v : values) max = Math.max(max, v);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            sb.append(padRight(labels.get(i), labelWidth)).append(" │ ");
            int filled = (max > 0) ? (int) Math.round(values[i] / max * barWidth) : 0;
            for (int b = 0; b < filled; b++) sb.append('█');
            sb.append(String.format(" %.4f\n", values[i]));
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Pads a string to exactly {@code width} characters with trailing spaces. */
    public static String padRight(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return s + repeat(' ', width - s.length());
    }

    /** Pads a string to exactly {@code width} characters with leading spaces. */
    public static String padLeft(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        return repeat(' ', width - s.length()) + s;
    }

    /** Truncates a string to at most {@code maxLen} characters. */
    public static String truncate(String s, int maxLen) {
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + "…";
    }

    /** Prints a horizontal rule of the given character repeated {@code n} times. */
    public static String repeat(char c, int n) {
        char[] chars = new char[n];
        java.util.Arrays.fill(chars, c);
        return new String(chars);
    }

    /** Returns a formatted section header. */
    public static String sectionHeader(String title) {
        String line = repeat('═', title.length() + 4);
        return line + '\n' + "║ " + title + " ║\n" + line + '\n';
    }

    /** Returns a formatted subsection header. */
    public static String subHeader(String title) {
        return "── " + title + " " + repeat('─', Math.max(0, 60 - title.length() - 4)) + '\n';
    }

    private static int maxLabelWidth(List<String> labels, int max) {
        int w = 4;
        for (String label : labels) w = Math.max(w, label.length());
        return Math.min(w, max);
    }
}
