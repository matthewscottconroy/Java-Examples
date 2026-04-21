package com.bugblaster.core;

import java.awt.*;

/**
 * The cockroach — erratic mover, hard to pin down, somehow invulnerable to
 * everything except a really satisfying stomp.
 *
 * <p><b>Stats:</b> 3 HP · medium speed · 30 pts
 * <br><b>Weakness:</b> boot stomp (double-click) or charged blast.
 *
 * <p>Cockroaches change direction randomly every few ticks, making them
 * infuriating to spray. The player's best strategy is a well-timed double-click.
 * They have reportedly survived nuclear blasts; a light spritz of insecticide
 * merely inconveniences them.
 */
public final class CockroachBug extends Bug {

    private static final Color SHELL  = new Color(80,  55, 15);
    private static final Color DETAIL = new Color(110, 80, 25);
    private static final Color LEG    = new Color(60,  40, 10);

    private static final String[] LAST_WORDS = {
        "I have survived nuclear blasts — this is embarrassing",
        "SQUASHED? SQUASHED?! I am ANCIENT.",
        "I'll be back. Probably. We always come back.",
        "This kitchen was MINE.",
        "Five hundred million years of evolution… foiled by a boot",
        "Next time I'm going for the bathroom"
    };

    private int dirChangeTick;
    private int nextDirChange;

    public CockroachBug(double x, double y) {
        super(x, y, 3, LAST_WORDS[(int)(Math.random() * LAST_WORDS.length)]);
        nextDirChange = 40 + (int)(Math.random() * 60);
    }

    @Override protected double getBaseSpeed() { return 0.9 + Math.random() * 0.4; }
    @Override public    int    getHitRadius()  { return 13; }
    @Override public    int    getScoreValue() { return 30; }

    @Override
    public void update() {
        if (caught) return;
        animTick++;
        dirChangeTick++;

        // Erratic direction change
        if (dirChangeTick >= nextDirChange) {
            dirChangeTick = 0;
            nextDirChange = 30 + (int)(Math.random() * 70);
            double speed  = getBaseSpeed();
            // Mostly toward the target with a random deviation
            double angle  = Math.atan2(TARGET_Y - y, TARGET_X - x);
            angle += (Math.random() - 0.5) * Math.PI * 0.8;
            vx = Math.cos(angle) * speed;
            vy = Math.sin(angle) * speed;
        }

        x += vx;
        y += vy;

        // Clamp to game area (roaches don't fall off the edge — they're too wily)
        x = Math.max(5,  Math.min(895, x));
        y = Math.max(5,  Math.min(615, y));

        if (distanceToTarget() < 20) {
            reached = true;
            alive   = false;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) x;
        int cy = (int) y;
        int phase = (animTick / 3) % 2;

        // Six long legs
        g.setColor(LEG);
        g.setStroke(new BasicStroke(1f));
        for (int i = 0; i < 3; i++) {
            int lx = cx - 5 + i * 5;
            int offY = (i % 2 == phase) ? 6 : 3;
            g.drawLine(lx, cy,     lx - 7, cy - offY);
            g.drawLine(lx, cy + 1, lx + 7, cy + offY);
        }

        // Wing covers (elytra)
        g.setColor(SHELL);
        g.fillOval(cx - 10, cy - 5, 20, 11);  // main body
        g.setColor(DETAIL);
        g.drawLine(cx, cy - 5, cx, cy + 5);   // centre seam

        // Head
        g.setColor(SHELL);
        g.fillOval(cx + 8, cy - 3, 7, 7);

        // Long antennae
        g.setColor(LEG);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(cx + 12, cy - 2, cx + 22, cy - 8);
        g.drawLine(cx + 12, cy + 2, cx + 22, cy + 6);

        drawHpBar(g, cx, cy);
    }
}
