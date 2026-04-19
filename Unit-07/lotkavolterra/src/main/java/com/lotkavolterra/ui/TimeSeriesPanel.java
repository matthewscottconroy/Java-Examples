package com.lotkavolterra.ui;

import com.lotkavolterra.model.LotkaVolterra;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Scrolling time-series chart for prey, predator, and conserved-quantity histories.
 *
 * <h2>Rendering</h2>
 * <ul>
 *   <li>Prey x(t)   — drawn in {@code Color(80, 200, 120)} (green)</li>
 *   <li>Predator y(t) — drawn in {@code Color(220, 120, 60)} (orange)</li>
 *   <li>Conserved quantity V(t) normalised to [0,1] — drawn in
 *       semi-transparent {@code Color(200, 200, 80, 120)} (yellow)</li>
 * </ul>
 *
 * <p>The most recent {@value #HISTORY} frames are retained.  Older values scroll
 * off the left edge.  Population values are auto-scaled vertically to the maximum
 * value seen in the current window so the chart never clips.
 *
 * <p>A HUD in the top-left corner shows current x, y, V, and the R₀ analogue α/γ.
 */
public class TimeSeriesPanel extends JPanel {

    /** Number of frames retained in the scrolling history. */
    public static final int HISTORY = 600;

    private static final int W = 700;
    private static final int H = 400;

    private static final Color BG         = new Color(8,  10, 22);
    private static final Color GRID_COLOR = new Color(25, 28, 45);
    private static final Color PREY_COLOR = new Color(80,  200, 120);
    private static final Color PRED_COLOR = new Color(220, 120,  60);
    private static final Color LYA_COLOR  = new Color(200, 200,  80, 120);
    private static final Color TEXT_COLOR = new Color(180, 190, 210);
    private static final Color AXIS_COLOR = new Color(60,  70, 100);

    private final LotkaVolterra model;
    private final Timer         gameLoop;

    /** Rolling history of prey values. */
    private final Deque<Double> preyHistory  = new ArrayDeque<>(HISTORY);
    /** Rolling history of predator values. */
    private final Deque<Double> predHistory  = new ArrayDeque<>(HISTORY);
    /** Rolling history of conserved-quantity values. */
    private final Deque<Double> lyaHistory   = new ArrayDeque<>(HISTORY);

    /**
     * Construct the time-series panel and start the animation loop.
     *
     * @param model the shared Lotka-Volterra model (not null)
     */
    public TimeSeriesPanel(LotkaVolterra model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);

        gameLoop = new Timer(16, e -> {
            model.step();
            appendHistory(preyHistory,  model.getX());
            appendHistory(predHistory,  model.getY());
            appendHistory(lyaHistory,   model.conservedQuantity());
            repaint();
        });
        gameLoop.start();
    }

    /** Add a value to a history deque, evicting the oldest entry if full. */
    private static void appendHistory(Deque<Double> deque, double value) {
        if (deque.size() >= HISTORY) deque.pollFirst();
        deque.addLast(value);
    }

    /** Clear all history buffers (called on reset). */
    void clearHistory() {
        preyHistory.clear();
        predHistory.clear();
        lyaHistory.clear();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawSeries(g2);
        drawHud(g2);
        drawLegend(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_COLOR);
        for (int x = 0; x < W; x += 50) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 50) g2.drawLine(0, y, W, y);
        g2.setColor(AXIS_COLOR);
        g2.drawLine(0, H - 1, W, H - 1);
    }

    private void drawSeries(Graphics2D g2) {
        if (preyHistory.size() < 2) return;

        List<Double> prey = new ArrayList<>(preyHistory);
        List<Double> pred = new ArrayList<>(predHistory);
        List<Double> lya  = new ArrayList<>(lyaHistory);

        // Determine population scale (max of both population series)
        double maxPop = 1.0;
        for (double v : prey) maxPop = Math.max(maxPop, v);
        for (double v : pred) maxPop = Math.max(maxPop, v);

        // Determine Lyapunov scale
        double minLya = Double.MAX_VALUE;
        double maxLya = -Double.MAX_VALUE;
        for (double v : lya) {
            if (Double.isFinite(v)) {
                minLya = Math.min(minLya, v);
                maxLya = Math.max(maxLya, v);
            }
        }
        double lyaRange = (maxLya > minLya) ? (maxLya - minLya) : 1.0;

        int n = prey.size();
        int margin = 10;
        int plotH  = H - 2 * margin;

        Stroke prevStroke = g2.getStroke();
        g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Lyapunov (V) — drawn first so population lines render on top
        g2.setColor(LYA_COLOR);
        for (int i = 1; i < n; i++) {
            int x0 = (int)((double)(i - 1) / (HISTORY - 1) * (W - 1));
            int x1 = (int)((double) i       / (HISTORY - 1) * (W - 1));
            double v0 = lya.get(i - 1);
            double v1 = lya.get(i);
            if (!Double.isFinite(v0) || !Double.isFinite(v1)) continue;
            int y0 = margin + plotH - (int)(((v0 - minLya) / lyaRange) * plotH);
            int y1 = margin + plotH - (int)(((v1 - minLya) / lyaRange) * plotH);
            g2.drawLine(x0, y0, x1, y1);
        }

        // Prey (x) — green
        g2.setColor(PREY_COLOR);
        for (int i = 1; i < n; i++) {
            int x0 = (int)((double)(i - 1) / (HISTORY - 1) * (W - 1));
            int x1 = (int)((double) i       / (HISTORY - 1) * (W - 1));
            int y0 = margin + plotH - (int)(prey.get(i - 1) / maxPop * plotH);
            int y1 = margin + plotH - (int)(prey.get(i)     / maxPop * plotH);
            g2.drawLine(x0, y0, x1, y1);
        }

        // Predator (y) — orange
        g2.setColor(PRED_COLOR);
        for (int i = 1; i < n; i++) {
            int x0 = (int)((double)(i - 1) / (HISTORY - 1) * (W - 1));
            int x1 = (int)((double) i       / (HISTORY - 1) * (W - 1));
            int y0 = margin + plotH - (int)(pred.get(i - 1) / maxPop * plotH);
            int y1 = margin + plotH - (int)(pred.get(i)     / maxPop * plotH);
            g2.drawLine(x0, y0, x1, y1);
        }

        g2.setStroke(prevStroke);
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int tx = 12, ty = 20;
        g2.drawString(String.format("x (prey)     = %7.2f", model.getX()),  tx, ty);
        g2.drawString(String.format("y (predator) = %7.2f", model.getY()),  tx, ty + 16);
        g2.drawString(String.format("V (conserved)= %7.4f", model.conservedQuantity()), tx, ty + 32);
        g2.drawString(String.format("R\u2080 = \u03b1/\u03b3 = %5.3f",
                model.getAlpha() / model.getGamma()), tx, ty + 48);
        g2.drawString(String.format("x* = %.2f   y* = %.2f",
                model.equilibriumX(), model.equilibriumY()), tx, ty + 64);
        if (model.getHarvesting() > 0.0) {
            g2.setColor(new Color(255, 180, 60));
            g2.drawString(String.format("H (harvest) = %.3f", model.getHarvesting()), tx, ty + 80);
        }
    }

    private void drawLegend(Graphics2D g2) {
        int lx = W - 160, ly = 14;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

        g2.setColor(PREY_COLOR);
        g2.fillRect(lx, ly,     12, 3);
        g2.drawString("Prey x(t)",     lx + 16, ly + 4);

        g2.setColor(PRED_COLOR);
        g2.fillRect(lx, ly + 14, 12, 3);
        g2.drawString("Predator y(t)", lx + 16, ly + 18);

        g2.setColor(LYA_COLOR);
        g2.fillRect(lx, ly + 28, 12, 3);
        g2.drawString("V(t) (Lyapunov)", lx + 16, ly + 32);
    }
}
