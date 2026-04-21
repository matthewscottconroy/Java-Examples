package com.bugblaster.core;

import java.awt.*;

/**
 * A short-lived spray trail drawn as a glowing green line segment.
 *
 * <p>One {@code DragTrailEffect} is added for each mouse-drag event, producing
 * a continuous painted trail as the player drags the mouse. The segments fade
 * quickly so the trail looks dynamic rather than permanent.
 */
public final class DragTrailEffect extends Effect {

    private static final Color TRAIL = new Color(60,  220, 100);

    private final double x1, y1, x2, y2;

    public DragTrailEffect(double x1, double y1, double x2, double y2) {
        super(14);
        this.x1 = x1; this.y1 = y1;
        this.x2 = x2; this.y2 = y2;
    }

    @Override
    public void draw(Graphics2D g) {
        float a = alpha();
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a * 0.9f));
        g.setColor(TRAIL);
        g.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        // Brighter core
        g.setColor(new Color(160, 255, 180));
        g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);

        g.setComposite(old);
    }
}
