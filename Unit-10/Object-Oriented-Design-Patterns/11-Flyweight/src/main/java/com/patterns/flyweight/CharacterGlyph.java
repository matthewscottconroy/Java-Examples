package com.patterns.flyweight;

/**
 * Context — stores the <em>extrinsic</em> (unique, per-character) state.
 *
 * <p>Each character on the page has its own position ({@code x}, {@code y}) and
 * its own glyph ({@code ch}). These cannot be shared. The expensive
 * {@link FontStyle} object (intrinsic state) is shared via the flyweight.
 *
 * <p>In a 500-page document with 200,000 characters in three distinct styles,
 * this design creates 200,000 tiny {@code CharacterGlyph} objects (3 ints each)
 * plus just 3 {@code FontStyle} flyweights, instead of 200,000 full font objects.
 */
public class CharacterGlyph {

    private final char      ch;
    private final int       x;
    private final int       y;
    private final FontStyle style; // shared flyweight — NOT owned by this object

    /**
     * @param ch    the character to render
     * @param x     horizontal position in pixels
     * @param y     vertical position in pixels
     * @param style the shared font style flyweight
     */
    public CharacterGlyph(char ch, int x, int y, FontStyle style) {
        this.ch    = ch;
        this.x     = x;
        this.y     = y;
        this.style = style;
    }

    /**
     * Renders this character by delegating to the shared flyweight, passing in
     * the extrinsic position state.
     */
    public void render() {
        style.render(ch, x, y);
    }

    /** @return the shared {@link FontStyle} flyweight */
    public FontStyle getStyle() { return style; }
}
