package com.pendulums.ui;

import com.pendulums.model.SimplePendulum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation canvas for the simple pendulum.
 *
 * <p>Renders the pivot, rod, bob, and a fading position trail.
 * An optional ghost pendulum using the small-angle approximation is drawn
 * alongside the exact simulation so you can see the period difference at
 * large amplitudes.
 *
 * <p>Click and drag on the canvas to set the release angle interactively.
 */
public class SimplePendulumPanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int W = 700;
    private static final int H = 500;

    private static final int    PIVOT_X   = W / 2;
    private static final int    PIVOT_Y   = 90;
    private static final int    BOB_R     = 18;
    private static final int    PIVOT_R   = 6;
    private static final int    STEPS     = 8;   // physics substeps per frame
    private static final double FRAME_DT  = 0.016;

    private static final Color BG           = new Color(8,  10, 22);
    private static final Color PIVOT_COLOR  = new Color(200, 200, 210);
    private static final Color ROD_COLOR    = new Color(160, 170, 190);
    private static final Color BOB_COLOR    = new Color(220, 100,  55);
    private static final Color GHOST_COLOR  = new Color(60, 180, 120, 120);
    private static final Color TEXT_COLOR   = new Color(180, 190, 210);

    private final SimplePendulum model;
    private final Timer           gameLoop;

    boolean paused      = false;
    boolean showTrail   = true;
    boolean showGhost   = true;

    // ghost pendulum — small-angle approximation (pure SHM)
    private double ghostTheta;
    private double ghostOmega;
    private double ghostTime;

    private boolean dragging = false;

    public SimplePendulumPanel(SimplePendulum model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);
        addMouseMotionListener(this);

        syncGhost();

        gameLoop = new Timer(16, e -> {
            if (!paused) {
                double dt = FRAME_DT / STEPS;
                for (int i = 0; i < STEPS; i++) {
                    model.step(dt);
                    stepGhost(dt);
                }
                model.recordTrail(PIVOT_X, PIVOT_Y);
            }
            repaint();
        });
        gameLoop.start();
    }

    /** Re-synchronise the ghost (SHM) pendulum with the current model angle. */
    void syncGhost() {
        ghostTheta = model.getTheta();
        ghostOmega = 0.0;
        ghostTime  = 0.0;
    }

    private void stepGhost(double dt) {
        if (!showGhost) return;
        double omega0 = Math.sqrt(model.getGravity() / model.getLength());
        ghostTime += dt;
        ghostTheta = model.getTheta() * Math.cos(omega0 * ghostTime)
                   + (ghostOmega / omega0) * Math.sin(omega0 * ghostTime);
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
        if (showTrail)  drawTrail(g2);
        if (showGhost)  drawGhost(g2);
        drawPendulum(g2);
        drawHud(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(25, 28, 45));
        for (int x = 0; x < W; x += 50) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 50) g2.drawLine(0, y, W, y);
    }

    private void drawTrail(Graphics2D g2) {
        List<double[]> pts = new ArrayList<>(model.getTrail());
        if (pts.size() < 2) return;
        Stroke prev = g2.getStroke();
        g2.setStroke(new BasicStroke(1.5f));
        for (int i = 1; i < pts.size(); i++) {
            float alpha = (float) i / pts.size();
            g2.setColor(new Color(220, 100, 55, (int) (alpha * 200)));
            g2.drawLine((int) pts.get(i - 1)[0], (int) pts.get(i - 1)[1],
                        (int) pts.get(i)[0],     (int) pts.get(i)[1]);
        }
        g2.setStroke(prev);
    }

    private void drawGhost(Graphics2D g2) {
        double gx = PIVOT_X + model.getLength() * Math.sin(ghostTheta);
        double gy = PIVOT_Y + model.getLength() * Math.cos(ghostTheta);
        g2.setColor(GHOST_COLOR);
        g2.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                     0, new float[]{6, 4}, 0));
        g2.drawLine(PIVOT_X, PIVOT_Y, (int) gx, (int) gy);
        g2.setStroke(new BasicStroke());
        int gr = BOB_R - 4;
        g2.fillOval((int)(gx - gr), (int)(gy - gr), 2*gr, 2*gr);
    }

    private void drawPendulum(Graphics2D g2) {
        double bx = PIVOT_X + model.bobRelX();
        double by = PIVOT_Y + model.bobRelY();

        // Rod
        g2.setColor(ROD_COLOR);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(PIVOT_X, PIVOT_Y, (int) bx, (int) by);
        g2.setStroke(new BasicStroke());

        // Bob
        GradientPaint gp = new GradientPaint(
            (float)(bx - BOB_R), (float)(by - BOB_R), BOB_COLOR.brighter(),
            (float)(bx + BOB_R), (float)(by + BOB_R), BOB_COLOR.darker());
        g2.setPaint(gp);
        g2.fill(new Ellipse2D.Double(bx - BOB_R, by - BOB_R, BOB_R * 2, BOB_R * 2));
        g2.setColor(BOB_COLOR.brighter());
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(bx - BOB_R, by - BOB_R, BOB_R * 2, BOB_R * 2));
        g2.setStroke(new BasicStroke());

        // Pivot
        g2.setColor(PIVOT_COLOR);
        g2.fillRect(PIVOT_X - PIVOT_R, PIVOT_Y - PIVOT_R, PIVOT_R * 2, PIVOT_R * 2);

        // Angle arc hint
        if (dragging) {
            g2.setColor(new Color(255, 255, 100, 80));
            int arcR = (int)(model.getLength() * 0.4);
            double angle = model.getTheta();
            int startDeg = 90 - (int) Math.toDegrees(Math.abs(angle));
            int arcDeg   = (int) Math.abs(Math.toDegrees(angle));
            if (angle >= 0)
                g2.drawArc(PIVOT_X - arcR, PIVOT_Y - arcR, arcR*2, arcR*2, startDeg, arcDeg);
            else
                g2.drawArc(PIVOT_X - arcR, PIVOT_Y - arcR, arcR*2, arcR*2, 90, arcDeg);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = 12, y = 20;
        double t = model.totalEnergy();
        g2.drawString(String.format("θ = %6.2f°", Math.toDegrees(model.getTheta())), x, y);
        g2.drawString(String.format("ω = %6.3f rad/s", model.getOmega()), x, y + 16);
        g2.drawString(String.format("E = %8.1f  (KE %6.1f + PE %6.1f)",
                                    t, model.kineticEnergy(), model.potentialEnergy()), x, y + 32);
        g2.drawString(String.format("T₀ = %.3f s  (small-angle)", model.smallAnglePeriod()), x, y + 48);
        g2.drawString(model.getMethod().name(), x, y + 64);

        if (paused) {
            g2.setColor(new Color(255, 200, 60));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("PAUSED", W - 80, 20);
        }
        if (showGhost) {
            g2.setColor(GHOST_COLOR);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.drawString("green dashed = SHM approximation", x, H - 10);
        }
    }

    // -------------------------------------------------------------------------
    // Mouse — drag to set angle
    // -------------------------------------------------------------------------

    @Override public void mousePressed(MouseEvent e) {
        dragging = true;
        paused   = true;
        updateAngle(e);
    }

    @Override public void mouseDragged(MouseEvent e) { updateAngle(e); }

    @Override public void mouseReleased(MouseEvent e) {
        dragging = false;
        paused   = false;
        model.setOmega(0.0);
        syncGhost();
        model.getTrail().clear();
    }

    private void updateAngle(MouseEvent e) {
        double dx = e.getX() - PIVOT_X;
        double dy = e.getY() - PIVOT_Y;
        double angle = Math.atan2(dx, dy);
        model.setTheta(angle);
    }

    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}
}
