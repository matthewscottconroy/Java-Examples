package com.patterns.templatemethod;

import java.util.List;

/**
 * Demonstrates the Template Method pattern with a report generator.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Report Generator (Template Method Pattern) ===\n");

        List<String> headers = List.of("Employee", "Department", "Salary");
        List<List<String>> rows = List.of(
                List.of("Alice Chen",   "Engineering", "$95,000"),
                List.of("Bob Patel",    "Marketing",   "$78,000"),
                List.of("Carol James",  "Engineering", "$102,000")
        );

        ReportGenerator[] generators = {
                new CsvReportGenerator(),
                new HtmlReportGenerator(),
                new MarkdownReportGenerator()
        };

        for (ReportGenerator gen : generators) {
            System.out.println("--- " + gen.formatName() + " ---");
            System.out.println(gen.generate("Q1 Payroll", headers, rows));
        }
    }
}
