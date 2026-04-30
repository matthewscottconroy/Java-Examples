package com.patterns.interpreter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private InvertedIndex index;

    @BeforeEach
    void setUp() {
        index = new InvertedIndex();
        index.addDocument("d1", "java", "patterns");
        index.addDocument("d2", "java", "concurrency");
        index.addDocument("d3", "python", "patterns");
        index.addDocument("d4", "rust", "safety");
    }

    @Test
    @DisplayName("TermExpression returns documents containing that term")
    void termLookup() {
        Set<String> result = new TermExpression("java").evaluate(index);
        assertEquals(Set.of("d1", "d2"), result);
    }

    @Test
    @DisplayName("TermExpression is case-insensitive")
    void termCaseInsensitive() {
        Set<String> result = new TermExpression("JAVA").evaluate(index);
        assertEquals(Set.of("d1", "d2"), result);
    }

    @Test
    @DisplayName("AndExpression returns intersection")
    void andIntersection() {
        SearchExpression expr = new AndExpression(
                new TermExpression("java"),
                new TermExpression("patterns"));
        assertEquals(Set.of("d1"), expr.evaluate(index));
    }

    @Test
    @DisplayName("OrExpression returns union")
    void orUnion() {
        SearchExpression expr = new OrExpression(
                new TermExpression("java"),
                new TermExpression("python"));
        assertEquals(Set.of("d1", "d2", "d3"), expr.evaluate(index));
    }

    @Test
    @DisplayName("NotExpression returns complement")
    void notComplement() {
        SearchExpression expr = new NotExpression(new TermExpression("java"));
        Set<String> result = expr.evaluate(index);
        assertTrue(result.contains("d3"));
        assertTrue(result.contains("d4"));
        assertFalse(result.contains("d1"));
        assertFalse(result.contains("d2"));
    }

    @Test
    @DisplayName("Nested expressions compose correctly: java AND NOT concurrency")
    void nestedExpression() {
        SearchExpression expr = new AndExpression(
                new TermExpression("java"),
                new NotExpression(new TermExpression("concurrency")));
        assertEquals(Set.of("d1"), expr.evaluate(index));
    }

    @Test
    @DisplayName("Unknown term returns empty set")
    void unknownTermReturnsEmpty() {
        Set<String> result = new TermExpression("cobol").evaluate(index);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("toString produces human-readable query string")
    void toStringIsReadable() {
        SearchExpression expr = new AndExpression(
                new TermExpression("java"),
                new TermExpression("patterns"));
        assertEquals("(java AND patterns)", expr.toString());
    }
}
