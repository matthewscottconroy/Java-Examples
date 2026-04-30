package com.exam;

/**
 * The five question formats on the exam.
 * Each type tests a different cognitive level:
 *   WRITE  — recall + synthesis (can you produce code from scratch?)
 *   DEBUG  — analysis (can you find and fix errors?)
 *   EXTEND — application (can you add to existing code?)
 *   TRACE  — comprehension (can you follow execution by hand?)
 *   DESIGN — evaluation (can you plan a solution at a high level?)
 */
public enum QuestionType {
    WRITE("Write a Program"),
    DEBUG("Debug a Program"),
    EXTEND("Extend a Program"),
    TRACE("Trace the Output"),
    DESIGN("Design a Program");

    public final String label;

    QuestionType(String label) { this.label = label; }
}
