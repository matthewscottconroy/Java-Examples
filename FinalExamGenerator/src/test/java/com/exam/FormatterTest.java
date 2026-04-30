package com.exam;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FormatterTest {

    private static final List<Question> SAMPLE = List.of(
        new Question(QuestionType.WRITE,  "Arrays",    "01", Difficulty.EASY,
            "Write a method that reverses an array.", "",
            "Use two pointers."),
        new Question(QuestionType.DEBUG,  "Loops",     "02", Difficulty.MEDIUM,
            "Fix the off-by-one error.",
            "for (int i = 0; i <= arr.length; i++) {}",
            "Change <= to <."),
        new Question(QuestionType.EXTEND, "Classes",   "03", Difficulty.HARD,
            "Extend this class to add a toString().",
            "public class Point { int x, y; }",
            "Override toString() returning \"(\" + x + \",\" + y + \")\"."),
        new Question(QuestionType.TRACE,  "Recursion", "04", Difficulty.EASY,
            "Trace the output of this recursive method.",
            "static int f(int n) { return n < 2 ? n : f(n-1)+f(n-2); }",
            "f(4) prints 3."),
        new Question(QuestionType.DESIGN, "OOP",       "05", Difficulty.MEDIUM,
            "Design a class hierarchy for a zoo.", "",
            "Animal base class with speak(); Dog and Cat subclasses.")
    );

    @Test
    void headerContainsTotalPoints() {
        String output = ExamFormatter.format(SAMPLE, false);
        int expectedTotal = SAMPLE.stream().mapToInt(Question::points).sum();
        assertTrue(output.contains(expectedTotal + " points"),
            "Header should show total points");
    }

    @Test
    void headerContainsQuestionCount() {
        String output = ExamFormatter.format(SAMPLE, false);
        assertTrue(output.contains("Questions: " + SAMPLE.size()),
            "Header should show question count");
    }

    @Test
    void versionLabelAppearsInHeader() {
        String output = ExamFormatter.format(SAMPLE, false, "B");
        assertTrue(output.contains("Version B"), "Version label should appear in header");
    }

    @Test
    void noVersionLabel_whenNull() {
        String output = ExamFormatter.format(SAMPLE, false, null);
        assertFalse(output.contains("Version"), "No version label when null");
    }

    @Test
    void answerSpacePresent() {
        String output = ExamFormatter.format(SAMPLE, false);
        assertTrue(output.contains("[Answer — ~"), "Answer space marker must be present");
    }

    @Test
    void answerKeyIncluded_whenRequested() {
        String output = ExamFormatter.format(SAMPLE, true);
        assertTrue(output.contains("ANSWER KEY"), "Answer key section should be present");
        assertTrue(output.contains("Use two pointers"), "Key text should appear");
    }

    @Test
    void answerKeyAbsent_whenNotRequested() {
        String output = ExamFormatter.format(SAMPLE, false);
        assertFalse(output.contains("ANSWER KEY"), "Answer key must not appear when not requested");
    }

    @Test
    void answerKeyVersionLabel() {
        String output = ExamFormatter.format(SAMPLE, true, "C");
        assertTrue(output.contains("ANSWER KEY — Version C"),
            "Answer key should show version label");
    }

    @Test
    void codeBlockRendered() {
        String output = ExamFormatter.format(SAMPLE, false);
        assertTrue(output.contains("```java"), "Fenced code block should appear for questions with code");
    }

    @Test
    void difficultyAndPointsShownPerQuestion() {
        String output = ExamFormatter.format(SAMPLE, false);
        assertTrue(output.contains("pts"), "Point value should appear next to each question");
        assertTrue(output.contains("Easy") || output.contains("Medium") || output.contains("Hard"),
            "Difficulty label should appear next to each question");
    }

    @Test
    void wrap_shortText_unchanged() {
        String result = ExamFormatter.wrap("hello world", 80, "   ");
        assertEquals("hello world", result);
    }

    @Test
    void wrap_longText_breaksAtWordBoundary() {
        String text = "one two three four five six seven eight nine ten eleven twelve";
        String result = ExamFormatter.wrap(text, 20, "  ");
        String[] lines = result.split("\n");
        assertTrue(lines.length > 1, "Long text should be wrapped");
        for (String line : lines) {
            assertTrue(line.length() <= 22, "No line should exceed width + indent");
        }
    }
}
