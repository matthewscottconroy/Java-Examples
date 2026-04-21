package com.bugblaster.core;

import java.awt.*;

/**
 * The spider — slow, methodical, and utterly unperturbed by casual spray.
 *
 * <p><b>Stats:</b> 5 HP · slow speed · 40 pts
 * <br><b>Weakness:</b> sticky trap (right-click to place); trapped spiders die
 * in under 3 seconds. Spray still works but you'll be there a while.
 *
 * <p>Spiders move with eerie deliberateness, pausing every few ticks as if
 * deciding which of their eight eyes to use next. Place a trap in their path
 * and they'll walk right into it. This is because spiders, despite eight eyes,
 * have the spatial awareness of a raisin.
 */
public final class SpiderBug extends Bug {

    private static final Color BODY  = new Color(25,  20, 30);
    private static final Color LEG   = new Color(50,  40, 60);
    private static final Color MARK  = new Color(200, 40, 40);

    private static final String[] LAST_WORDS = {
        "I was HELPING. Flies eat bugs too, you know.",
        "Eight eyes and I still didn't see that coming",
        "I had a WEB here. A BEAUTIFUL web.",
        "Put me outside. I would have gone OUTSIDE.",
        "Do you know how long that web took?",
        "I'm more scared of you than you are of me — oh wait"
    };

    private int pauseTick;
    private int nextPause;
    private boolean paused;

    public SpiderBug(double x, double y) {
        super(x, y, 5, LAST_WORDS[(int)(Math.random() * LAST_WORDS.length)]);
        nextPause = 60 + (int)(Math.random() * 90);
    }

    @Override protected double getBaseSpeed() { return 0.5 + Math.random() * 0.3; }
    @Override public    int    getHitRadius()  { return 14; }
    @Override public    int    getScoreValue() { return 40; }

    @Override
    public void update() {
        if (caught) return;
        animTick++;
        pauseTick++;

        // Pause and recalculate direction periodically
        if (pauseTick >= nextPause) {
            pauseTick = 0;
            nextPause = 50 + (int)(Math.random() * 100);
            paused    = !paused;
            if (!paused) pointTowardTarget(getBaseSpeed());
        }

        if (!paused) {
            x += vx;
            y += vy;
        }

        if (distanceToTarget() < 20) { reached = true; alive = false; }
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) x;
        int cy = (int) y;
        int phase = (animTick / 6) % 2;

        // Eight legs radiating outward
        g.setColor(LEG);
        g.setStroke(new BasicStroke(1.5f));
        double[] angles = { -2.4, -1.8, -1.2, -0.5, 0.5, 1.2, 1.8, 2.4 };
        for (int i = 0; i < 8; i++) {
            double a  = angles[i];
            int legR  = 16 + (i % 2 == phase ? 2 : 0);
            int ex    = cx + (int)(Math.cos(a) * legR);
            int ey    = cy + (int)(Math.sin(a) * legR);
            int kx    = cx + (int)(Math.cos(a) * 8);
            int ky    = cy + (int)(Math.sin(a) * 8);
            // Two-segment leg (knee in the middle)
            g.drawLine(cx, cy, kx, ky);
            g.drawLine(kx, ky, ex, ey);
        }

        // Abdomen
        g.setColor(BODY);
        g.fillOval(cx - 10, cy - 8, 20, 16);

        // Cephalothorax
        g.fillOval(cx - 5, cy - 5, 11, 10);

        // Red hourglass mark on abdomen
        g.setColor(MARK);
        g.fillOval(cx - 4, cy - 3, 8, 6);

        // Eyes (two rows)
        g.setColor(new Color(220, 200, 50));
        for (int i = 0; i < 4; i++) {
            g.fillRect(cx - 6 + i * 3, cy - 5, 2, 2);
        }

        drawHpBar(g, cx, cy);
    }
}
