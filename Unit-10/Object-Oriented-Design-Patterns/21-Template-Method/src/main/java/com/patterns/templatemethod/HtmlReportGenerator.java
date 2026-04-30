package com.patterns.templatemethod;

import java.util.List;

/** Concrete generator — produces an HTML table. */
public class HtmlReportGenerator extends ReportGenerator {

    @Override
    protected void writeHeader(StringBuilder sb, String title) {
        sb.append("<!DOCTYPE html>\n<html>\n<head><title>")
          .append(title)
          .append("</title></head>\n<body>\n<h1>")
          .append(title)
          .append("</h1>\n<table border=\"1\">\n");
    }

    @Override
    protected void writeColumnHeaders(StringBuilder sb, List<String> headers) {
        sb.append("  <tr>");
        headers.forEach(h -> sb.append("<th>").append(h).append("</th>"));
        sb.append("</tr>\n");
    }

    @Override
    protected void writeRow(StringBuilder sb, List<String> row) {
        sb.append("  <tr>");
        row.forEach(cell -> sb.append("<td>").append(cell).append("</td>"));
        sb.append("</tr>\n");
    }

    @Override
    protected void writeFooter(StringBuilder sb, int rowCount) {
        sb.append("</table>\n<p>")
          .append(rowCount)
          .append(" record(s)</p>\n</body>\n</html>\n");
    }

    @Override
    public String formatName() { return "HTML"; }
}
