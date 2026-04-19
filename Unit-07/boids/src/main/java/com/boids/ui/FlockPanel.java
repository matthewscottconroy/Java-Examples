package com.boids.ui;

import com.boids.model.Boid;
import com.boids.model.FlockSimulation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * Simulation canvas for the Boids flocking model.
 *
 * <h2>Rendering</h2>
 * <p>Each boid is drawn as a small filled triangle pointing in its heading
 * direction.  The fill colour is derived from the heading angle mapped to HSB
 * hue, giving the flock a rainbow appearance as boids bank and turn.  A
 * predator (when active) is rendered as a bold red X.</p>
 *
 * <h2>Animation</h2>
 * <p>A 16 ms Swing {@link Timer} drives the loop.  Three physics substeps are
 * executed per frame so the effective simulation rate is approximately
 * 3 × 62.5 ≈ 188 Hz, giving smooth, stable flocking at the default parameters.</p>
 *
 * <h2>Interaction</h2>
 * <ul>
 *   <li><b>Right-click</b> — place / move the predator at the cursor position.</li>
 *   <li><b>Left-click</b> — add a burst of five boids near the cursor.</li>
 * </ul>
 */
public class FlockPanel extends JPanel implements MouseListener {

    // -------------------------------------------------------------------------
    // Layout constants
    // -------------------------------------------------------------------------

    static final int W = 700;
    static final int H = 500;

    // -------------------------------------------------------------------------
    // Physics constants
    // -------------------------------------------------------------------------

    private static final int    SUBSTEPS  = 3;
    private static final double FRAME_DT  = 0.016;
    private static final double SUB_DT    = FRAME_DT / SUBSTEPS;

    // -------------------------------------------------------------------------
    // Rendering constants
    // -------------------------------------------------------------------------

    private static final Color BG          = new Color(8, 10, 22);
    private static final Color GRID_COLOR  = new Color(22, 26, 45);
    private static final Color TEXT_COLOR  = new Color(180, 190, 210);
    private static final Color PREDATOR_COLOR = new Color(220, 40, 40);

    // Triangle geometry: front tip and two rear vertices
    private static final double TIP_R  = 7.0;
    private static final double TAIL_R = 4.0;
    private static final double TAIL_A = 2.4;  // wing half-angle (radians)

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final FlockSimulation sim;
    private final Timer           gameLoop;

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Create a flock panel bound to the given simulation.
     *
     * @param sim the {@link FlockSimulation} to render and advance
     */
    public FlockPanel(FlockSimulation sim) {
        this.sim = sim;
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);
        addMouseListener(this);

        gameLoop = new Timer(16, e -> {
            for (int i = 0; i < SUBSTEPS; i++) {
                sim.step(SUB_DT);
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
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2);
        drawBoids(g2);
        drawPredator(g2);
        drawHud(g2);
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(GRID_COLOR);
        for (int x = 0; x < W; x += 50) g2.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 50) g2.drawLine(0, y, W, y);
    }

    private void drawBoids(Graphics2D g2) {
        List<Boid> boids = sim.getBoids();
        for (Boid b : boids) {
            double h = b.heading();

            // Tip vertex
            double tx = b.getX() + TIP_R * Math.cos(h);
            double ty = b.getY() + TIP_R * Math.sin(h);

            // Rear-left vertex
            double lx = b.getX() + TAIL_R * Math.cos(h + TAIL_A);
            double ly = b.getY() + TAIL_R * Math.sin(h + TAIL_A);

            // Rear-right vertex
            double rx = b.getX() + TAIL_R * Math.cos(h - TAIL_A);
            double ry = b.getY() + TAIL_R * Math.sin(h - TAIL_A);

            int[] xs = {(int) tx, (int) lx, (int) rx};
            int[] ys = {(int) ty, (int) ly, (int) ry};

            // Colour by heading — map heading from (−π, π) to hue in [0, 1]
            float hue = (float) ((h / (2 * Math.PI) + 0.5) % 1.0);
            g2.setColor(Color.getHSBColor(hue, 0.8f, 0.9f));
            g2.fillPolygon(xs, ys, 3);
        }
    }

    private void drawPredator(Graphics2D g2) {
        double[] pred = sim.getPredator();
        if (pred == null) return;

        int px = (int) pred[0];
        int py = (int) pred[1];
        int sz = 9;

        g2.setColor(PREDATOR_COLOR);
        g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(px - sz, py - sz, px + sz, py + sz);
        g2.drawLine(px + sz, py - sz, px - sz, py + sz);
        g2.setStroke(new BasicStroke());
    }

    private void drawHud(Graphics2D g2) {
        g2.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2.setColor(TEXT_COLOR);
        int x = 12, y = 20;
        g2.drawString(String.format("Boids: %d", sim.getBoids().size()), x, y);
        g2.drawString(String.format("Perception r: %.0f px", sim.getPerceptionRadius()), x, y + 16);
        g2.drawString(String.format("Sep / Align / Coh: %.1f / %.1f / %.1f",
                sim.getSeparationWeight(), sim.getAlignmentWeight(), sim.getCohesionWeight()),
                x, y + 32);
        g2.drawString(String.format("Max speed: %.0f px/s", sim.getMaxSpeed()), x, y + 48);

        if (sim.getPredator() != null) {
            g2.setColor(PREDATOR_COLOR);
            g2.drawString("PREDATOR ACTIVE  (right-click to move)", x, y + 64);
        } else {
            g2.setColor(new Color(120, 130, 150));
            g2.drawString("Right-click to place predator  |  Left-click to add boids", x, H - 10);
        }
    }

    // -------------------------------------------------------------------------
    // Mouse interaction
    // -------------------------------------------------------------------------

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            sim.setPredator(e.getX(), e.getY());
        } else if (SwingUtilities.isLeftMouseButton(e)) {
            // Add a burst of 5 boids near the click position
            for (int i = 0; i < 5; i++) {
                double ox = (Math.random() - 0.5) * 20;
                double oy = (Math.random() - 0.5) * 20;
                sim.addBoid(e.getX() + ox, e.getY() + oy);
            }
        }
    }

    @Override public void mousePressed(MouseEvent e)  {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e)  {}
    @Override public void mouseExited(MouseEvent e)   {}

    // -------------------------------------------------------------------------
    // Accessor for animation timer (used by tests indirectly)
    // -------------------------------------------------------------------------

    /** Stop the animation timer (used when the window is closed). */
    void stopTimer() { gameLoop.stop(); }
}
