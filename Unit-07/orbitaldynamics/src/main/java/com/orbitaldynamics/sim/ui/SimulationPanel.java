package com.orbitaldynamics.sim.ui;

import com.orbitaldynamics.math.Vector2D;
import com.orbitaldynamics.sim.body.OrbitalBody;
import com.orbitaldynamics.sim.physics.PhysicsEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Main canvas for the N-body orbital simulation.
 *
 * <h2>Interaction</h2>
 * <ul>
 *   <li><b>Place Mode active</b>: click+drag creates a new body; release velocity = mouse velocity</li>
 *   <li><b>Click existing body</b>: drag to reposition; release applies mouse velocity</li>
 *   <li><b>Right-click background</b>: pan camera</li>
 *   <li><b>Right-click body</b>: delete it</li>
 *   <li><b>Scroll wheel</b>: zoom in/out around cursor</li>
 * </ul>
 *
 * <h2>Angular Velocity on Throw</h2>
 * <p>When placing or releasing a body, angular velocity is derived from
 * ω = (offset × velocity) / r²  (cross product of throw-offset and velocity).
 * This models the spin imparted by applying a force off-center.
 */
public final class SimulationPanel extends JPanel {

    // ── Physics ──────────────────────────────────────────────────────────────
    private final List<OrbitalBody> bodies = new CopyOnWriteArrayList<>();
    private final PhysicsEngine engine;
    private final Camera camera = new Camera();

    // ── Simulation state ─────────────────────────────────────────────────────
    private volatile boolean running = true;
    private double speedMultiplier   = 1.0;
    private double newBodyRadius     = 20.0;
    private double newBodyDensity    = 1.0;
    private boolean placeMode        = false;
    private boolean showVelocityArrows = true;
    private boolean showTrails         = true;

    // ── Mouse tracking ────────────────────────────────────────────────────────
    private enum DragMode { NONE, CREATING, DRAGGING_BODY, PANNING }
    private DragMode dragMode   = DragMode.NONE;
    private OrbitalBody draggedBody = null;   // body being dragged
    private Vector2D    ghostPos    = null;   // position of body being placed
    private int lastMouseX, lastMouseY;
    private final Deque<long[]> mouseHistory = new ArrayDeque<>(); // [x, y, t_nanos]
    private static final int HISTORY_SIZE = 12;

    // Any body farther than this from the world origin is considered escaped
    // and removed automatically. At min zoom (0.05×) a 900-px panel sees ±9000
    // world units, so 20 000 is well beyond anything visible.
    private static final double ESCAPE_THRESHOLD = 20_000.0;

    // ── Rendering ─────────────────────────────────────────────────────────────
    private static final Color BG_COLOR    = new Color(5, 5, 18);
    private static final Color TRAIL_COLOR = new Color(100, 130, 200, 60);
    private static final Color VEL_COLOR   = new Color(80, 255, 120, 180);
    private static final Color COM_COLOR   = new Color(255, 200, 60, 120);
    private static final Color GRID_COLOR  = new Color(20, 25, 40);
    private static final Color SELECT_COLOR= new Color(255, 220, 80, 200);

    // Callbacks
    private Runnable sidebarRefreshCallback;

