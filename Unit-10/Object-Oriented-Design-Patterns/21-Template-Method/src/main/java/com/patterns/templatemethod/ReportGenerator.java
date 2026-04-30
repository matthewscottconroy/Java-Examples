package com.patterns.templatemethod;

import java.util.List;

/**
 * Abstract class defining the report-generation template.
 *
 * <p>The {@link #generate} method is the <em>template method</em> — it fixes
 * the sequence of steps. Subclasses override the hook methods to supply
 * format-specific behaviour without changing the overall algorithm.
 */
public abstract class ReportGenerator {

    /**
     * Template method — the invariant skeleton.
     * Subclasses must not override this method.
     *
     * @param title   report title
     * @param headers column headers
     * @param rows    data rows (each inner list is one row)
     * @return completed report as a String
     */
    public final String generate(String title, List<String> headers, List<List<String>> rows) {
        StringBuilder sb = new StringBuilder();
        writeHeader(sb, title);
        writeColumnHeaders(sb, headers);
        for (List<String> row : rows) {
            writeRow(sb, row);
        }
        writeFooter(sb, rows.size());
        return sb.toString();
    }

    /** Write the document-level header (title, file preamble, etc.). */
    protected abstract void writeHeader(StringBuilder sb, String title);

    /** Write the column header row. */
    protected abstract void writeColumnHeaders(StringBuilder sb, List<String> headers);

    /** Write a single data row. */
    protected abstract void writeRow(StringBuilder sb, List<String> row);

    /**
     * Write the document footer.
     *
     * <p>Default implementation does nothing — subclasses may override.
     *
     * @param sb       output buffer
     * @param rowCount number of data rows written
     */
    protected void writeFooter(StringBuilder sb, int rowCount) { /* optional hook */ }

    /** Human-readable format name, used in file names and UI labels. */
    public abstract String formatName();
}
