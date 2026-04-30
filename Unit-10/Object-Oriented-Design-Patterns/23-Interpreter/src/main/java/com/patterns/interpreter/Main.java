package com.patterns.interpreter;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Demonstrates the Interpreter pattern with a boolean document search engine.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Boolean Search Engine (Interpreter Pattern) ===\n");

        InvertedIndex index = new InvertedIndex();
        index.addDocument("doc1", "java", "design", "patterns");
        index.addDocument("doc2", "java", "concurrency", "threads");
        index.addDocument("doc3", "python", "design", "patterns");
        index.addDocument("doc4", "rust", "concurrency", "safety");
        index.addDocument("doc5", "java", "safety", "design");

        // Query: java AND design
        SearchExpression q1 = new AndExpression(
                new TermExpression("java"),
                new TermExpression("design"));

        // Query: (java OR python) AND patterns
        SearchExpression q2 = new AndExpression(
                new OrExpression(new TermExpression("java"), new TermExpression("python")),
                new TermExpression("patterns"));

        // Query: java AND NOT concurrency
        SearchExpression q3 = new AndExpression(
                new TermExpression("java"),
                new NotExpression(new TermExpression("concurrency")));

        List.of(q1, q2, q3).forEach(q -> {
            Set<String> hits = new TreeSet<>(q.evaluate(index));
            System.out.printf("Query: %-40s → %s%n", q, hits);
        });
    }
}
