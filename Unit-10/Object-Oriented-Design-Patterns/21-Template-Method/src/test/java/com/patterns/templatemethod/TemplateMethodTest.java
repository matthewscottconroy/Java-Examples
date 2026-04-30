package com.patterns.templatemethod;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemplateMethodTest {

    private static final List<String>       HEADERS = List.of("Name", "Score");
    private static final List<List<String>> ROWS    = List.of(
            List.of("Alice", "95"),
            List.of("Bob",   "87")
    );

    @Test
    @DisplayName("CSV output contains column headers as first non-comment line")
    void csvContainsHeaders() {
        String out = new CsvReportGenerator().generate("Test", HEADERS, ROWS);
        assertTrue(out.contains("Name,Score"), "CSV must contain header line");
    }

    @Test
    @DisplayName("CSV output contains one line per data row")
    void csvContainsRows() {
        String out = new CsvReportGenerator().generate("Test", HEADERS, ROWS);
        assertTrue(out.contains("Alice,95"));
        assertTrue(out.contains("Bob,87"));
    }

    @Test
    @DisplayName("HTML output wraps content in table tags")
    void htmlContainsTableTags() {
        String out = new HtmlReportGenerator().generate("Test", HEADERS, ROWS);
        assertTrue(out.contains("<table"));
        assertTrue(out.contains("</table>"));
    }

    @Test
    @DisplayName("HTML output includes title in h1 tag")
    void htmlContainsTitle() {
        String out = new HtmlReportGenerator().generate("My Report", HEADERS, ROWS);
        assertTrue(out.contains("<h1>My Report</h1>"));
    }

    @Test
    @DisplayName("Markdown output contains pipe-delimited header row")
    void markdownContainsPipeDelimitedHeaders() {
        String out = new MarkdownReportGenerator().generate("Test", HEADERS, ROWS);
        assertTrue(out.contains("| Name | Score |"));
    }

    @Test
    @DisplayName("Markdown output contains separator row with dashes")
    void markdownContainsSeparator() {
        String out = new MarkdownReportGenerator().generate("Test", HEADERS, ROWS);
        assertTrue(out.contains("| --- | --- |"));
    }

    @Test
    @DisplayName("All generators include every data row in their output")
    void allGeneratorsIncludeAllRows() {
        ReportGenerator[] gens = {
                new CsvReportGenerator(),
                new HtmlReportGenerator(),
                new MarkdownReportGenerator()
        };
        for (ReportGenerator gen : gens) {
            String out = gen.generate("T", HEADERS, ROWS);
            assertTrue(out.contains("Alice"), gen.formatName() + " missing Alice");
            assertTrue(out.contains("Bob"),   gen.formatName() + " missing Bob");
        }
    }

    @Test
    @DisplayName("CSV footer contains row count")
    void csvFooterContainsCount() {
        String out = new CsvReportGenerator().generate("T", HEADERS, ROWS);
        assertTrue(out.contains("2 record(s)"));
    }
}
