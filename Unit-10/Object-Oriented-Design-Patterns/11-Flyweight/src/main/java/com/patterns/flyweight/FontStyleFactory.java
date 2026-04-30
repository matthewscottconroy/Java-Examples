package com.patterns.flyweight;

import java.util.HashMap;
import java.util.Map;

/**
 * Flyweight Factory — creates and caches {@link FontStyle} flyweights.
 *
 * <p>Callers never construct a {@code FontStyle} directly. They ask the factory
 * for one by specifying its properties. The factory returns an existing cached
 * instance if one already exists, or creates and caches a new one.
 *
 * <p>This ensures that all characters using "Arial 12pt Bold" share a single
 * {@code FontStyle} object in memory — not 50,000 identical copies.
 */
public class FontStyleFactory {

    private final Map<String, FontStyle> cache = new HashMap<>();

    /**
     * Returns a flyweight for the specified font style, creating it if necessary.
     *
     * @param fontFamily the font family name
     * @param sizePt     the point size
     * @param bold       bold flag
     * @param italic     italic flag
     * @return a shared, cached {@link FontStyle} instance
     */
    public FontStyle getStyle(String fontFamily, int sizePt, boolean bold, boolean italic) {
        String key = fontFamily + "|" + sizePt + "|" + bold + "|" + italic;
        return cache.computeIfAbsent(key, k -> {
            System.out.println("  [Factory] Creating new FontStyle: " + fontFamily
                    + " " + sizePt + "pt" + (bold ? " Bold" : "") + (italic ? " Italic" : ""));
            return new FontStyle(fontFamily, sizePt, bold, italic);
        });
    }

    /** @return the number of distinct font styles currently cached */
    public int getCacheSize() { return cache.size(); }
}
