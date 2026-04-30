package com.patterns.flyweight;

import java.util.Objects;

/**
 * Flyweight — the <em>intrinsic</em> (shared, immutable) state of a text style.
 *
 * <p>In a word processor, a 500-page document might contain 200,000 characters.
 * If every character stored its own font family, size, bold, and italic flags,
 * that would be four fields × 200,000 objects. Most characters in any paragraph
 * share the same style. A flyweight captures that shared state once, and every
 * character that uses "Arial 12pt bold" points to the same flyweight object.
 *
 * <p><b>Intrinsic state</b> (in this object): font family, size, bold, italic.
 * <b>Extrinsic state</b> (passed in when used): the character itself and its
 * position on the page.
 */
public final class FontStyle {

    private final String  fontFamily;
    private final int     sizePt;
    private final boolean bold;
    private final boolean italic;

    /**
     * @param fontFamily the font family name (e.g., "Arial")
     * @param sizePt     the point size (e.g., 12)
     * @param bold       true if bold
     * @param italic     true if italic
     */
    FontStyle(String fontFamily, int sizePt, boolean bold, boolean italic) {
        this.fontFamily = fontFamily;
        this.sizePt     = sizePt;
        this.bold       = bold;
        this.italic     = italic;
    }

    /**
     * Renders the given character at the given position using this font style.
     *
     * <p>In a real word processor this would invoke a native rendering call.
     * Here it simply prints a description.
     *
     * @param ch the character glyph (extrinsic state)
     * @param x  horizontal pixel position (extrinsic state)
     * @param y  vertical pixel position (extrinsic state)
     */
    public void render(char ch, int x, int y) {
        System.out.printf("  Render '%c' at (%d,%d) in %s %dpt%s%s%n",
                ch, x, y, fontFamily, sizePt,
                bold   ? " Bold"   : "",
                italic ? " Italic" : "");
    }

    /** @return the font family name */
    public String getFontFamily() { return fontFamily; }

    /** @return the point size */
    public int getSizePt()        { return sizePt; }

    /** @return true if bold */
    public boolean isBold()       { return bold; }

    /** @return true if italic */
    public boolean isItalic()     { return italic; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof FontStyle f)) return false;
        return sizePt == f.sizePt && bold == f.bold && italic == f.italic
                && Objects.equals(fontFamily, f.fontFamily);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fontFamily, sizePt, bold, italic);
    }

    @Override
    public String toString() {
        return fontFamily + " " + sizePt + "pt"
                + (bold   ? " Bold"   : "")
                + (italic ? " Italic" : "");
    }
}
