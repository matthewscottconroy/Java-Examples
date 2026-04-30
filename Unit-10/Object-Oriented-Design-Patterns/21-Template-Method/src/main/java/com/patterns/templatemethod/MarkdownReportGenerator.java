package com.patterns.templatemethod;

import java.util.List;
import java.util.stream.Collectors;

/** Concrete generator — produces a GitHub-Flavored Markdown table. */
public class MarkdownReportGenerator extends ReportGenerator {

    @Override
    protected void writeHeader(StringBuilder sb, String title) {
        sb.append("# ").append(title).append("\n\n");
    }

    @Override
    protected void writeColumnHeaders(StringBuilder sb, List<String> headers) {
        sb.append("| ").append(String.join(" | ", headers)).append(" |\n");
        String separator = headers.stream().map(h -> "---").collect(Collectors.joining(" | "));
        sb.append("| ").append(separator).append(" |\n");
    }

    @Override
    protected void writeRow(StringBuilder sb, List<String> row) {
        sb.append("| ").append(String.join(" | ", row)).append(" |\n");
    }

    @Override
    public String formatName() { return "Markdown"; }
}
