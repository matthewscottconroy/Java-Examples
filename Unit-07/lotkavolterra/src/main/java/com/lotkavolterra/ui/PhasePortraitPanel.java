package com.lotkavolterra.ui;

import com.lotkavolterra.model.LotkaVolterra;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Phase-portrait canvas for the Lotka-Volterra system.
 *
 * <h2>Rendering</h2>
 * <p>The trajectory of the system in (x, y) — prey vs. predator — space is drawn
 * as a fading trail of up to {@value #MAX_TRAIL} points.  Older segments are
 * rendered with lower alpha so recent motion stands out.  The current state is
 * shown as a filled circle.
 *
 * <p>The non-trivial equilibrium point {@code (x*, y*) = (γ/δ, α/β)} is marked
 * with a {@code +} crosshair.
 *
 * <h2>Axes</h2>
 * <p>The plot auto-scales so both the equilibrium and the full trail remain
 * visible.  Axis labels indicate prey (horizontal) and predator (vertical).
 *
 * <p>This panel shares the same {@link LotkaVolterra} model as
 * {@link TimeSeriesPanel} but does <em>not</em> drive its own timer — the
 * {@link TimeSeriesPanel} timer advances the shared model; this panel merely
 * reads the state and appends to its own trail on each repaint triggered by a
 * separate {@link javax.swing.Timer}.
 */
public class PhasePortraitPanel extends JPanel {

    /** Maximum number of (x, y) trail points retained. */
    public static final int MAX_TRAIL = 1500;

    private static final int W = 700;
    private static final int H = 400;

    private static final int MARGIN = 50;

    private static final Color BG           = new Color(8,  10, 22);
    private static final Color GRID_COLOR   = new Color(25, 28, 45);
    private static final Color TRAIL_BASE   = new Color(100, 180, 255);
    private static final Color EQ_COLOR     = new Color(255, 220, 60);
    private static final Color DOT_COLOR    = new Color(255, 255, 255);
    private static final Color TEXT_COLOR   = new Color(180, 190, 210);
    private static final Color AXIS_COLOR   = new Color(60,  70, 100);

    private final LotkaVolterra model;
    private final Timer         repaintTimer;

    /** Fading trail of (x, y) population pairs. */
    private final Deque<double[]> trail = new ArrayDeque<>(MAX_TRAIL);

    /**
     * Construct the phase-portrait panel and start the repaint timer.
     *
     * @param model the shared Lotka-Volterra model (not null)
     */
    public PhasePortraitPanel(LotkaVolterra model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);

        repaintTimer = new Timer(16, e -> {
            appendTrail(model.getX(), model.getY());
            repaint();
        });
        repaintTimer.start();
    }

    /** Append a trail point, evicting the oldest if the buffer is full. */
    private void appendTrail(double x, double y) {
        if (trail.size() >= MAX_TRAIL) trail.pollFirst();
        trail.addLast(new double[]{x, y});
    }

    /** Clear the trail (called on reset). */
    void clearTrail() {
        trail.clear();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Compute axis bounds from trail + equilibrium
        double[] bounds = computeBounds();
        double xMin = bounds[0], xMax = bounds[1];
        double yMin = bounds[2], yMax = bounds[3];

        drawGrid(g2, xMin, xMax, yMin, yMax);
        drawTrail(g2, xMin, xMax, yMin, yMax);
        drawEquilibrium(g2, xMin, xMax, yMin, yMax);
        drawCurrentDot(g2, xMin, xMax, yMin, yMax);
        drawAxisLabels(g2);
    }

    /**
     * Compute axis bounds that include all trail points plus the equilibrium.
     *
     * @return {@code [xMin, xMax, yMin, yMax]}
     */
    private double[] computeBounds() {
        double eqX = model.equilibriumX();
        double eqY = model.equilibriumY();

        double xMin = Math.max(0.0, eqX * 0.1);
        double xMax = eqX * 2.0;
        double yMin = Math.max(0.0, eqY * 0.1);
        double yMax = eqY * 2.0;

        for (double[] pt : trail) {
            xMin = Math.min(xMin, pt[0] * 0.9);
            xMax = Math.max(xMax, pt[0] * 1.1);
            yMin = Math.min(yMin, pt[1] * 0.9);
            yMax = Math.max(yMax, pt[1] * 1.1);
        }

        // Ensure a minimum span to avoid degenerate scale
        if (xMax - xMin < 1.0) xMax = xMin + 1.0;
        if (yMax - yMin < 1.0) yMax = yMin + 1.0;

        return new double[]{xMin, xMax, yMin, yMax};
    }

    /** Map a prey value to a screen x-coordinate. */
    private int screenX(double x, double xMin, double xMax) {
        return MARGIN + (int)((x - xMin) / (xMax - xMin) * (W - 2 * MARGIN));
    }

    /** Map a predator value to a screen y-coordinate (y-axis inverted). */
    private int screenY(double y, double yMin, double yMax) {
        return H - MARGIN - (int)((y - yMin) / (yMax - yMin) * (H - 2 * MARGIN));
    }

    private void drawGrid(Graphics2D g2, double xMin, double xMax, double yMin, double yMax) {
        g2.setColor(GRID_COLOR);
        for (int x = 0; x < W; x += 50) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 50) g2.drawLine(0, y, W, y);

        g2.setColor(AXIS_COLOR);
        g2.drawLine(MARGIN, H - MARGIN, W - MARGIN, H - MARGIN);
        g2.drawLine(MARGIN, MARGIN,     MARGIN,     H - MARGIN);
    }

    private void drawTrail(Graphics2D g2, double xMin, double xMax, double yMin, double yMax) {
        List<double[]> pts = new ArrayList<>(trail);
        if (pts.size() < 2) return;

        Stroke prev = g2.getStroke();
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int n = pts.size();
        for (int i = 1; i < n; i++) {
            float alpha = (float) i / n;
            int a = (int)(alpha * 200);
            g2.setColor(new Color(TRAIL_BASE.getRed(), TRAIL_BASE.getGreen(),
                                  TRAIL_BASE.getBlue(), a));
            int sx0 = screenX(pts.get(i - 1)[0], xMin, xMax);
            int sy0 = screenY(pts.get(i - 1)[1], yMin, yMax);
            int sx1 = screenX(pts.get(i)[0],     xMin, xMax);
            int sy1 = screenY(pts.get(i)[1],     yMin, yMax);
            g2.drawLine(sx0, sy0, sx1, sy1);
        }
        g2.setStroke(prev);
    }

    private void drawEquilibrium(Graphics2D g2, double xMin, double xMax,
                                 double yMin, double yMax) {
        double eqX = model.equilibriumX();
        double eqY = model.equilibriumY();
        int sx = screenX(eqX, xMin, xMax);
        int sy = screenY(eqY, yMin, yMax);

        g2.setColor(EQ_COLOR);
        g2.setStroke(new BasicStroke(1.8f));
        int arm = 8;
        g2.drawLine(sx - arm, sy, sx + arm, sy);
        g2.drawLine(sx, sy - arm, sx, sy + arm);
        g2.setStroke(new BasicStroke());

        g2.setFont(new Font("Monospaced", Font.PLAIN, 10));
        g2.drawString(String.format("(%.1f, %.1f)", eqX, eqY), sx + 10, sy - 4);
    }

    private void drawCurrentDot(Graphics2D g2, double xMin, double xMax,
                                double yMin, double yMax) {
        int sx = screenX(model.getX(), xMin, xMax);
        int sy = screenY(model.getY(), yMin, yMax);

        g2.setColor(DOT_COLOR);
        int r = 5;
        g2.fillOval(sx - r, sy - r, r * 2, r * 2);
        g2.setColor(TRAIL_BASE);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(sx - r, sy - r, r * 2, r * 2);
        g2.setStroke(new BasicStroke());
    }

    private void drawAxisLabels(Graphics2D g2) {
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(TEXT_COLOR);
        g2.drawString("Prey x", W / 2 - 20, H - 10);

        // Rotated "Predator y" label on the left axis
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.rotate(-Math.PI / 2.0);
        g2r.drawString("Predator y", -(H / 2) - 30, 14);
        g2r.dispose();

        // Title
        g2.setFont(new Font("SansSerif", Font.BOLD, 12));
        g2.setColor(TEXT_COLOR);
        g2.drawString("Phase Portrait  (x vs y)", W / 2 - 70, 18);
    }
}
