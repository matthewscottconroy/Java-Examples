package com.bugblaster.core;

import java.awt.*;

/**
 * The rhinoceros beetle — slow, armoured, and utterly contemptuous of spray.
 *
 * <p><b>Stats:</b> 8 HP · very slow · 60 pts
 * <br><b>Weakness:</b> hold the left mouse button for a fully charged blast
 * (max charge = 100 px radius, 8 damage — a one-shot kill). A trap also works
 * but you have to wait ~4 seconds for it to digest the beetle.
 *
 * <p>Beetles are living tanks. The spray barely tickles them. They advance with
 * the serene confidence of something that knows it is practically unkillable.
 * The player's best hope is a satisfyingly large charged blast to the face.
 */
public final class BeetleBug extends Bug {

    private static final Color SHELL   = new Color(50,  30, 90);
    private static final Color SHEEN   = new Color(90,  60, 150);
    private static final Color OUTLINE = new Color(30,  15, 60);
    private static final Color LEG     = new Color(40,  25, 70);

    private static final String[] LAST_WORDS = {
        "I. Am. INDESTRUCTIBLE. Oh.",
        "You needed a BOMB for that?",
        "This is EMBARRASSING. I have a horn.",
        "I have survived 300 million years of this planet.",
        "Do you have any idea what I bench?",
        "Fine. FINE. But the next one will need a bigger bomb."
    };

    public BeetleBug(double x, double y) {
        super(x, y, 8, LAST_WORDS[(int)(Math.random() * LAST_WORDS.length)]);
    }

    @Override protected double getBaseSpeed() { return 0.28 + Math.random() * 0.15; }
    @Override public    int    getHitRadius()  { return 16; }
    @Override public    int    getScoreValue() { return 60; }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) x;
        int cy = (int) y;

        // Short, stubby legs
        g.setColor(LEG);
        g.setStroke(new BasicStroke(2f));
        for (int i = 0; i < 3; i++) {
            int lx = cx - 6 + i * 6;
            g.drawLine(lx, cy - 1, lx - 5, cy - 6);
            g.drawLine(lx, cy + 1, lx + 5, cy + 6);
        }

        // Elytra (wing covers) — the big shiny oval
        g.setColor(OUTLINE);
        g.fillOval(cx - 13, cy - 9, 27, 18);
        g.setColor(SHELL);
        g.fillOval(cx - 12, cy - 8, 25, 16);

        // Metallic sheen stripe
        g.setColor(SHEEN);
        g.fillOval(cx - 8, cy - 6, 10, 5);

        // Head
        g.setColor(OUTLINE);
        g.fillOval(cx + 10, cy - 5, 9, 10);
        g.setColor(SHELL);
        g.fillOval(cx + 11, cy - 4, 7,  8);

        // Rhino horn (the beetle's pride and joy)
        g.setColor(SHEEN);
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(cx + 15, cy - 4, cx + 22, cy - 10);

        // Eyes
        g.setColor(new Color(240, 200, 60));
        g.fillRect(cx + 12, cy - 2, 2, 2);

        drawHpBar(g, cx, cy);
    }
}
