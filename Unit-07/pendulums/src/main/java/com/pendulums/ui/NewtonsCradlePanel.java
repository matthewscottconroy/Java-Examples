package com.pendulums.ui;

import com.pendulums.model.NewtonsCradle;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * Simulation canvas for Newton's Cradle.
 *
 * <p>Renders the cradle frame, suspension strings, and metallic balls.
 * Click and drag any ball leftward to pull it back; release to drop.
 * The number of balls lifted is determined by how many are dragged past
 * the equilibrium line.
 */
public class NewtonsCradlePanel extends JPanel implements MouseListener, MouseMotionListener {

    private static final int W          = 700;
    private static final int H          = 500;
    private static final int STEPS      = 16;
    private static final double FRAME_DT = 0.016;

    private static final Color BG          = new Color(8, 10, 22);
    private static final Color FRAME_COLOR = new Color(70, 80, 100);
    private static final Color STRING_COL  = new Color(160, 165, 180);
    private static final Color BALL_BASE   = new Color(180, 190, 205);
    private static final Color TEXT_COLOR  = new Color(180, 190, 210);
    private static final Color GROUND_COL  = new Color(30, 34, 52);

    private final NewtonsCradle model;
    private final Timer         gameLoop;

    // Frame geometry — recomputed when ball count changes
    private double pivotY;
    private double pivotX0;

    private int    dragIndex  = -1;
    private double dragTheta0 = 0;

    NewtonsCradlePanel(NewtonsCradle model) {
        this.model = model;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);
        addMouseMotionListener(this);

        recomputeGeometry();

        gameLoop = new Timer(16, e -> {
            double dt = FRAME_DT / STEPS;
            for (int i = 0; i < STEPS; i++) model.step(dt);
            repaint();
        });
        gameLoop.start();
    }

    /** Recompute pivot positions based on current ball count and panel size. */
    void recomputeGeometry() {
        int n = model.getBallCount();
        pivotY  = 90.0;
        pivotX0 = (W - (n - 1) * model.pivotSpacing) / 2.0;
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawFrame(g2);
        drawStringsAndBalls(g2);
        drawHud(g2);
    }

    private void drawBackground(Graphics2D g2) {
        // Subtle floor
        int floorY = (int)(pivotY + model.getLength() + model.getBallRadius() + 18);
        g2.setColor(GROUND_COL);
        g2.fillRect(0, floorY, W, H - floorY);
        g2.setColor(new Color(55, 60, 80));
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(0, floorY, W, floorY);
        g2.setStroke(new BasicStroke());
    }

    private void drawFrame(Graphics2D g2) {
        int n = model.getBallCount();
        double leftPivotX  = pivotX0;
        double rightPivotX = pivotX0 + (n - 1) * model.pivotSpacing;

        // Extension beyond outermost balls
        double extend = model.getBallRadius() * 3;
        double frameLeft  = leftPivotX  - extend;
        double frameRight = rightPivotX + extend;

        int topY     = (int) pivotY - 30;
        int bottomY  = (int) pivotY;

        // Top horizontal bar
        g2.setColor(FRAME_COLOR);
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) frameLeft, topY, (int) frameRight, topY);

        // Vertical supports
        int legH = (int)(model.getLength() + model.getBallRadius() * 2 + 30);
        g2.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) frameLeft,  topY, (int) frameLeft,  topY + legH);
        g2.drawLine((int) frameRight, topY, (int) frameRight, topY + legH);

        // Bottom horizontal connector
        g2.drawLine((int) frameLeft, topY + legH, (int) frameRight, topY + legH);

        // Front horizontal bar (at pivot level)
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine((int) frameLeft, bottomY, (int) frameRight, bottomY);

        g2.setStroke(new BasicStroke());
    }

    private void drawStringsAndBalls(Graphics2D g2) {
        int n = model.getBallCount();
        double r = model.getBallRadius();

        for (int i = 0; i < n; i++) {
            double bx = model.ballScreenX(i, pivotX0);
            double by = model.ballScreenY(i, pivotY);
            double px = model.pivotScreenX(i, pivotX0);

            // String
            g2.setColor(STRING_COL);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawLine((int) px, (int) pivotY, (int) bx, (int) by);
            g2.setStroke(new BasicStroke());

            // Ball — metallic gradient
            GradientPaint gp = new GradientPaint(
                (float)(bx - r * 0.4), (float)(by - r * 0.4), BALL_BASE.brighter(),
                (float)(bx + r),       (float)(by + r),       BALL_BASE.darker().darker());
            g2.setPaint(gp);
            g2.fill(new Ellipse2D.Double(bx - r, by - r, r * 2, r * 2));

            // Highlight
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fill(new Ellipse2D.Double(bx - r * 0.55, by - r * 0.65, r * 0.6, r * 0.4));

            // Outline
            g2.setColor(BALL_BASE.darker());
            g2.setStroke(new BasicStroke(1.2f));
            g2.draw(new Ellipse2D.Double(bx - r, by - r, r * 2, r * 2));
            g2.setStroke(new BasicStroke());
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = 12, y = 20;
        g2.drawString(String.format("E = %.1f  (KE %.1f + PE %.1f)",
                      model.totalEnergy(), model.kineticEnergy(), model.potentialEnergy()), x, y);
        g2.drawString(String.format("Restitution e = %.2f", model.getRestitution()), x, y + 16);
        g2.drawString(String.format("Balls: %d", model.getBallCount()), x, y + 32);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 11));
        g2.setColor(new Color(150, 160, 180));
        g2.drawString("Drag a ball left to lift it, release to drop", x, H - 10);
    }

    // -------------------------------------------------------------------------
    // Mouse — drag any ball to the left to set its initial angle
    // -------------------------------------------------------------------------

    @Override
    public void mousePressed(MouseEvent e) {
        int n = model.getBallCount();
        double r = model.getBallRadius();
        for (int i = 0; i < n; i++) {
            double bx = model.ballScreenX(i, pivotX0);
            double by = model.ballScreenY(i, pivotY);
            if (Math.hypot(e.getX() - bx, e.getY() - by) <= r + 4) {
                dragIndex  = i;
                dragTheta0 = model.getTheta(i);
                break;
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragIndex < 0) return;
        double px = model.pivotScreenX(dragIndex, pivotX0);
        double dx = e.getX() - px;
        double dy = e.getY() - pivotY;
        double angle = Math.atan2(dx, dy);
        // Only allow pulling to the left (negative angle) for intuitive feel
        if (angle > 0) angle = 0;
        // Direct-set; model handles this safely
        setAllOnSide(dragIndex, angle);
    }

    private void setAllOnSide(int pivotIndex, double angle) {
        // Lift the dragged ball and all adjacent ones still in contact on that side
        model.reset((int) 1, angle);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragIndex = -1;
    }

    @Override public void mouseClicked(MouseEvent e)  {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}
    @Override public void mouseMoved(MouseEvent e)    {}
}
