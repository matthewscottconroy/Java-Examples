package com.pendulums.ui;

import com.pendulums.model.DoublePendulum;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulation canvas for the double pendulum.
 *
 * <p>Renders both rods and bobs plus a long fading trail for the end bob.
 * An optional second "shadow" pendulum starts with angles offset by a
 * tiny amount so you can watch the exponential divergence that signals chaos.
 *
 * <p>Click and drag to set the first arm's release angle.
 */
public class DoublePendulumPanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int W         = 700;
    private static final int H         = 500;
    private static final int PIVOT_X   = W / 2;
    private static final int PIVOT_Y   = 90;
    private static final int BOB1_R    = 14;
    private static final int BOB2_R    = 14;
    private static final int STEPS     = 12;
    private static final double FRAME_DT = 0.016;

    private static final Color BG          = new Color(8,  10, 22);
    private static final Color ROD_COLOR   = new Color(160, 170, 190);
    private static final Color BOB1_COLOR  = new Color(220, 100,  55);
    private static final Color BOB2_COLOR  = new Color(60,  160, 230);
    private static final Color PIVOT_COLOR = new Color(200, 200, 210);
    private static final Color TRAIL_COLOR = new Color(60, 160, 230);
    private static final Color SHADOW_COL  = new Color(120, 230, 100, 90);
    private static final Color TEXT_COLOR  = new Color(180, 190, 210);

    private final DoublePendulum model;
    private final Timer           gameLoop;

    boolean paused      = false;
    boolean showTrail   = true;
    boolean showShadow  = false;

    // Shadow pendulum — starts at θ₁ + ε to demonstrate chaos
    private DoublePendulum shadow;
    private static final double SHADOW_EPSILON = 1e-4;

    private boolean dragging = false;

    public DoublePendulumPanel(DoublePendulum model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);
        addMouseMotionListener(this);

        resetShadow();

        gameLoop = new Timer(16, e -> {
            if (!paused) {
                double dt = FRAME_DT / STEPS;
                for (int i = 0; i < STEPS; i++) {
                    model.step(dt);
                    if (showShadow) shadow.step(dt);
                }
                model.recordTrail(PIVOT_X, PIVOT_Y);
            }
            repaint();
        });
        gameLoop.start();
    }

    /** Create a shadow pendulum offset by SHADOW_EPSILON on θ₁. */
    void resetShadow() {
        shadow = new DoublePendulum(
                model.getTheta1() + SHADOW_EPSILON, model.getTheta2(),
                model.getLength1(), model.getLength2(),
                model.getMass1(), model.getMass2(),
                model.getGravity());
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
        if (showShadow) drawShadow(g2);
        drawPendulum(g2, model, BOB1_COLOR, BOB2_COLOR);
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
        g2.setStroke(new BasicStroke(1.2f));
        for (int i = 1; i < pts.size(); i++) {
            float alpha = (float) i / pts.size();
            g2.setColor(new Color(TRAIL_COLOR.getRed(), TRAIL_COLOR.getGreen(),
                                  TRAIL_COLOR.getBlue(), (int)(alpha * 210)));
            g2.drawLine((int) pts.get(i-1)[0], (int) pts.get(i-1)[1],
                        (int) pts.get(i)[0],   (int) pts.get(i)[1]);
        }
        g2.setStroke(prev);
    }

    private void drawShadow(Graphics2D g2) {
        double sx1 = PIVOT_X + shadow.bob1RelX();
        double sy1 = PIVOT_Y + shadow.bob1RelY();
        double sx2 = sx1 + shadow.bob2RelX();
        double sy2 = sy1 + shadow.bob2RelY();
        g2.setColor(SHADOW_COL);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(PIVOT_X, PIVOT_Y, (int)sx1, (int)sy1);
        g2.drawLine((int)sx1, (int)sy1, (int)sx2, (int)sy2);
        g2.setStroke(new BasicStroke());
        int r = BOB2_R - 4;
        g2.fillOval((int)(sx2-r), (int)(sy2-r), 2*r, 2*r);
    }

    private void drawPendulum(Graphics2D g2, DoublePendulum p, Color c1, Color c2) {
        double b1x = PIVOT_X + p.bob1RelX();
        double b1y = PIVOT_Y + p.bob1RelY();
        double b2x = b1x + p.bob2RelX();
        double b2y = b1y + p.bob2RelY();

        // Rods
        g2.setColor(ROD_COLOR);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(PIVOT_X, PIVOT_Y, (int)b1x, (int)b1y);
        g2.drawLine((int)b1x, (int)b1y, (int)b2x, (int)b2y);
        g2.setStroke(new BasicStroke());

        // Bob 2 (end — chaotic one)
        GradientPaint gp2 = new GradientPaint(
            (float)(b2x - BOB2_R), (float)(b2y - BOB2_R), c2.brighter(),
            (float)(b2x + BOB2_R), (float)(b2y + BOB2_R), c2.darker());
        g2.setPaint(gp2);
        g2.fill(new Ellipse2D.Double(b2x - BOB2_R, b2y - BOB2_R, BOB2_R*2, BOB2_R*2));
        g2.setColor(c2.brighter());
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(b2x - BOB2_R, b2y - BOB2_R, BOB2_R*2, BOB2_R*2));
        g2.setStroke(new BasicStroke());

        // Bob 1
        GradientPaint gp1 = new GradientPaint(
            (float)(b1x - BOB1_R), (float)(b1y - BOB1_R), c1.brighter(),
            (float)(b1x + BOB1_R), (float)(b1y + BOB1_R), c1.darker());
        g2.setPaint(gp1);
        g2.fill(new Ellipse2D.Double(b1x - BOB1_R, b1y - BOB1_R, BOB1_R*2, BOB1_R*2));
        g2.setColor(c1.brighter());
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(b1x - BOB1_R, b1y - BOB1_R, BOB1_R*2, BOB1_R*2));
        g2.setStroke(new BasicStroke());

        // Pivot
        g2.setColor(PIVOT_COLOR);
        g2.fillRect(PIVOT_X - 5, PIVOT_Y - 5, 10, 10);
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = 12, y = 20;
        g2.drawString(String.format("θ₁=%6.1f°  θ₂=%6.1f°",
                      Math.toDegrees(model.getTheta1()), Math.toDegrees(model.getTheta2())), x, y);
        g2.drawString(String.format("ω₁=%5.2f  ω₂=%5.2f rad/s",
                      model.getOmega1(), model.getOmega2()), x, y + 16);
        g2.drawString(String.format("E = %.1f (KE %.1f + PE %.1f)",
                      model.totalEnergy(), model.kineticEnergy(), model.potentialEnergy()), x, y + 32);
        if (paused) {
            g2.setColor(new Color(255, 200, 60));
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));
            g2.drawString("PAUSED", W - 80, 20);
        }
        if (showShadow) {
            g2.setColor(SHADOW_COL);
            g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g2.drawString("green = shadow (Δθ₁ = 1e-4 rad) — observe exponential divergence", x, H - 10);
        }
    }

    // -------------------------------------------------------------------------
    // Mouse
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
        model.reset(model.getTheta1(), model.getTheta2());
        resetShadow();
    }

    private void updateAngle(MouseEvent e) {
        double dx = e.getX() - PIVOT_X;
        double dy = e.getY() - PIVOT_Y;
        double angle = Math.atan2(dx, dy);
        model.setTheta1(angle);
        model.setTheta2(angle * 0.7);
    }

    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}
}
