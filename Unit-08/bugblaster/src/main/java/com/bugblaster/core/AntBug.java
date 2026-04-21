package com.bugblaster.core;

import java.awt.*;

/**
 * The common ant — marches in a straight line with military precision.
 *
 * <p><b>Stats:</b> 1 HP · medium speed · 10 pts
 * <br><b>Weakness:</b> any spray (left click) or drag trail.
 *
 * <p>Ants are cannon fodder that arrive in large numbers. A well-placed drag
 * spray can wipe out an entire column in one stroke. Their devotion to the
 * colony makes them dangerously single-minded about that Snack Bowl.
 */
public final class AntBug extends Bug {

    private static final Color BODY  = new Color(60,  30, 10);
    private static final Color LEG   = new Color(80,  45, 15);
    private static final Color EYE   = new Color(240, 80, 80);

    private static final String[] LAST_WORDS = {
        "FOR THE COLONY! …oh no",
        "I was SO CLOSE to the crumbs",
        "Tell the queen I tried",
        "Ow. Rude.",
        "This is why we have a million sisters",
        "Worth it for the crumbs"
    };

    public AntBug(double x, double y) {
        super(x, y, 1, LAST_WORDS[(int)(Math.random() * LAST_WORDS.length)]);
    }

    @Override protected double getBaseSpeed() { return 1.1 + Math.random() * 0.5; }
    @Override public    int    getHitRadius()  { return 10; }
    @Override public    int    getScoreValue() { return 10; }

    @Override
    public void update() {
        super.update();
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) x;
        int cy = (int) y;
        int phase = (animTick / 4) % 2;

        // Six legs, three per side, alternating phase
        g.setColor(LEG);
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 3; i++) {
            int lx = cx - 4 + i * 4;
            int offL = (i % 2 == phase) ? 5 : 2;
            int offR = (i % 2 == phase) ? 2 : 5;
            g.drawLine(lx, cy - 1, lx - 4, cy - 1 - offL);
            g.drawLine(lx, cy + 1, lx + 4, cy + 1 + offR);
        }

        // Three body segments
        g.setColor(BODY);
        g.fillOval(cx - 8, cy - 4, 8,  7);  // abdomen
        g.fillOval(cx - 2, cy - 3, 5,  6);  // thorax
        g.fillOval(cx + 2, cy - 3, 6,  6);  // head

        // Antennae
        g.setColor(LEG);
        g.drawLine(cx + 5, cy - 3, cx + 9, cy - 7);
        g.drawLine(cx + 6, cy - 2, cx + 10, cy - 1);

        // Eyes
        g.setColor(EYE);
        g.fillRect(cx + 4, cy - 2, 2, 2);

        drawHpBar(g, cx, cy);
    }
}
