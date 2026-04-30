package com.exam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Converts a list of {@link Question}s into a printable exam document.
 *
 * <p>Features:
 * <ul>
 *   <li>Version label ("Version A", "Version B", …) in the header
 *   <li>Total point count and per-question point values
 *   <li>Fenced code blocks (Markdown-compatible)
 *   <li>Blank answer space sized to the question type and code length
 *   <li>Optional answer key appended at the end
 * </ul>
 */
public class ExamFormatter {

    private static final int    WIDTH     = 72;
    private static final int    INDENT_W  = 3;
    private static final String INDENT    = " ".repeat(INDENT_W);
    private static final String HR_HEAVY  = "═".repeat(WIDTH);
    private static final String HR_LIGHT  = "─".repeat(WIDTH);

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    /** Format a single exam (no version label). */
    public static String format(List<Question> questions, boolean includeKey) {
        return format(questions, includeKey, null);
    }

    /** Format a single exam with an optional version label ("A", "B", …). */
    public static String format(List<Question> questions, boolean includeKey, String version) {
        StringBuilder sb = new StringBuilder();
        appendHeader(sb, questions, version);

        Map<QuestionType, List<Question>> byType = groupByType(questions);

        int[] num = {1};
        for (QuestionType type : QuestionType.values()) {
            List<Question> section = byType.get(type);
            if (section == null || section.isEmpty()) continue;
            appendSection(sb, type.label);
            for (Question q : section) appendQuestion(sb, num[0]++, q);
        }

        if (includeKey) {
            sb.append("\n").append(HR_HEAVY).append("\n");
            sb.append("  ANSWER KEY");
            if (version != null) sb.append(" — Version ").append(version);
            sb.append("\n").append(HR_HEAVY).append("\n\n");
            num[0] = 1;
            for (QuestionType type : QuestionType.values()) {
                List<Question> section = byType.get(type);
                if (section == null || section.isEmpty()) continue;
                for (Question q : section) {
                    sb.append(num[0]++).append(". [").append(q.topic()).append("]\n");
                    sb.append(wrap(q.key(), WIDTH - INDENT_W, INDENT)).append("\n\n");
                }
            }
        }

        return sb.toString();
    }

    // -----------------------------------------------------------------------
    // Header
    // -----------------------------------------------------------------------

    private static void appendHeader(StringBuilder sb,
                                     List<Question> questions,
                                     String version) {
        int totalPoints = questions.stream().mapToInt(Question::points).sum();

        sb.append(HR_HEAVY).append("\n");
        sb.append("  JAVA PROGRAMMING — FINAL EXAMINATION");
        if (version != null) sb.append("  [Version ").append(version).append("]");
        sb.append("\n");
        sb.append(HR_HEAVY).append("\n");
        sb.append("Name: ___________________________________  Date: ______________\n\n");
        sb.append("Total: ").append(totalPoints).append(" points")
          .append("   Questions: ").append(questions.size()).append("\n\n");
        sb.append("Instructions:\n");
        sb.append(INDENT).append("• Answer every question. Show all code and working.\n");
        sb.append(INDENT).append("• Write / Extend: your code must compile and run correctly.\n");
        sb.append(INDENT).append("• Debug: name each bug before writing the corrected version.\n");
        sb.append(INDENT).append("• Trace: write the exact console output, line by line.\n");
        sb.append(INDENT).append("• Design: labelled diagrams and pseudocode are welcome.\n");
        sb.append(HR_HEAVY).append("\n\n");
    }

    // -----------------------------------------------------------------------
    // Section heading
    // -----------------------------------------------------------------------

    private static void appendSection(StringBuilder sb, String label) {
        sb.append(HR_LIGHT).append("\n");
        sb.append("  Section: ").append(label).append("\n");
        sb.append(HR_LIGHT).append("\n\n");
    }

    // -----------------------------------------------------------------------
    // Individual question
    // -----------------------------------------------------------------------

    private static void appendQuestion(StringBuilder sb, int num, Question q) {
        // Header line: number, topic, unit, difficulty badge, points
        sb.append(num).append(". [")
          .append(q.topic()).append(" — Unit ").append(q.unit())
          .append("]  (").append(q.points()).append(" pts")
          .append(" / ").append(q.difficulty().name().charAt(0))
          .append(q.difficulty().name().substring(1).toLowerCase())
          .append(")\n");

        sb.append(wrap(q.prompt(), WIDTH - INDENT_W, INDENT)).append("\n");

        if (q.hasCode()) {
            sb.append("\n").append(INDENT).append("```java\n");
            for (String line : q.code().split("\n")) {
                sb.append(INDENT).append(line).append("\n");
            }
            sb.append(INDENT).append("```\n");
        }

        appendAnswerSpace(sb, q);
        sb.append("\n");
    }

    // -----------------------------------------------------------------------
    // Answer space
    // -----------------------------------------------------------------------

    /**
     * Appends blank lines proportional to how much writing the question needs.
     *
     * <ul>
     *   <li>TRACE  — output lines ≈ code lines + 4 (students write expected output)
     *   <li>DEBUG  — code lines + 10 (identify bugs, then write corrected version)
     *   <li>WRITE  — fixed 28 lines (full implementation from scratch)
     *   <li>EXTEND — code lines + 12 (extend existing code)
     *   <li>DESIGN — fixed 22 lines (prose + diagrams)
     * </ul>
     */
    private static void appendAnswerSpace(StringBuilder sb, Question q) {
        int codeLines = q.hasCode() ? q.code().split("\n").length : 0;
        int lines = switch (q.type()) {
            case TRACE  -> Math.max(6,  codeLines + 4);
            case DEBUG  -> Math.max(14, codeLines + 10);
            case WRITE  -> 28;
            case EXTEND -> Math.max(18, codeLines + 12);
            case DESIGN -> 22;
        };
        sb.append("\n").append(INDENT).append("[Answer — ~").append(lines).append(" lines]\n");
        sb.append("\n".repeat(lines));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static Map<QuestionType, List<Question>> groupByType(List<Question> questions) {
        return questions.stream().collect(Collectors.groupingBy(
            Question::type, LinkedHashMap::new, Collectors.toList()));
    }

    /** Word-wraps {@code text} to {@code width} chars, indenting continuation lines. */
    static String wrap(String text, int width, String indent) {
        String[] words = text.split(" ");
        StringBuilder sb = new StringBuilder();
        int lineLen = 0;
        boolean first = true;
        for (String word : words) {
            if (word.isEmpty()) continue;
            if (!first && lineLen + 1 + word.length() > width) {
                sb.append('\n').append(indent);
                lineLen = indent.length();
            } else if (!first) {
                sb.append(' ');
                lineLen++;
            }
            sb.append(word);
            lineLen += word.length();
            first = false;
        }
        return sb.toString();
    }
}