    public SimulationPanel(PhysicsEngine engine) {
        this.engine = engine;
        setBackground(BG_COLOR);
        setPreferredSize(new Dimension(900, 700));
        setDoubleBuffered(true);
        setFocusable(true);

        // Delete / Backspace → remove selected body
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                int k = e.getKeyCode();
                if (k == KeyEvent.VK_DELETE || k == KeyEvent.VK_BACK_SPACE) {
                    synchronized (bodies) {
                        bodies.stream()
                              .filter(OrbitalBody::isSelected)
                              .findFirst()
                              .ifPresent(bodies::remove);
                    }
                    repaint();
                }
            }
        });

        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e)  { onMousePressed(e); }
            @Override public void mouseReleased(MouseEvent e) { onMouseReleased(e); }
            @Override public void mouseDragged(MouseEvent e)  { onMouseDragged(e); }
            @Override public void mouseMoved(MouseEvent e)    { lastMouseX = e.getX(); lastMouseY = e.getY(); }
            @Override public void mouseWheelMoved(MouseWheelEvent e) {
                camera.zoom(e.getWheelRotation(), e.getX(), e.getY());
                repaint();
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);
        addMouseWheelListener(ma);

        // Game loop: physics + repaint at ~60 fps using Swing Timer (runs on EDT)
        javax.swing.Timer gameTimer = new javax.swing.Timer(16, e -> {
            if (!running) return;
            double dt       = 0.016 * speedMultiplier;
            int    subSteps = Math.max(1, (int)(speedMultiplier * 2));
            double subDt    = dt / subSteps;
            synchronized (bodies) {
                for (int s = 0; s < subSteps; s++) {
                    if (!bodies.isEmpty()) engine.step(bodies, subDt);
                }
                // Remove bodies that were merged into another body this frame
                List<OrbitalBody> merged = engine.drainMergeRemovals();
                if (!merged.isEmpty()) bodies.removeAll(merged);

                // Remove bodies that have escaped beyond the visible universe
                bodies.removeIf(b -> {
                    double x = b.getPosition().x(), y = b.getPosition().y();
                    return Math.abs(x) > ESCAPE_THRESHOLD || Math.abs(y) > ESCAPE_THRESHOLD;
                });
            }
            if (sidebarRefreshCallback != null) sidebarRefreshCallback.run();
            repaint();
        });
        gameTimer.start();
    }

    // -------------------------------------------------------------------------
    // Painting
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,         RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();

        // Background
        g.setColor(BG_COLOR);
        g.fillRect(0, 0, w, h);

        // Grid
        drawGrid(g, w, h);

        // Trails
        if (showTrails) drawTrails(g);

        // Bodies
        synchronized (bodies) {
            for (OrbitalBody b : bodies) drawBody(g, b);
        }

        // Ghost body being placed
        if (ghostPos != null) drawGhostBody(g);

        // Center of mass crosshair
        if (!bodies.isEmpty()) drawCoM(g);

        // HUD
        drawHUD(g, w, h);
    }

    private void drawGrid(Graphics2D g, int w, int h) {
        g.setColor(GRID_COLOR);
        g.setStroke(new BasicStroke(0.5f));
        double gridStep = 100 * camera.getScale();
        if (gridStep < 20) gridStep *= 5;
        if (gridStep < 20) return;

        double startX = camera.getOffsetX() % gridStep;
        double startY = camera.getOffsetY() % gridStep;
        for (double x = startX; x < w; x += gridStep) g.drawLine((int)x, 0, (int)x, h);
        for (double y = startY; y < h; y += gridStep) g.drawLine(0, (int)y, w, (int)y);
    }

    private void drawTrails(Graphics2D g) {
        g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        synchronized (bodies) {
            for (OrbitalBody b : bodies) {
                var trail = b.getTrail();
                if (trail.size() < 2) continue;

                var iter = trail.iterator();
                Vector2D prev = iter.next();
                int total = trail.size();
                int idx = 0;
                while (iter.hasNext()) {
                    Vector2D curr = iter.next();
                    float alpha = (float) idx / total;
                    Color c = b.getColor();
                    g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(),
                        (int)(20 + alpha * 120)));

                    Point sp = camera.worldToScreen(prev);
                    Point sc = camera.worldToScreen(curr);
                    g.drawLine(sp.x, sp.y, sc.x, sc.y);
                    prev = curr;
                    idx++;
                }
            }
        }
    }

    private void drawBody(Graphics2D g, OrbitalBody b) {
        Point  sc = camera.worldToScreen(b.getPosition());
        int    sr = camera.worldToScreenRadius(b.getRadius());

        b.getTexture().paint(g, sc.x, sc.y, sr, b.getAngle());

        // Selection ring
        if (b.isSelected()) {
            g.setColor(SELECT_COLOR);
            g.setStroke(new BasicStroke(2f));
            g.drawOval(sc.x - sr - 3, sc.y - sr - 3, 2*(sr+3), 2*(sr+3));
        }

        // Velocity arrow
        if (showVelocityArrows && sr >= 4) {
            drawArrow(g, sc.x, sc.y, b.getVelocity(), VEL_COLOR, 0.4);
        }

        // Name label
        if (sr >= 8) {
            g.setColor(new Color(200, 210, 230, 180));
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
            g.drawString(b.getName(), sc.x + sr + 3, sc.y - 2);
        }
    }

    private void drawArrow(Graphics2D g, int ox, int oy, Vector2D v, Color color, double scale) {
        double vlen = v.magnitude();
        if (vlen < 0.01) return;
        int dx = (int)(v.x() * scale);
        int dy = (int)(v.y() * scale);
        int ex = ox + dx, ey = oy + dy;

        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(ox, oy, ex, ey);

        // Arrowhead
        double angle = Math.atan2(dy, dx);
        int headLen = Math.max(5, Math.min(12, (int)(vlen * scale * 0.3)));
        double a1 = angle + 2.7, a2 = angle - 2.7;
        g.drawLine(ex, ey,
            ex + (int)(headLen * Math.cos(a1)), ey + (int)(headLen * Math.sin(a1)));
        g.drawLine(ex, ey,
            ex + (int)(headLen * Math.cos(a2)), ey + (int)(headLen * Math.sin(a2)));
    }

    private void drawGhostBody(Graphics2D g) {
        if (ghostPos == null) return;
        Point sc = camera.worldToScreen(ghostPos);
        int   sr = camera.worldToScreenRadius(newBodyRadius);

        // Semi-transparent ghost
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        Color base = Color.getHSBColor(0.55f, 0.7f, 0.8f);
        g.setColor(base);
        g.fillOval(sc.x - sr, sc.y - sr, 2*sr, 2*sr);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g.setColor(Color.CYAN);
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
            1f, new float[]{4f, 3f}, 0f));
        g.drawOval(sc.x - sr, sc.y - sr, 2*sr, 2*sr);

        // Velocity preview arrow
        Vector2D measuredVel = measureMouseVelocity();
        if (measuredVel != null && measuredVel.magnitude() > 1) {
            drawArrow(g, sc.x, sc.y, measuredVel, Color.CYAN, 0.4);
        }

        g.setStroke(new BasicStroke(1f));
    }

    private void drawCoM(Graphics2D g) {
        synchronized (bodies) {
            if (bodies.isEmpty()) return;
            Vector2D com = engine.centerOfMass(bodies);
            Point sc = camera.worldToScreen(com);
            g.setColor(COM_COLOR);
            g.setStroke(new BasicStroke(1.5f));
            int s = 6;
            g.drawLine(sc.x - s, sc.y, sc.x + s, sc.y);
            g.drawLine(sc.x, sc.y - s, sc.x, sc.y + s);
        }
    }

    private void drawHUD(Graphics2D g, int w, int h) {
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        g.setColor(new Color(120, 140, 180, 160));
        String mode = placeMode ? "MODE: PLACE  (r=" + (int)newBodyRadius + " ρ=" + newBodyDensity + ")"
                                : "MODE: NAVIGATE";
        g.drawString(mode, 10, h - 10);
        g.drawString("G=" + (int)engine.getG() + "  speed=" + speedMultiplier + "x  zoom="
            + String.format("%.2f", camera.getScale()), 10, h - 25);
    }

    // -------------------------------------------------------------------------
    // Mouse event handlers
    // -------------------------------------------------------------------------

    private void onMousePressed(MouseEvent e) {
        requestFocusInWindow();   // ensure Delete key events reach this panel
        mouseHistory.clear();
        recordMouseHistory(e.getX(), e.getY());

        if (SwingUtilities.isRightMouseButton(e)) {
            // Right-click: pan OR delete body
            OrbitalBody hit = bodyAt(e.getX(), e.getY());
            if (hit != null) {
                synchronized (bodies) { bodies.remove(hit); }
            } else {
                dragMode = DragMode.PANNING;
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
            return;
        }

        if (placeMode) {
            ghostPos = camera.screenToWorld(e.getX(), e.getY());
            dragMode = DragMode.CREATING;
            return;
        }

        OrbitalBody hit = bodyAt(e.getX(), e.getY());
        if (hit != null) {
            synchronized (bodies) { bodies.forEach(b -> b.setSelected(false)); }
            hit.setSelected(true);
            draggedBody = hit;
            dragMode    = DragMode.DRAGGING_BODY;
        } else {
            dragMode = DragMode.PANNING;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        }
    }

    private void onMouseDragged(MouseEvent e) {
        recordMouseHistory(e.getX(), e.getY());

        switch (dragMode) {
            case CREATING -> ghostPos = camera.screenToWorld(e.getX(), e.getY());
            case DRAGGING_BODY -> {
                if (draggedBody != null) {
                    draggedBody.setPosition(camera.screenToWorld(e.getX(), e.getY()));
                    draggedBody.setVelocity(Vector2D.ZERO);  // freeze while dragging
                }
            }
            case PANNING -> {
                int dx = e.getX() - lastMouseX;
                int dy = e.getY() - lastMouseY;
                camera.pan(dx, dy);
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
            default -> {}
        }
        repaint();
    }

    private void onMouseReleased(MouseEvent e) {
        recordMouseHistory(e.getX(), e.getY());
        Vector2D releaseWorld = camera.screenToWorld(e.getX(), e.getY());
        Vector2D vel = measureMouseVelocity();
        if (vel == null) vel = Vector2D.ZERO;

        switch (dragMode) {
            case CREATING -> {
                if (ghostPos != null) {
                    // Angular velocity from cross product of offset × velocity / r²
                    Vector2D mouseOffset = releaseWorld.sub(ghostPos);
                    double omega = mouseOffset.cross(vel) / (newBodyRadius * newBodyRadius * 0.5);
                    omega = Math.max(-10, Math.min(10, omega));  // clamp for sanity

                    OrbitalBody b = new OrbitalBody(
                        releaseWorld, vel, newBodyRadius, newBodyDensity,
                        Math.random() * 2 * Math.PI, omega);
                    synchronized (bodies) { bodies.add(b); }
                }
                ghostPos = null;
            }
            case DRAGGING_BODY -> {
                if (draggedBody != null) {
                    draggedBody.setPosition(releaseWorld);
                    Vector2D mouseOffset = releaseWorld.sub(draggedBody.getPosition());
                    double omega = mouseOffset.cross(vel) / (draggedBody.getRadius() * draggedBody.getRadius() * 0.5);
                    omega = Math.max(-10, Math.min(10, omega));
                    draggedBody.setVelocity(vel);
                    draggedBody.setOmega(omega);
                    draggedBody.clearTrail();
                }
                draggedBody = null;
            }
            default -> {}
        }
        dragMode = DragMode.NONE;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Returns the body under screen point (x,y), or null. */
    private OrbitalBody bodyAt(int sx, int sy) {
        Vector2D world = camera.screenToWorld(sx, sy);
        synchronized (bodies) {
            // Iterate in reverse so topmost-painted body is picked first
            for (int i = bodies.size() - 1; i >= 0; i--) {
                OrbitalBody b = bodies.get(i);
                if (world.distanceTo(b.getPosition()) <= b.getRadius()) return b;
            }
        }
        return null;
    }

    private void recordMouseHistory(int x, int y) {
        long t = System.nanoTime();
        mouseHistory.addLast(new long[]{x, y, t});
        while (mouseHistory.size() > HISTORY_SIZE) mouseHistory.pollFirst();
    }

    /** Computes velocity from the mouse history ring buffer. Returns null if not enough data. */
    private Vector2D measureMouseVelocity() {
        if (mouseHistory.size() < 2) return Vector2D.ZERO;
        long[] first = mouseHistory.peekFirst();
        long[] last  = mouseHistory.peekLast();
        double dtSec = (last[2] - first[2]) / 1e9;
        if (dtSec < 1e-6) return Vector2D.ZERO;

        // Convert screen delta to world delta
        Vector2D worldFirst = camera.screenToWorld((int)first[0], (int)first[1]);
        Vector2D worldLast  = camera.screenToWorld((int)last[0],  (int)last[1]);
        return worldLast.sub(worldFirst).scale(1.0 / dtSec);
    }

    // -------------------------------------------------------------------------
    // Public API (called from SimulationFrame)
    // -------------------------------------------------------------------------

    public List<OrbitalBody> getBodies()   { return bodies; }
    public Camera            getCamera()   { return camera; }
    public boolean           isRunning()   { return running; }

    public void setRunning(boolean r)      { this.running = r; }
    public void setSpeedMultiplier(double s) { this.speedMultiplier = s; }
    public void setPlaceMode(boolean p)    { this.placeMode = p; }
    public void setNewBodyRadius(double r) { this.newBodyRadius = r; }
    public void setNewBodyDensity(double d){ this.newBodyDensity = d; }
    public void setShowVelocityArrows(boolean b){ this.showVelocityArrows = b; }
    public void setShowTrails(boolean b)   { this.showTrails = b; }
    public void setSidebarRefreshCallback(Runnable r){ this.sidebarRefreshCallback = r; }

    /** Removes a single body from the simulation. */
    public void removeBody(OrbitalBody b) {
        synchronized (bodies) { bodies.remove(b); }
    }

    public void clearBodies() {
        synchronized (bodies) { bodies.clear(); }
    }

    public void loadPreset(List<OrbitalBody> preset) {
        synchronized (bodies) {
            bodies.clear();
            bodies.addAll(preset);
        }
        camera.centerOn(getWidth() > 0 ? getWidth() : 900,
                         getHeight()> 0 ? getHeight(): 700);
    }

    /** Called once the panel is shown to initialize camera. */
    @Override
    public void addNotify() {
        super.addNotify();
        SwingUtilities.invokeLater(() -> camera.centerOn(getWidth(), getHeight()));
    }
}
