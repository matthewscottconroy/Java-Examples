package com.bugblaster.core;

import java.awt.*;

/**
 * The satisfying boot stomp — a cartoon boot slams down followed by a brown
 * shockwave ring. Triggered by a left double-click.
 */
public final class StompEffect extends Effect {

    static final int RADIUS = 70;

    private static final Color BOOT_DARK  = new Color(70,  45, 20);
    private static final Color BOOT_MID   = new Color(100, 65, 30);
    private static final Color BOOT_SHEEN = new Color(130, 90, 45);
    private static final Color WAVE_COLOR = new Color(180, 120, 40);

    private final double x;
    private final double y;

    public StompEffect(double x, double y) {
        super(30);
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Graphics2D g) {
        float prog = progress();
        float a    = alpha();
        Composite old = g.getComposite();

        // Phase 1 (0–40%): Boot appears and slams
        if (prog < 0.45f) {
            float bootA = 1f - prog / 0.45f;
            float drop  = prog / 0.45f; // 0 = top, 1 = fully landed

            int bx = (int)(x - 28);
            int by = (int)(y - 35 + drop * 35);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bootA));

            // Sole
            g.setColor(BOOT_DARK);
            g.fillOval(bx, by + 20, 56, 22);
            g.setColor(BOOT_MID);
            g.fillOval(bx + 1, by + 21, 54, 20);

            // Shaft
            g.setColor(BOOT_DARK);
            g.fillRoundRect(bx + 10, by - 15, 30, 38, 8, 8);
            g.setColor(BOOT_MID);
            g.fillRoundRect(bx + 12, by - 13, 26, 34, 6, 6);
            g.setColor(BOOT_SHEEN);
            g.fillRoundRect(bx + 14, by - 11, 10, 12, 4, 4);

            // Sole outline
            g.setColor(BOOT_DARK);
            g.setStroke(new BasicStroke(1.5f));
            g.drawOval(bx, by + 20, 56, 22);
        }

        // Phase 2 (20–100%): Shockwave ring expands
        if (prog > 0.20f) {
            float ringProg = (prog - 0.20f) / 0.80f;
            int   r        = (int)(RADIUS * ringProg);
            float ringA    = a * (1f - ringProg * 0.5f);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, ringA)));
            g.setColor(WAVE_COLOR);
            g.setStroke(new BasicStroke(3f * (1f - ringProg) + 1f));
            g.drawOval((int)(x - r), (int)(y - r), r * 2, r * 2);

            // "STOMP!" text
            if (tick < 12) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
                g.setColor(Color.YELLOW);
                g.setFont(new Font("SansSerif", Font.BOLD, 20));
                FontMetrics fm = g.getFontMetrics();
                String s = "STOMP!";
                g.drawString(s, (int)(x - fm.stringWidth(s) / 2f), (int)(y - 45 + tick * 1.5));
            }
        }

        g.setComposite(old);
    }
}
