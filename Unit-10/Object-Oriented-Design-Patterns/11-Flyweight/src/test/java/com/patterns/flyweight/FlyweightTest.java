package com.patterns.flyweight;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Flyweight pattern — Word Processor Character Styles.
 */
class FlyweightTest {

    @Test
    @DisplayName("Factory returns the same object for identical style requests")
    void sameStyleIsCached() {
        FontStyleFactory factory = new FontStyleFactory();
        FontStyle a = factory.getStyle("Arial", 12, false, false);
        FontStyle b = factory.getStyle("Arial", 12, false, false);
        assertSame(a, b, "Identical style requests should return the cached instance");
    }

    @Test
    @DisplayName("Factory returns different objects for different styles")
    void differentStylesAreDistinct() {
        FontStyleFactory factory = new FontStyleFactory();
        FontStyle normal = factory.getStyle("Arial", 12, false, false);
        FontStyle bold   = factory.getStyle("Arial", 12, true,  false);
        assertNotSame(normal, bold);
    }

    @Test
    @DisplayName("Cache size equals distinct style count, not request count")
    void cacheSizeIsDistinctCount() {
        FontStyleFactory factory = new FontStyleFactory();
        factory.getStyle("Arial", 12, false, false);
        factory.getStyle("Arial", 12, false, false); // duplicate
        factory.getStyle("Arial", 14, true,  false);
        assertEquals(2, factory.getCacheSize());
    }

    @Test
    @DisplayName("CharacterGlyph holds a reference to the shared flyweight")
    void glyphSharesStyle() {
        FontStyleFactory factory = new FontStyleFactory();
        FontStyle style = factory.getStyle("Times", 12, false, false);

        CharacterGlyph g1 = new CharacterGlyph('A', 0, 0, style);
        CharacterGlyph g2 = new CharacterGlyph('B', 10, 0, style);

        assertSame(g1.getStyle(), g2.getStyle(), "Both glyphs should share the same FontStyle");
    }

    @Test
    @DisplayName("FontStyle equality is value-based, not reference-based")
    void fontStyleEquality() {
        FontStyle a = new FontStyleFactory().getStyle("Arial", 12, false, false);
        // Create manually with same properties
        FontStyle b = new FontStyleFactory().getStyle("Arial", 12, false, false);
        assertEquals(a, b); // equals() is implemented by value
    }
}
