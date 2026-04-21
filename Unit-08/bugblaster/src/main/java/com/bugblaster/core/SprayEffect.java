package com.bugblaster.core;

import java.awt.*;

/**
 * A quick puff of insecticide spray — green mist circle that expands and fades.
 * Triggered by a single left click.
 */
public final class SprayEffect extends Effect {

    private static final Color MIST  = new Color(50,  220, 80);
    private static final Color OUTER = new Color(80,  255, 110);

    private final double x;
    private final double y;
    private final int    baseRadius;

    public SprayEffect(double x, double y, int radius) {
        super(18);
        this.x          = x;
        this.y          = y;
        this.baseRadius = radius;
    }

    @Override
    public void draw(Graphics2D g) {
        float  a    = alpha();
        int    r    = (int)(baseRadius * (1.0f + progress() * 0.6f));
        Composite old = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a * 0.45f));
        g.setColor(MIST);
        g.fillOval((int)(x - r), (int)(y - r), r * 2, r * 2);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        g.setColor(OUTER);
        g.setStroke(new BasicStroke(2f));
        g.drawOval((int)(x - r), (int)(y - r), r * 2, r * 2);

        g.setComposite(old);
    }
}
