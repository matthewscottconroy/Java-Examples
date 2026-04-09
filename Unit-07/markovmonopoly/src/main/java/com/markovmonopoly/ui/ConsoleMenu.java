package com.markovmonopoly.ui;

import java.io.PrintStream;
import java.util.*;

/**
 * A reusable console menu that presents numbered options and reads validated input.
 */
public final class ConsoleMenu {

    /** An entry in a menu. */
    public record MenuItem(String key, String description) {}

    private final String title;
    private final List<MenuItem> items;
    private final Scanner in;
    private final PrintStream out;

    public ConsoleMenu(String title, List<MenuItem> items, Scanner in, PrintStream out) {
        this.title = title;
        this.items = List.copyOf(items);
        this.in    = in;
        this.out   = out;
    }

    /**
     * Displays the menu and blocks until the user enters a valid key.
     *
     * @return the key of the selected item (lowercased)
     */
    public String prompt() {
        Set<String> validKeys = new LinkedHashSet<>();
        for (MenuItem item : items) validKeys.add(item.key().toLowerCase());

        while (true) {
            out.println();
            out.println(TableFormatter.sectionHeader(title));
            for (MenuItem item : items) {
                out.printf("  [%s] %s%n", item.key(), item.description());
            }
            out.print("\nEnter choice: ");

            String input = in.nextLine().trim().toLowerCase();
            if (validKeys.contains(input)) return input;
            out.println("  Invalid choice '" + input + "'. Options: " + validKeys);
        }
    }

    /**
     * Prompts for a free-form string. Returns the trimmed input; re-prompts if blank.
     */
    public String promptString(String message) {
        while (true) {
            out.print(message);
            String s = in.nextLine().trim();
            if (!s.isEmpty()) return s;
            out.println("  Input cannot be empty.");
        }
    }

    /**
     * Prompts for an optional string. Returns the trimmed input; empty string is allowed.
     */
    public String promptOptionalString(String message) {
        out.print(message);
        return in.nextLine().trim();
    }

    /**
     * Prompts for an integer in [min, max]. Re-prompts on bad input.
     */
    public int promptInt(String message, int min, int max) {
        while (true) {
            out.print(message);
            try {
                int v = Integer.parseInt(in.nextLine().trim());
                if (v >= min && v <= max) return v;
                out.printf("  Please enter a number between %d and %d.%n", min, max);
            } catch (NumberFormatException e) {
                out.println("  Please enter a valid integer.");
            }
        }
    }

    /**
     * Prompts for a double in [min, max]. Re-prompts on bad input.
     */
    public double promptDouble(String message, double min, double max) {
        while (true) {
            out.print(message);
            try {
                double v = Double.parseDouble(in.nextLine().trim());
                if (v >= min && v <= max) return v;
                out.printf("  Please enter a number between %.4f and %.4f.%n", min, max);
            } catch (NumberFormatException e) {
                out.println("  Please enter a valid decimal number.");
            }
        }
    }

    /**
     * Prompts for y/n confirmation.
     */
    public boolean promptYesNo(String message) {
        while (true) {
            out.print(message + " (y/n): ");
            String s = in.nextLine().trim().toLowerCase();
            if (s.equals("y") || s.equals("yes")) return true;
            if (s.equals("n") || s.equals("no")) return false;
            out.println("  Please enter 'y' or 'n'.");
        }
    }

    /** Pauses until the user presses Enter. */
    public void pressEnterToContinue() {
        out.print("\nPress Enter to continue...");
        in.nextLine();
    }
}
