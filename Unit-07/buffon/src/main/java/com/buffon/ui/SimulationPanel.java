package com.buffon.ui;

import com.buffon.model.BuffonExperiment;
import com.buffon.model.Needle;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation canvas for Buffon's Needle.
 *
 * <p>Renders the ruled floor with all dropped needles, an HUD showing live
 * statistics, and an inset convergence graph plotting the running π estimate.
 *
 * <p>A Swing {@link Timer} fires at ~60 fps; each tick drops {@link #needlesPerFrame}
 * new needles, advances the experiment, and repaints.
 */
public class SimulationPanel extends JPanel {

    static final int W = 700;
    static final int H = 520;

    private static final Color BG           = new Color(8,  10, 22);
    private static final Color LINE_COLOR   = new Color(50, 60, 85);
    private static final Color CROSS_COLOR  = new Color(220, 75, 55);
    private static final Color MISS_COLOR   = new Color(70, 110, 170);
    private static final Color TEXT_COLOR   = new Color(190, 200, 220);
    private static final Color WARN_COLOR   = new Color(255, 200, 60);
    private static final Color PI_REF_COLOR = new Color(255, 230, 60, 200);
    private static final Color GRAPH_BG     = new Color(12, 15, 28, 220);
    private static final Color GRAPH_LINE   = new Color(80, 200, 120);

    // Graph inset geometry
    private static final int GX = W - 255, GY = H - 175, GW = 245, GH = 160;
    private static final double GRAPH_Y_MIN = 2.0, GRAPH_Y_MAX = 4.5;

    private final BuffonExperiment experiment;
    private final Timer            gameLoop;

    /** Number of needles dropped per 16 ms frame. */
    int needlesPerFrame = 1;

    /** When true, the drop timer is paused. */
    boolean paused = false;

    SimulationPanel(BuffonExperiment experiment) {
        this.experiment = experiment;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);

        gameLoop = new Timer(16, e -> {
            if (!paused) {
                experiment.drop(needlesPerFrame, W, H);
            }
            repaint();
        });
        gameLoop.start();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,        RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,      RenderingHints.VALUE_STROKE_PURE);

        drawFloorLines(g2);
        drawNeedles(g2);
        drawHud(g2);
        drawConvergenceGraph(g2);
    }

    private void drawFloorLines(Graphics2D g2) {
        double spacing = experiment.getLineSpacing();
        int numLines = (int)(H / spacing) + 2;
        g2.setColor(LINE_COLOR);
        g2.setStroke(new BasicStroke(1.0f));
        for (int i = 0; i <= numLines; i++) {
            int y = (int)(i * spacing);
            g2.drawLine(0, y, W, y);
        }
        g2.setStroke(new BasicStroke());
    }

    private void drawNeedles(Graphics2D g2) {
        List<Needle> needles = new ArrayList<>(experiment.getDisplayNeedles());
        if (needles.isEmpty()) return;

        int total = needles.size();
        g2.setStroke(new BasicStroke(1.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        for (int i = 0; i < total; i++) {
            Needle n = needles.get(i);
            // Fade older needles; recent ones are fully opaque.
            int alpha = 40 + (int)(180.0 * i / total);

            Color base = n.crosses() ? CROSS_COLOR : MISS_COLOR;
            g2.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha));
            g2.draw(new Line2D.Double(n.x1(), n.y1(), n.x2(), n.y2()));
        }

        // Highlight the very last needle
        if (!needles.isEmpty()) {
            Needle last = needles.get(total - 1);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(last.crosses() ? CROSS_COLOR.brighter() : MISS_COLOR.brighter());
            g2.draw(new Line2D.Double(last.x1(), last.y1(), last.x2(), last.y2()));
        }
        g2.setStroke(new BasicStroke());
    }

    private void drawHud(Graphics2D g2) {
        int x = 12, y = 20;
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);

        long   N    = experiment.getTotalDrops();
        long   C    = experiment.getCrossings();
        double piHat = experiment.estimatePi();
        double pObs  = experiment.observedCrossingProbability();
        double pTheo = experiment.theoreticalCrossingProbability();

        // Large π estimate (prominent display)
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        String piStr = Double.isNaN(piHat)
                ? "π̂ = —"
                : String.format("π̂ = %.6f", piHat);
        g2.setColor(new Color(120, 220, 130));
        g2.drawString(piStr, x, y + 4);

        if (!Double.isNaN(piHat)) {
            double err = Math.abs(piHat - Math.PI);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
            g2.setColor(TEXT_COLOR);
            g2.drawString(String.format("error  = %.6f  (%5.3f %%)", err, 100.0 * err / Math.PI), x, y + 24);
        }

        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int row = y + 42;
        int gap = 16;
        g2.drawString(String.format("drops      N = %,d",   N),       x, row);         row += gap;
        g2.drawString(String.format("crossings  C = %,d",   C),       x, row);         row += gap;
        g2.drawString(String.format("P(cross) obs  = %.5f", pObs),    x, row);         row += gap;
        g2.drawString(String.format("P(cross) theo = %.5f", pTheo),   x, row);         row += gap;
        g2.drawString(String.format("L = %.0f px   d = %.0f px",
                      experiment.getNeedleLength(),
                      experiment.getLineSpacing()),                    x, row);         row += gap;

        // Formula reminder
        g2.setColor(new Color(140, 150, 170));
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString("π̂ = 2LN / (dC)", x, row + 6);

        // Long-needle warning
        if (experiment.isLongNeedle()) {
            g2.setColor(WARN_COLOR);
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("⚠ L > d: simple formula invalid", x, row + 22);
        }

        if (paused) {
            g2.setColor(WARN_COLOR);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("PAUSED", W - 80, 20);
        }
    }

    private void drawConvergenceGraph(Graphics2D g2) {
        // Background
        g2.setColor(GRAPH_BG);
        g2.fillRoundRect(GX - 4, GY - 4, GW + 8, GH + 8, 8, 8);
        g2.setColor(new Color(60, 70, 95));
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(GX - 4, GY - 4, GW + 8, GH + 8, 8, 8);

        // Title
        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.setColor(TEXT_COLOR);
        g2.drawString("π estimate convergence", GX, GY - 7);

        // π reference line
        int refY = graphY(Math.PI);
        g2.setColor(PI_REF_COLOR);
        g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                     1f, new float[]{5, 3}, 0));
        g2.drawLine(GX, refY, GX + GW, refY);
        g2.setStroke(new BasicStroke());
        g2.setFont(new Font("Monospaced", Font.PLAIN, 9));
        g2.drawString("π", GX + GW + 2, refY + 4);

        // y-axis tick labels
        g2.setColor(new Color(120, 130, 150));
        for (double v : new double[]{2.0, 2.5, 3.0, 3.5, 4.0, 4.5}) {
            int ty = graphY(v);
            g2.drawLine(GX - 3, ty, GX, ty);
            g2.drawString(String.format("%.1f", v), GX - 28, ty + 4);
        }

        // Plot the history
        List<Double> history = experiment.getPiHistory();
        if (history.size() < 2) return;

        g2.setColor(GRAPH_LINE);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int hSize = history.size();
        for (int i = 1; i < hSize; i++) {
            double v0 = history.get(i - 1);
            double v1 = history.get(i);
            if (Double.isNaN(v0) || Double.isNaN(v1)) continue;
            int px0 = GX + (int)((double)(i - 1) / (hSize - 1) * GW);
            int px1 = GX + (int)((double) i       / (hSize - 1) * GW);
            int py0 = graphY(v0);
            int py1 = graphY(v1);
            g2.drawLine(px0, py0, px1, py1);
        }
        g2.setStroke(new BasicStroke());
    }

    /** Map a π-estimate value to a y-pixel inside the graph inset. */
    private int graphY(double value) {
        double clamped = Math.max(GRAPH_Y_MIN, Math.min(GRAPH_Y_MAX, value));
        double frac    = (clamped - GRAPH_Y_MIN) / (GRAPH_Y_MAX - GRAPH_Y_MIN);
        return GY + GH - (int)(frac * GH);
    }
}
