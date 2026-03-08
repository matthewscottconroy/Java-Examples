package com.examples.hello;

/**
 * Reusable string utilities — the public API of this library.
 *
 * <p>A library has no {@code main} method.  It is compiled into a JAR and
 * consumed by other projects as a {@code <dependency>}.  This class represents
 * the kind of general-purpose utility code that makes sense to package once
 * and reuse everywhere.
 */
public class StringUtils {

    // Private constructor prevents instantiation — all methods are static utilities.
    private StringUtils() {}

    /**
     * Repeats {@code text} exactly {@code times} times.
     *
     * @param text  the string to repeat
     * @param times number of repetitions; must be &gt;= 0
     * @return the repeated string, or an empty string if {@code times} is 0
     * @throws IllegalArgumentException if {@code times} is negative
     */
    public static String repeat(String text, int times) {
        if (times < 0) {
            throw new IllegalArgumentException("times must be >= 0, got: " + times);
        }
        return text.repeat(times);
    }

    /**
     * Returns {@code true} if {@code text} reads the same forwards and backwards,
     * ignoring case and non-letter characters.
     *
     * @param text the string to test
     * @return {@code true} if {@code text} is a palindrome
     */
    public static boolean isPalindrome(String text) {
        String cleaned = text.toLowerCase().replaceAll("[^a-z0-9]", "");
        String reversed = new StringBuilder(cleaned).reverse().toString();
        return cleaned.equals(reversed);
    }

    /**
     * Capitalises the first letter of each word in {@code text}.
     *
     * @param text the input string
     * @return the title-cased result
     */
    public static String toTitleCase(String text) {
        if (text == null || text.isBlank()) return text;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0)));
            sb.append(word.substring(1).toLowerCase());
        }
        return sb.toString();
    }
}
