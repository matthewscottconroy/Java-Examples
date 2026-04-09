package com.wattsstrogatz.ui;

import com.wattsstrogatz.model.NetworkMetrics;

import javax.swing.*;
import java.awt.*;

/**
 * Panel showing normalised Watts-Strogatz metrics as horizontal bars:
 * <ul>
 *   <li>C(p)/C(0) — clustering relative to ring lattice (blue)</li>
 *   <li>L(p)/L(0) — path length relative to ring lattice (red)</li>
 * </ul>
 * When L drops while C stays high, the network is in the small-world regime.
 */
public final class MetricsPanel extends JPanel {

    private static final Color C_COLOR  = new Color(70, 140, 200);
    private static final Color L_COLOR  = new Color(210, 90, 60);
    private static final Color BAR_BG   = new Color(220, 225, 230);
    private static final Color TEXT_FG  = new Color(50, 50, 60);
    private static final int   BAR_H    = 26;
    private static final int   MARGIN   = 14;

    private NetworkMetrics.MetricsSnapshot current;
    private NetworkMetrics.MetricsSnapshot relative;
    private double progress;

    /** Creates the metrics panel with all values at 1.0. */
    public MetricsPanel() {
        setPreferredSize(new Dimension(210, 155));
        setBackground(new Color(245, 245, 248));
        NetworkMetrics.MetricsSnapshot unit = new NetworkMetrics.MetricsSnapshot(1.0, 1.0);
        this.current  = unit;
        this.relative = unit;
        this.progress = 0.0;
    }

    /**
     * Updates the displayed metrics. Does not repaint.
     *
     * @param current  raw current metrics
     * @param relative metrics normalised by baseline
     * @param progress rewiring progress in [0, 1]
     */
    public void update(NetworkMetrics.MetricsSnapshot current,
                       NetworkMetrics.MetricsSnapshot relative,
                       double progress) {
        this.current  = current;
        this.relative = relative;
        this.progress = progress;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int y = MARGIN;

        y = bar(g2, "Rewiring progress",
            progress, new Color(100, 170, 100), w, y) + 6;

        double relC = clamp(relative.getClusteringCoefficient());
        y = bar(g2, String.format("C(p)/C(0) = %.3f", relC),
            relC, C_COLOR, w, y) + 4;

        double relL = clamp(relative.getAvgPathLength());
        y = bar(g2, String.format("L(p)/L(0) = %.3f", relL),
            relL, L_COLOR, w, y) + 10;

        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(TEXT_FG);
        g2.drawString(String.format("C = %.4f", current.getClusteringCoefficient()),
            MARGIN, y + 13);
        double rawL = current.getAvgPathLength();
        g2.drawString(String.format("L = %.4f", Double.isInfinite(rawL) ? 0.0 : rawL),
            MARGIN, y + 27);
        } finally {
            g2.dispose();
        }
    }

    private int bar(Graphics2D g2, String label, double frac, Color fill, int w, int y) {
        int barW = w - 2 * MARGIN;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(TEXT_FG);
        g2.drawString(label, MARGIN, y + 11);
        y += 13;
        g2.setColor(BAR_BG);
        g2.fillRoundRect(MARGIN, y, barW, BAR_H, 6, 6);
        int fillW = (int) Math.round(frac * barW);
        if (fillW > 0) { g2.setColor(fill); g2.fillRoundRect(MARGIN, y, fillW, BAR_H, 6, 6); }
        g2.setColor(new Color(175, 180, 190));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(MARGIN, y, barW, BAR_H, 6, 6);
        return y + BAR_H + 2;
    }

    private static double clamp(double v) {
        if (!Double.isFinite(v)) return 1.0;
        return Math.max(0.0, Math.min(1.0, v));
    }
}
