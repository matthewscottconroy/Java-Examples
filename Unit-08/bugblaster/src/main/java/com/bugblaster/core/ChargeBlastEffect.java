package com.bugblaster.core;

import java.awt.*;

/**
 * The charged blast — three expanding concentric rings and a central flash.
 * Triggered by holding the left mouse button for ≥400 ms and releasing.
 * The blast radius (and damage) scale with how long the button was held.
 */
public final class ChargeBlastEffect extends Effect {

    private static final Color RING1 = new Color(100, 200, 255);
    private static final Color RING2 = new Color(150, 230, 255);
    private static final Color RING3 = new Color(200, 245, 255);
    private static final Color FLASH = new Color(180, 235, 255);

    private final double x;
    private final double y;
    private final int    radius;

    public ChargeBlastEffect(double x, double y, int radius) {
        super(45);
        this.x      = x;
        this.y      = y;
        this.radius = radius;
    }

    @Override
    public void draw(Graphics2D g) {
        float  prog = progress();
        float  a    = alpha();
        Composite old = g.getComposite();

        // Three staggered expanding rings
        Color[] colors = { RING1, RING2, RING3 };
        for (int ring = 0; ring < 3; ring++) {
            float rp = Math.min(1f, prog * 2.5f - ring * 0.25f);
            if (rp <= 0) continue;
            float ra = a * (1f - rp * 0.4f);
            if (ra <= 0) continue;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ra));
            int r = (int)(radius * rp);
            g.setColor(colors[ring]);
            g.setStroke(new BasicStroke(3f - ring * 0.7f));
            g.drawOval((int)(x - r), (int)(y - r), r * 2, r * 2);
        }

        // Central flash (only first 8 ticks)
        if (tick < 8) {
            float fa = (8 - tick) / 8f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fa * 0.85f));
            g.setColor(FLASH);
            int fr = radius / 3;
            g.fillOval((int)(x - fr), (int)(y - fr), fr * 2, fr * 2);
        }

        // Radius label (so the player can see how charged they got)
        if (tick < 20) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.setColor(RING1);
            g.setFont(new Font("SansSerif", Font.BOLD, 13));
            String s = "BLAST r=" + radius;
            FontMetrics fm = g.getFontMetrics();
            g.drawString(s, (int)(x - fm.stringWidth(s) / 2f), (int)(y - radius - 6));
        }

        g.setComposite(old);
    }
}
