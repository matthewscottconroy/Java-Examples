package com.bugblaster.core;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A sticky glue trap placed by right-clicking anywhere on the floor.
 *
 * <p>When a bug walks within the trap's radius it is caught: its movement stops
 * and it takes 2 damage every second until it dies or the trap fills to capacity
 * ({@value #MAX_CAUGHT} bugs). Dead bugs are automatically released and the
 * trap becomes available again.
 *
 * <p>Right-dragging an existing trap repositions it — useful for herding slow
 * targets like spiders and beetles into the glue.
 */
public final class Trap {

    private static final int MAX_CAUGHT      = 3;
    private static final int DAMAGE_INTERVAL = 60;  // ticks between damage pulses (~1 s at 60 fps)
    static final int RADIUS = 25;

    private double x;
    private double y;
    private int    tick;
    private final List<Bug> caughtBugs = new ArrayList<>();

    public Trap(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /** Advances the trap by one tick, dealing periodic damage to caught bugs. */
    public void update() {
        tick++;

        if (tick % DAMAGE_INTERVAL == 0) {
            caughtBugs.forEach(b -> b.hit(2));
        }

        // Release dead bugs so the slot opens up for new victims
        caughtBugs.removeIf(b -> !b.isAlive());
    }

    /**
     * Checks whether the given bug is within range; if so, and the trap is not
     * full, it catches the bug and halts its movement.
     */
    public void tryToCatch(Bug bug) {
        if (caughtBugs.size() >= MAX_CAUGHT) return;
        if (caughtBugs.contains(bug))        return;
        double dx = bug.getX() - x;
        double dy = bug.getY() - y;
        if (dx * dx + dy * dy <= (double)(RADIUS + bug.getHitRadius()) * (RADIUS + bug.getHitRadius())) {
            caughtBugs.add(bug);
            bug.setCaught(true);
        }
    }

    /** Returns {@code true} if this trap can still catch more bugs. */
    public boolean isActive() { return caughtBugs.size() < MAX_CAUGHT; }

    /** Returns {@code true} if the given screen point is within the trap's grab radius. */
    public boolean containsPoint(double px, double py) {
        double dx = px - x, dy = py - y;
        return dx * dx + dy * dy <= (double) RADIUS * RADIUS;
    }

    public void setPosition(double x, double y) { this.x = x; this.y = y; }
    public double getX() { return x; }
    public double getY() { return y; }

    public void draw(Graphics2D g) {
        int cx = (int) x, cy = (int) y;

        // Glue base
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        g.setColor(new Color(255, 215, 35));
        g.fillOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);
        g.setComposite(old);

        // Border
        Color border = isActive() ? new Color(200, 155, 15) : new Color(130, 100, 15);
        g.setColor(border);
        g.setStroke(new BasicStroke(2f));
        g.drawOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);

        // Label
        g.setFont(new Font("Monospaced", Font.BOLD, 8));
        g.setColor(new Color(90, 60, 0));
        String lbl = "GLUE";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(lbl, cx - fm.stringWidth(lbl) / 2, cy + 4);

        // Caught count
        if (!caughtBugs.isEmpty()) {
            g.setColor(Color.RED);
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            g.drawString("×" + caughtBugs.size(), cx + RADIUS - 10, cy - RADIUS + 12);
        }
    }
}
