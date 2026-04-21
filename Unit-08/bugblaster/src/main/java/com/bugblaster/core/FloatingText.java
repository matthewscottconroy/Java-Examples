package com.bugblaster.core;

import java.awt.*;

/**
 * A short humorous or informational message that floats upward and fades out
 * over about one second.
 *
 * <p>Floating texts appear on kills ("FOR THE COLONY! …oh no"), on misses
 * ("MISS!"), on combo bonuses ("COMBO ×4!"), and when a bug reaches the Snack
 * Bowl ("THEY'RE IN THE PIE!").
 */
public final class FloatingText {

    private static final int LIFETIME = 65;

    private final String text;
    private final Color  color;
    private final int    fontSize;
    private double x;
    private double y;
    private int    tick;

    public FloatingText(String text, double x, double y, Color color) {
        this(text, x, y, color, 13);
    }

    public FloatingText(String text, double x, double y, Color color, int fontSize) {
        this.text     = text;
        this.x        = x;
        this.y        = y;
        this.color    = color;
        this.fontSize = fontSize;
    }

    public void update() {
        tick++;
        y -= 1.0 + tick * 0.01;  // accelerate slightly as it fades
    }

    public boolean isExpired() { return tick >= LIFETIME; }

    public void draw(Graphics2D g) {
        float alpha = Math.max(0f, 1f - (float) tick / LIFETIME);
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(text);

        // Drop shadow
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(text, (int)(x - tw / 2f) + 1, (int) y + 1);

        // Text
        g.setColor(color);
        g.drawString(text, (int)(x - tw / 2f), (int) y);

        g.setComposite(old);
    }
}
