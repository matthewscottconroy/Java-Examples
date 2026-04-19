package com.bifurcation.ui;

import com.bifurcation.model.LorenzAttractor;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation canvas for the Lorenz attractor.
 *
 * <h2>Rendering</h2>
 * <p>The x-z projection is rendered (the classic "butterfly wings" view). A long
 * fading trail shows the recent trajectory history; trail color encodes speed
 * (magnitude of the state-space velocity vector): slow segments are blue
 * ({@code hue ≈ 0.65}) and fast segments are red ({@code hue ≈ 0.0}).
 *
 * <h2>Shadow Trajectory</h2>
 * <p>A second trajectory (in orange) is drawn alongside the primary one. It
 * starts with an initial condition offset by just {@code 1e-5} — but it diverges
 * exponentially, visually demonstrating the sensitive dependence on initial
 * conditions that is the hallmark of chaos.
 *
 * <h2>Timer</h2>
 * <p>The simulation advances 20 substeps per frame at {@code dt = 0.005} each
 * (total simulated time ≈ 0.1 s per frame), giving smooth animation at 60 fps.
 */
public class LorenzPanel extends JPanel {

    static final int W = 700;
    static final int H = 500;

    private static final int    SUBSTEPS = 20;
    private static final double DT       = 0.005;

    private static final Color BG         = new Color(8, 10, 22);
    private static final Color TEXT_COLOR = new Color(180, 190, 210);
    private static final Color GRID_COLOR = new Color(20, 24, 40);

    private final LorenzAttractor model;
    private final Timer           gameLoop;

    // Previous state for speed computation (x, z only)
    private double prevX, prevZ;

    public LorenzPanel(LorenzAttractor model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);

        this.prevX = model.getX();
        this.prevZ = model.getZ();

        gameLoop = new Timer(16, e -> {
            for (int i = 0; i < SUBSTEPS; i++) {
                model.step(DT);
                model.recordTrail();
            }
            repaint();
        });
        gameLoop.start();
    }

    /** Stop the animation timer. */
    public void stopTimer() { gameLoop.stop(); }

    /** Restart the animation timer. */
    public void startTimer() { gameLoop.start(); }

    // -------------------------------------------------------------------------
    // Coordinate mapping
    // -------------------------------------------------------------------------

    /**
     * Map Lorenz x coordinate to panel pixel x.
     *
     * <p>The Lorenz x range is approximately [−20, 20]; we map it to [20, W−20].
     *
     * @param lx Lorenz x value
     * @return   screen pixel x
     */
    private int toScreenX(double lx) {
        return (int)(W / 2.0 + (lx / 30.0) * (W / 2.0 - 20));
    }

    /**
     * Map Lorenz z coordinate to panel pixel y.
     *
     * <p>The Lorenz z range is approximately [0, 60]; we map it to [H−20, 20]
     * (flipped because screen y increases downward).
     *
     * @param lz Lorenz z value
     * @return   screen pixel y
     */
    private int toScreenY(double lz) {
        return (int)(H - 20 - (lz / 60.0) * (H - 40));
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        drawGrid(g2);
        drawTrail(g2, new ArrayList<>(model.getTrail()), false);
        drawTrail(g2, new ArrayList<>(model.getShadowTrail()), true);
        drawCurrentPoint(g2);
        drawHud(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_COLOR);
        for (int x = 0; x < W; x += 50) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 50) g2.drawLine(0, y, W, y);
    }

    /**
     * Draw a fading trail with color encoding speed (for primary) or orange (for shadow).
     *
     * @param g2     graphics context
     * @param pts    list of [x, y, z] trail points
     * @param shadow if {@code true} draw in orange range (shadow trajectory)
     */
    private void drawTrail(Graphics2D g2, List<double[]> pts, boolean shadow) {
        if (pts.size() < 2) return;
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        int n = pts.size();
        for (int i = 1; i < n; i++) {
            double[] a = pts.get(i - 1);
            double[] b = pts.get(i);

            // Fade alpha by age: oldest segments are nearly transparent
            float ageFraction = (float) i / n;
            int alpha = (int)(ageFraction * 220);

            Color c;
            if (shadow) {
                // Shadow trajectory: orange-yellow range
                float hue = 0.08f + ageFraction * 0.04f;
                c = Color.getHSBColor(hue, 0.9f, 0.9f);
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
            } else {
                // Primary: color by speed (|dz/dt| as proxy for speed)
                double speed = Math.abs(b[2] - a[2]) / DT;
                float  speedNorm = (float) Math.min(1.0, speed / 150.0);
                // hue: 0.65 (blue) for slow, 0.0 (red) for fast
                float hue = 0.65f * (1.0f - speedNorm);
                c = Color.getHSBColor(hue, 0.95f, 0.9f);
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
            }

            g2.setColor(c);
            g2.drawLine(toScreenX(a[0]), toScreenY(a[2]),
                        toScreenX(b[0]), toScreenY(b[2]));
        }

        g2.setStroke(new BasicStroke());
    }

    private void drawCurrentPoint(Graphics2D g2) {
        int px = toScreenX(model.getX());
        int py = toScreenY(model.getZ());
        g2.setColor(Color.WHITE);
        g2.fill(new Ellipse2D.Double(px - 3, py - 3, 6, 6));

        // Shadow current point
        int spx = toScreenX(model.getShadowX());
        int spy = toScreenY(model.getShadowZ());
        g2.setColor(new Color(255, 160, 40));
        g2.fill(new Ellipse2D.Double(spx - 3, spy - 3, 6, 6));
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = 12, y = 20;
        g2.drawString(String.format("x = %8.3f", model.getX()), x, y);
        g2.drawString(String.format("y = %8.3f", model.getY()), x, y + 16);
        g2.drawString(String.format("z = %8.3f", model.getZ()), x, y + 32);
        g2.drawString(String.format("σ = %.2f  ρ = %.2f  β = %.4f",
                model.getSigma(), model.getRho(), model.getBeta()), x, y + 52);

        double div = model.divergence();
        Color divColor = div < 1.0 ? new Color(100, 220, 100) : new Color(255, 100, 60);
        g2.setColor(divColor);
        g2.drawString(String.format("|Δ| = %.4e", div), x, y + 72);

        g2.setColor(TEXT_COLOR);
        g2.drawString("Trail colors: blue=slow, red=fast (primary)", x, y + 92);
        g2.setColor(new Color(255, 160, 40));
        g2.drawString("Orange = shadow trajectory (offset 1e-5)", x, y + 108);

        // Title label
        g2.setColor(new Color(160, 200, 255, 180));
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        g2.drawString("Lorenz Attractor  (x-z projection)", W / 2 - 120, 18);
    }
}
