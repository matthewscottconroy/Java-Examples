package com.bugblaster.core;

import java.awt.*;

/**
 * The housefly — fast, erratic, and actively dodges the player's cursor.
 *
 * <p><b>Stats:</b> 1 HP · high speed · 50 pts
 * <br><b>Weakness:</b> precise left click; patience.
 *
 * <p>Flies detect the player's cursor within 90 pixels and accelerate away from
 * it, making them the trickiest target in the game. The trick is to anticipate
 * where they'll flee rather than trying to click on them directly. Worth 50 pts
 * because they absolutely deserve it.
 */
public final class FlyBug extends Bug {

    private static final Color BODY   = new Color(30,  28, 25);
    private static final Color WING   = new Color(180, 210, 240, 120);
    private static final Color WING_E = new Color(120, 160, 200, 180);
    private static final Color EYE    = new Color(200, 50,  50);

    private static final String[] LAST_WORDS = {
        "BZZZZ—",
        "I was JUST at the bin",
        "I had a whole route planned",
        "You'll never catch my 47 other siblings",
        "...bzzz",
        "I landed on that sandwich first"
    };

    /** Dodge radius in pixels — flies panic within this distance of the cursor. */
    private static final double DODGE_RADIUS = 90.0;

    private double wingAngle;

    public FlyBug(double x, double y) {
        super(x, y, 1, LAST_WORDS[(int)(Math.random() * LAST_WORDS.length)]);
    }

    @Override protected double getBaseSpeed() { return 2.4 + Math.random() * 0.8; }
    @Override public    int    getHitRadius()  { return 9; }
    @Override public    int    getScoreValue() { return 50; }

    @Override
    public void update() {
        if (caught) return;
        animTick++;
        wingAngle += 0.6;

        x += vx;
        y += vy;

        // Bounce off walls
        if (x < 5  || x > 895) { vx = -vx; x = Math.max(5,  Math.min(895, x)); }
        if (y < 5  || y > 615) { vy = -vy; y = Math.max(5,  Math.min(615, y)); }

        // Drift back toward target over time
        double angle = Math.atan2(TARGET_Y - y, TARGET_X - x);
        double speed = getBaseSpeed();
        vx = vx * 0.97 + Math.cos(angle) * speed * 0.05;
        vy = vy * 0.97 + Math.sin(angle) * speed * 0.05;

        // Cap speed
        double spd = Math.hypot(vx, vy);
        if (spd > speed * 1.8) { vx = vx / spd * speed * 1.8; vy = vy / spd * speed * 1.8; }

        if (distanceToTarget() < 20) { reached = true; alive = false; }
    }

    /**
     * Steers away from the cursor when it is within {@link #DODGE_RADIUS} pixels.
     * Called every game tick by the controller before {@link #update()}.
     */
    @Override
    public void onCursorNear(double cursorX, double cursorY) {
        double dx   = x - cursorX;
        double dy   = y - cursorY;
        double dist = Math.hypot(dx, dy);
        if (dist < DODGE_RADIUS && dist > 0) {
            double flee = getBaseSpeed() * 2.5 * (1.0 - dist / DODGE_RADIUS);
            vx += (dx / dist) * flee;
            vy += (dy / dist) * flee;
        }
    }

    @Override
    public void draw(Graphics2D g) {
        int cx = (int) x;
        int cy = (int) y;

        // Wings (translucent, flapping via wingAngle)
        float wingY = (float)(Math.sin(wingAngle) * 4);
        g.setColor(WING);
        g.fillOval(cx - 14, cy - 8  + (int)wingY, 12, 7);
        g.fillOval(cx +  2, cy - 8  + (int)wingY, 12, 7);
        g.setColor(WING_E);
        g.setStroke(new BasicStroke(1f));
        g.drawOval(cx - 14, cy - 8  + (int)wingY, 12, 7);
        g.drawOval(cx +  2, cy - 8  + (int)wingY, 12, 7);

        // Body (thorax + abdomen)
        g.setColor(BODY);
        g.fillOval(cx - 5, cy - 4, 10, 9);
        g.fillOval(cx - 4, cy + 3,  9, 7);

        // Compound eyes
        g.setColor(EYE);
        g.fillOval(cx - 5, cy - 5, 4, 4);
        g.fillOval(cx + 1, cy - 5, 4, 4);

        // Tiny legs
        g.setColor(new Color(60, 55, 50));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(cx - 3, cy + 1, cx - 7, cy + 5);
        g.drawLine(cx,     cy + 2, cx - 5, cy + 7);
        g.drawLine(cx + 3, cy + 1, cx + 7, cy + 5);

        drawHpBar(g, cx, cy);
    }
}
