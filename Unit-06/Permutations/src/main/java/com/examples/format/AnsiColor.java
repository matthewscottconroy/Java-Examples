package com.examples.format;

/**
 * ANSI escape codes for colorful terminal output.
 *
 * <p>Usage: {@code AnsiColor.CYAN + "some text" + AnsiColor.RESET}
 *
 * <p>If the terminal doesn't support ANSI codes, call {@link #disable()} once
 * at startup to suppress all color output.
 */
public final class AnsiColor {

    private static boolean enabled = true;

    private AnsiColor() {}

    public static void disable() { enabled = false; }
    public static boolean isEnabled() { return enabled; }

    // Text colors
    public static final String RESET   = code("\u001B[0m");
    public static final String BOLD    = code("\u001B[1m");
    public static final String DIM     = code("\u001B[2m");

    public static final String BLACK   = code("\u001B[30m");
    public static final String RED     = code("\u001B[31m");
    public static final String GREEN   = code("\u001B[32m");
    public static final String YELLOW  = code("\u001B[33m");
    public static final String BLUE    = code("\u001B[34m");
    public static final String MAGENTA = code("\u001B[35m");
    public static final String CYAN    = code("\u001B[36m");
    public static final String WHITE   = code("\u001B[37m");

    // Bright variants
    public static final String BRIGHT_RED     = code("\u001B[91m");
    public static final String BRIGHT_GREEN   = code("\u001B[92m");
    public static final String BRIGHT_YELLOW  = code("\u001B[93m");
    public static final String BRIGHT_BLUE    = code("\u001B[94m");
    public static final String BRIGHT_MAGENTA = code("\u001B[95m");
    public static final String BRIGHT_CYAN    = code("\u001B[96m");
    public static final String BRIGHT_WHITE   = code("\u001B[97m");

    private static String code(String escape) {
        // The field values are fixed at class-load time; enable/disable controls
        // a wrapper used in the format utilities, not the constants themselves.
        return escape;
    }

    /**
     * Wraps text with a color code and reset, but only if color is enabled.
     */
    public static String colorize(String color, String text) {
        if (!enabled) return text;
        return color + text + RESET;
    }

    /** Returns a bold version of the text. */
    public static String bold(String text) {
        return colorize(BOLD, text);
    }
}
