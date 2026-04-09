package com.orbitaldynamics.twobody.ui;

import com.orbitaldynamics.math.Vector2D;
import com.orbitaldynamics.twobody.OrbitalElements;
import com.orbitaldynamics.twobody.TwoBodySolver;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Canvas that draws the analytical two-body orbit.
 *
 * <p>Renders:
 * <ul>
 *   <li>The full orbit path (conic section, sampled at many points)</li>
 *   <li>Both bodies at their current time position</li>
 *   <li>Center of mass (crosshair)</li>
 *   <li>Periapsis and apoapsis markers</li>
 *   <li>Velocity vectors</li>
 *   <li>Semi-major axis line</li>
 * </ul>
 *
 * <p>Scale and pan are manual for simplicity (auto-fit via {@link #fitToWindow}).
 */
public final class TwoBodyPanel extends JPanel {

    private static final Color BG        = new Color(10, 12, 22);
    private static final Color GRID      = new Color(25, 30, 50);
    private static final Color ORBIT1    = new Color(80, 130, 255, 180);
    private static final Color ORBIT2    = new Color(255, 160, 60, 180);
    private static final Color BODY1     = new Color(100, 160, 255);
    private static final Color BODY2     = new Color(255, 180, 80);
    private static final Color COM_COLOR = new Color(200, 200, 200, 120);
    private static final Color PERI_COL  = new Color(100, 255, 160, 160);
    private static final Color AXIS_COL  = new Color(60, 80, 120);
    private static final Color VEL_COL   = new Color(200, 255, 200, 200);
    private static final Font  LABEL_F   = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

    // Current analytical state
    private OrbitalElements elements;
    private double m1, m2, G;
    private Vector2D comPos = Vector2D.ZERO;

    // Time animation
    private double currentTime = 0.0;
    private double timeStep    = 0.5;  // seconds per animation frame
    private boolean running    = false;
    private javax.swing.Timer animTimer;

    // Camera
    private double scale   = 1.0;
    private double offsetX = 0, offsetY = 0;

    // Orbit trace points
    private List<Vector2D> orbit1Points = new ArrayList<>();
    private List<Vector2D> orbit2Points = new ArrayList<>();

    // Orbit sample count
    private static final int ORBIT_SAMPLES = 500;

    public TwoBodyPanel() {
        setBackground(BG);
        setPreferredSize(new Dimension(700, 600));

        animTimer = new javax.swing.Timer(30, e -> {
            currentTime += timeStep;
            repaint();
        });
    }

    // -------------------------------------------------------------------------
    // Data binding
    // -------------------------------------------------------------------------

    /**
     * Updates the orbital elements and recomputes the orbit trace.
     * Resets time to zero.
     */
    public void setOrbit(OrbitalElements el, double m1, double m2, double G, Vector2D com) {
        this.elements = el;
        this.m1 = m1;
        this.m2 = m2;
        this.G  = G;
        this.comPos = com;
        this.currentTime = 0.0;
        recomputeOrbits();
        fitToWindow();
        repaint();
    }

    public void setCurrentTime(double t) {
        this.currentTime = t;
        repaint();
    }

    public double getCurrentTime() { return currentTime; }

    public void setTimeStep(double dt) { this.timeStep = dt; }
    public double getTimeStep()        { return timeStep; }

    public void setRunning(boolean run) {
        running = run;
        if (run) animTimer.start();
        else     animTimer.stop();
    }

    public boolean isRunning() { return running; }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setColor(BG);
        g2.fillRect(0, 0, getWidth(), getHeight());

        drawGrid(g2);

        if (elements == null) {
            g2.setColor(new Color(100, 120, 160));
            g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            g2.drawString("Set initial conditions to compute orbit", 60, getHeight() / 2);
            g2.dispose();
            return;
        }

        drawOrbits(g2);
        drawCoM(g2);
        drawPeriapsis(g2);
        drawBodies(g2);
        drawInfo(g2);

        g2.dispose();
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID);
        g2.setStroke(new BasicStroke(0.5f));
        int spacing = (int) (100 * scale);
        if (spacing < 20) spacing = 20;
        int cx = (int) (getWidth() / 2.0 + offsetX);
        int cy = (int) (getHeight() / 2.0 + offsetY);

        for (int x = cx % spacing; x < getWidth(); x += spacing) {
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int y = cy % spacing; y < getHeight(); y += spacing) {
            g2.drawLine(0, y, getWidth(), y);
        }

        // Axes
        g2.setColor(new Color(40, 50, 80));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(cx, 0, cx, getHeight());
        g2.drawLine(0, cy, getWidth(), cy);
    }

    private void drawOrbits(Graphics2D g2) {
        if (orbit1Points.isEmpty()) return;

        drawPath(g2, orbit1Points, ORBIT1, 1.5f);
        drawPath(g2, orbit2Points, ORBIT2, 1.5f);
    }

    private void drawPath(Graphics2D g2, List<Vector2D> pts, Color col, float width) {
        if (pts.size() < 2) return;
        g2.setColor(col);
        g2.setStroke(new BasicStroke(width));
        Path2D path = new Path2D.Double();
        boolean first = true;
        for (Vector2D p : pts) {
            int[] sc = worldToScreen(p);
            if (first) { path.moveTo(sc[0], sc[1]); first = false; }
            else        path.lineTo(sc[0], sc[1]);
        }
        if (elements.isBound()) path.closePath();
        g2.draw(path);
    }

    private void drawCoM(Graphics2D g2) {
        int[] sc = worldToScreen(comPos);
        g2.setColor(COM_COLOR);
        g2.setStroke(new BasicStroke(1f));
        int s = 8;
        g2.drawLine(sc[0] - s, sc[1], sc[0] + s, sc[1]);
        g2.drawLine(sc[0], sc[1] - s, sc[0], sc[1] + s);
        g2.drawOval(sc[0] - 3, sc[1] - 3, 6, 6);
    }

    private void drawPeriapsis(Graphics2D g2) {
        // Periapsis marker
        double peri = elements.periapsis();
        double omega = elements.periapsisAngle();
        Vector2D periPos = comPos.add(new Vector2D(
            peri * Math.cos(omega), peri * Math.sin(omega)));

        // For two-body: relative position, then map to absolute
        // Actually draw both periapsis positions
        double[] relPeri = {peri * Math.cos(omega), peri * Math.sin(omega)};
        Vector2D relPeriVec = new Vector2D(relPeri[0], relPeri[1]);
        Vector2D[] abs = TwoBodySolver.absolutePositions(comPos, relPeriVec, m1, m2);

        g2.setColor(PERI_COL);
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            0, new float[]{4, 4}, 0));

        // Draw semi-major axis line if bound
        if (elements.isBound()) {
            double apo = elements.apoapsis();
            Vector2D apoRel = new Vector2D(-apo * Math.cos(omega), -apo * Math.sin(omega));
            Vector2D[] absApo = TwoBodySolver.absolutePositions(comPos, apoRel, m1, m2);
            int[] s1 = worldToScreen(abs[0]);
            int[] s2 = worldToScreen(absApo[0]);
            g2.setColor(AXIS_COL);
            g2.drawLine(s1[0], s1[1], s2[0], s2[1]);
        }

        // Periapsis dot
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(PERI_COL);
        for (Vector2D ap : abs) {
            int[] sc = worldToScreen(ap);
            g2.drawOval(sc[0] - 4, sc[1] - 4, 8, 8);
        }
    }

    private void drawBodies(Graphics2D g2) {
        if (elements == null) return;

        Vector2D relPos = TwoBodySolver.positionAtTime(elements, currentTime);
        Vector2D relVel = TwoBodySolver.velocityAtTime(elements, currentTime);
        Vector2D[] abs  = TwoBodySolver.absolutePositions(comPos, relPos, m1, m2);
        Vector2D[] absV = {
            relVel.scale(-m2 / (m1 + m2)),
            relVel.scale( m1 / (m1 + m2))
        };

        drawBody(g2, abs[0], absV[0], BODY1, "Body 1", m1);
        drawBody(g2, abs[1], absV[1], BODY2, "Body 2", m2);
    }

    private void drawBody(Graphics2D g2, Vector2D pos, Vector2D vel, Color col, String name, double mass) {
        int r = Math.max(6, (int) (Math.sqrt(mass / 5000.0) * 20 * scale));
        r = Math.min(r, 40);
        int[] sc = worldToScreen(pos);

        // Glow
        float[] dist = {0f, 1f};
        Color[] cols = {col, new Color(col.getRed(), col.getGreen(), col.getBlue(), 0)};
        RadialGradientPaint glow = new RadialGradientPaint(sc[0], sc[1], r + 8, dist, cols);
        g2.setPaint(glow);
        g2.fillOval(sc[0] - r - 8, sc[1] - r - 8, 2 * (r + 8), 2 * (r + 8));

        // Body
        g2.setColor(col);
        g2.fillOval(sc[0] - r, sc[1] - r, 2 * r, 2 * r);
        g2.setColor(col.brighter());
        g2.drawOval(sc[0] - r, sc[1] - r, 2 * r, 2 * r);

        // Velocity arrow
        drawArrow(g2, sc[0], sc[1], vel, VEL_COL, 0.3);

        // Label
        g2.setFont(LABEL_F);
        g2.setColor(col.brighter());
        g2.drawString(name, sc[0] + r + 4, sc[1] - 4);
    }

    private void drawArrow(Graphics2D g2, int ox, int oy, Vector2D vel, Color col, double arrowScale) {
        double vx = vel.x() * arrowScale * scale;
        double vy = vel.y() * arrowScale * scale;
        int ex = (int) (ox + vx);
        int ey = (int) (oy + vy);

        g2.setColor(col);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(ox, oy, ex, ey);

        // Arrowhead
        double angle = Math.atan2(vy, vx);
        int ah = 8;
        g2.drawLine(ex, ey,
            (int)(ex - ah * Math.cos(angle - 0.4)),
            (int)(ey - ah * Math.sin(angle - 0.4)));
        g2.drawLine(ex, ey,
            (int)(ex - ah * Math.cos(angle + 0.4)),
            (int)(ey - ah * Math.sin(angle + 0.4)));
    }

    private void drawInfo(Graphics2D g2) {
        g2.setFont(LABEL_F);
        g2.setColor(new Color(120, 140, 180));
        String tStr = String.format("t = %.2f s", currentTime);
        if (elements.isBound() && !Double.isNaN(elements.period())) {
            double phase = (currentTime % elements.period()) / elements.period();
            tStr += String.format("  (%.1f%%  T)", phase * 100);
        }
        g2.drawString(tStr, 10, 16);
        g2.drawString(String.format("scale: %.3f", scale), 10, 30);
    }

    // -------------------------------------------------------------------------
    // Orbit precomputation
    // -------------------------------------------------------------------------

    private void recomputeOrbits() {
        orbit1Points.clear();
        orbit2Points.clear();
        if (elements == null) return;

        double tEnd;
        if (elements.isBound()) {
            tEnd = elements.period();
        } else {
            // For unbound orbits, sample a window around periapsis
            tEnd = 3.0 * Math.sqrt(2.0 * elements.periapsis() * elements.periapsis() * elements.periapsis() / elements.mu());
        }

        double tStart = elements.isBound() ? 0 : -tEnd / 2.0;
        double dt = (tEnd - tStart) / ORBIT_SAMPLES;

        for (int i = 0; i <= ORBIT_SAMPLES; i++) {
            double t = tStart + i * dt;
            try {
                Vector2D relPos = TwoBodySolver.positionAtTime(elements, t);
                Vector2D[] abs  = TwoBodySolver.absolutePositions(comPos, relPos, m1, m2);
                orbit1Points.add(abs[0]);
                orbit2Points.add(abs[1]);
            } catch (Exception ex) {
                // skip divergent points (e.g., near parabolic singularity)
            }
        }
    }

    // -------------------------------------------------------------------------
    // Camera
    // -------------------------------------------------------------------------

    public void fitToWindow() {
        if (orbit1Points.isEmpty()) {
            scale = 1.0;
            offsetX = 0;
            offsetY = 0;
            return;
        }

        // Find bounding box of all orbit points
        double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
        double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
        for (Vector2D p : orbit1Points) {
            minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
        }
        for (Vector2D p : orbit2Points) {
            minX = Math.min(minX, p.x()); maxX = Math.max(maxX, p.x());
            minY = Math.min(minY, p.y()); maxY = Math.max(maxY, p.y());
        }

        double worldW = maxX - minX;
        double worldH = maxY - minY;
        if (worldW < 1) worldW = 1;
        if (worldH < 1) worldH = 1;

        int w = getWidth()  > 0 ? getWidth()  : 700;
        int h = getHeight() > 0 ? getHeight() : 600;

        scale = Math.min((w - 80) / worldW, (h - 80) / worldH);
        scale = Math.max(0.01, Math.min(scale, 50.0));

        double centerX = (minX + maxX) / 2.0;
        double centerY = (minY + maxY) / 2.0;
        offsetX = -centerX * scale;
        offsetY = -centerY * scale;
    }

    private int[] worldToScreen(Vector2D w) {
        return new int[]{
            (int) Math.round(w.x() * scale + getWidth()  / 2.0 + offsetX),
            (int) Math.round(w.y() * scale + getHeight() / 2.0 + offsetY)
        };
    }
}
