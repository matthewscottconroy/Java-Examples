package com.patterns.templatemethod;

import java.util.List;
import java.util.stream.Collectors;

/** Concrete generator — produces RFC 4180-style CSV. */
public class CsvReportGenerator extends ReportGenerator {

    @Override
    protected void writeHeader(StringBuilder sb, String title) {
        sb.append("# ").append(title).append("\n");
    }

    @Override
    protected void writeColumnHeaders(StringBuilder sb, List<String> headers) {
        sb.append(toCsvLine(headers)).append("\n");
    }

    @Override
    protected void writeRow(StringBuilder sb, List<String> row) {
        sb.append(toCsvLine(row)).append("\n");
    }

    @Override
    protected void writeFooter(StringBuilder sb, int rowCount) {
        sb.append("# ").append(rowCount).append(" record(s)\n");
    }

    @Override
    public String formatName() { return "CSV"; }

    private String toCsvLine(List<String> cells) {
        return cells.stream()
                .map(c -> c.contains(",") ? "\"" + c + "\"" : c)
                .collect(Collectors.joining(","));
    }
}
