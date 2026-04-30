package com.patterns.flyweight;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the Flyweight pattern in a word-processor scenario.
 *
 * <p>A large document has many characters. Most share the same font style.
 * Only three distinct {@link FontStyle} objects are created, regardless of how
 * many characters use those styles.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== Word Processor Characters (Flyweight Pattern) ===\n");

        FontStyleFactory factory = new FontStyleFactory();

        System.out.println("Creating font styles (factory output shows cache misses):");
        FontStyle bodyStyle    = factory.getStyle("Times New Roman", 12, false, false);
        FontStyle headingStyle = factory.getStyle("Arial", 18, true, false);
        FontStyle emphStyle    = factory.getStyle("Times New Roman", 12, false, true);

        // Request the same styles again — factory returns cached objects, no output
        FontStyle bodyStyle2 = factory.getStyle("Times New Roman", 12, false, false);
        System.out.println("\nSame style requested twice — same object? "
                + (bodyStyle == bodyStyle2));
        System.out.println("Cache size: " + factory.getCacheSize()
                + " (3 styles, not 3 × character count)\n");

        // Build a document with many characters sharing just a few styles
        List<CharacterGlyph> document = new ArrayList<>();
        String heading = "Introduction";
        String body    = "The flyweight pattern reduces memory usage.";

        int x = 10, y = 10;
        for (char c : heading.toCharArray()) {
            document.add(new CharacterGlyph(c, x, y, headingStyle));
            x += 14;
        }
        y = 40; x = 10;
        for (char c : body.toCharArray()) {
            FontStyle s = c == 'f' || c == 'l' ? emphStyle : bodyStyle; // emphasise 'fl'
            document.add(new CharacterGlyph(c, x, y, s));
            x += 8;
        }

        System.out.println("Rendering " + document.size() + " characters using "
                + factory.getCacheSize() + " shared FontStyle objects:");
        document.forEach(CharacterGlyph::render);
    }
}
