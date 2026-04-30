package com.exam;

/**
 * One exam question.
 *
 * @param type       the question format (write / debug / extend / trace / design)
 * @param topic      short topic label shown in the exam header, e.g. "Generics"
 * @param unit       unit number where this concept is taught, e.g. "09"
 * @param difficulty EASY / MEDIUM / HARD, which also determines point value
 * @param prompt     the question text shown to students
 * @param code       code snippet (non-empty for debug / extend / trace questions)
 * @param key        answer or marking notes (only printed when --key flag is set)
 */
public record Question(
        QuestionType type,
        String topic,
        String unit,
        Difficulty difficulty,
        String prompt,
        String code,
        String key) {

    /** True when a code block should be displayed below the prompt. */
    public boolean hasCode() { return !code.isBlank(); }

    /** Point value for this question, derived from its difficulty tier. */
    public int points() { return difficulty.points; }
}
