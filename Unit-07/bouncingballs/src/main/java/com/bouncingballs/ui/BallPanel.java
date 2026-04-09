package com.bouncingballs.ui;

import com.bouncingballs.model.Ball;
import com.bouncingballs.physics.PhysicsEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * The main simulation canvas — the "glass jar".
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Owns the ball list and drives the 60 fps Swing {@link Timer}.</li>
 *   <li>Polls the parent frame's screen location each tick to compute window
 *       velocity and pass it to {@link PhysicsEngine} as an inertia impulse.</li>
 *   <li>Handles mouse: click empty space → add ball; drag ball → throw it.</li>
 *   <li>Renders the glass-jar backdrop, pressure gradient, balls with
 *       specular highlights, and a live HUD.</li>
 * </ul>
 */
public class BallPanel extends JPanel {

    // ── Layout constants ──────────────────────────────────────────────────────
    public static final int PANEL_W  = 500;
    public static final int PANEL_H  = 580;

    /** Glass wall thickness on the sides and bottom. */
    private static final int WALL    = 22;
    /** Glass wall thickness at the top. */
    private static final int TOP_WALL = 18;

    // Physics boundaries (inner surface of glass)
    private static final double LEFT   = WALL;
    private static final double RIGHT  = PANEL_W - WALL;
    private static final double TOP    = TOP_WALL;
    private static final double BOTTOM = PANEL_H - WALL;

    // ── Ball palette ──────────────────────────────────────────────────────────
    private static final Color[] PALETTE = {
        new Color(255,  80,  80),  // red
        new Color( 80, 160, 255),  // blue
        new Color( 80, 220, 100),  // green
        new Color(255, 200,  60),  // yellow
        new Color(200, 100, 255),  // purple
        new Color(255, 140,  50),  // orange
        new Color( 55, 215, 205),  // teal
        new Color(255, 120, 180),  // pink
        new Color(175, 230,  80),  // lime
        new Color(110, 190, 255),  // sky
    };

    // ── State ─────────────────────────────────────────────────────────────────
    private final List<Ball> balls  = new ArrayList<>();
    private final Random     rng    = new Random();
    private double           gravity = 800.0;  // px/s²

    // Pending add settings (updated by ControlPanel)
    private Ball.Size    pendingSize    = Ball.Size.MEDIUM;
    private Ball.Density pendingDensity = Ball.Density.MEDIUM;

    // Window-drag inertia tracking
    private final JFrame frame;
    private Point  prevFrameLoc   = null;
    private double windowVelX     = 0;
    private double windowVelY     = 0;

    // Mouse interaction
    private Ball   dragBall       = null;
    private double dragOffsetX, dragOffsetY;
    private double lastDragX, lastDragY;
    private long   lastDragTimeMs;
    private int    pressX, pressY; // position of mousePressed

    // Back buffer
    private BufferedImage backBuf;
    private Graphics2D    backG2;

    // Timer
    private final Timer timer;
    private long  lastTickNs = System.nanoTime();

    // ── Constructor ───────────────────────────────────────────────────────────

    public BallPanel(JFrame frame) {
        this.frame = frame;
        setPreferredSize(new Dimension(PANEL_W, PANEL_H));
        setBackground(new Color(8, 10, 22));
        setFocusable(true);

        backBuf = new BufferedImage(PANEL_W, PANEL_H, BufferedImage.TYPE_INT_ARGB);
        backG2  = backBuf.createGraphics();
        backG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        backG2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        addMouseListeners();

        timer = new Timer(1000 / 60, e -> tick());
        timer.start();
    }

    // ── Public API (called by ControlPanel) ───────────────────────────────────

    public void setGravity(double g)               { this.gravity = g; }
    public void setPendingSize(Ball.Size s)         { this.pendingSize = s; }
    public void setPendingDensity(Ball.Density d)   { this.pendingDensity = d; }
    public int  getBallCount()                      { return balls.size(); }

    /** Add a ball at the given panel coordinates using the pending size/density. */
    public void addBall(double px, double py) {
        double radius = pendingSize.randomRadius(rng);

        // Clamp to jar interior
        double cx = Math.max(LEFT + radius, Math.min(RIGHT - radius, px));
        double cy = Math.max(TOP  + radius, Math.min(BOTTOM - radius, py));

        Color base  = PALETTE[rng.nextInt(PALETTE.length)];
        Color color = tintByDensity(base, pendingDensity);
        balls.add(new Ball(cx, cy, radius, pendingDensity.value, color));
        requestFocusInWindow();
    }

    public void clearBalls() {
        balls.clear();
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    private void tick() {
        long nowNs = System.nanoTime();
        double dt  = Math.min((nowNs - lastTickNs) / 1_000_000_000.0, 0.05);
        lastTickNs = nowNs;

        updateWindowVelocity(dt);

        // Physics (skip dragged ball — it's pinned)
        List<Ball> simBalls = new ArrayList<>(balls);
        if (dragBall != null) simBalls.remove(dragBall);

        PhysicsEngine.step(simBalls, gravity, dt,
                LEFT, RIGHT, TOP, BOTTOM,
                windowVelX, windowVelY);

        render();
        repaint();
    }

    private void updateWindowVelocity(double dt) {
        Point loc = frame.getLocation();
        if (prevFrameLoc != null && dt > 0) {
            double raw_vx = (loc.x - prevFrameLoc.x) / dt;
            double raw_vy = (loc.y - prevFrameLoc.y) / dt;
            // Clamp to avoid extreme impulses from fast snaps
            windowVelX = Math.max(-1500, Math.min(1500, raw_vx));
            windowVelY = Math.max(-1500, Math.min(1500, raw_vy));
        } else {
            windowVelX = 0;
            windowVelY = 0;
        }
        prevFrameLoc = loc;
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void render() {
        Graphics2D g = backG2;

        // Background
        g.setColor(new Color(8, 10, 22));
        g.fillRect(0, 0, PANEL_W, PANEL_H);

        drawJar(g);
        drawBalls(g);
        drawHUD(g);
    }

    @Override
    protected void paintComponent(Graphics gScreen) {
        super.paintComponent(gScreen);
        gScreen.drawImage(backBuf, 0, 0, null);
    }

    // ── Jar drawing ───────────────────────────────────────────────────────────

    private void drawJar(Graphics2D g) {
        // Interior fill — dark liquid blue
        GradientPaint interior = new GradientPaint(
                0, (int) TOP, new Color(18, 24, 52),
                0, (int) BOTTOM, new Color(10, 14, 34));
        g.setPaint(interior);
        g.fillRect((int) LEFT, (int) TOP, (int)(RIGHT - LEFT), (int)(BOTTOM - TOP));

        // Draw glass walls (left, right, bottom, top) as gradient bands
        drawGlassWall(g, 0,      (int) TOP,   WALL, PANEL_H - (int)TOP,   true,  false);  // left
        drawGlassWall(g, (int)RIGHT, (int)TOP, WALL, PANEL_H - (int)TOP,   false, false);  // right
        drawGlassWall(g, 0,  PANEL_H - WALL,  PANEL_W, WALL,              false, true);   // bottom
        drawGlassWall(g, 0,  0,               PANEL_W, TOP_WALL,           false, true);   // top

        // Inner rim highlight (bright glass edge)
        g.setColor(new Color(140, 190, 255, 55));
        g.setStroke(new BasicStroke(1.4f));
        g.drawRect((int)LEFT, (int)TOP, (int)(RIGHT - LEFT), (int)(BOTTOM - TOP));

        // Corner accents
        g.setColor(new Color(160, 210, 255, 80));
        int cs = 10;
        g.drawLine((int)LEFT, (int)TOP,       (int)LEFT + cs,  (int)TOP);
        g.drawLine((int)LEFT, (int)TOP,       (int)LEFT,       (int)TOP + cs);
        g.drawLine((int)RIGHT, (int)TOP,      (int)RIGHT - cs, (int)TOP);
        g.drawLine((int)RIGHT, (int)TOP,      (int)RIGHT,      (int)TOP + cs);

        // Glass sheen — diagonal highlight streak on top-left of jar
        g.setClip((int)LEFT, (int)TOP, (int)(RIGHT - LEFT), (int)(BOTTOM - TOP));
        GradientPaint sheen = new GradientPaint(
                (int)LEFT + 10, (int)TOP + 10, new Color(255, 255, 255, 28),
                (int)LEFT + 80, (int)TOP + 120, new Color(255, 255, 255, 0));
        g.setPaint(sheen);
        g.fillRect((int)LEFT, (int)TOP, (int)(RIGHT - LEFT), (int)(BOTTOM - TOP));
        g.setClip(null);

        // Outer frame (border of the glass block)
        g.setColor(new Color(90, 130, 200, 120));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(1, 1, PANEL_W - 2, PANEL_H - 2);
    }

    private void drawGlassWall(Graphics2D g, int x, int y, int w, int h,
                                boolean innerOnRight, boolean innerOnTop) {
        GradientPaint gp;
        if (innerOnRight) {
            gp = new GradientPaint(x, 0, new Color(60, 100, 180, 120),
                                   x + w, 0, new Color(30, 60, 120, 30));
        } else if (!innerOnTop && !innerOnRight) {
            // right wall — inner is left side
            gp = new GradientPaint(x, 0, new Color(30, 60, 120, 30),
                                   x + w, 0, new Color(60, 100, 180, 120));
        } else {
            // top or bottom
            gp = new GradientPaint(0, y, new Color(50, 90, 160, 110),
                                   0, y + h, new Color(30, 55, 110, 40));
        }
        g.setPaint(gp);
        g.fillRect(x, y, w, h);
    }

    // ── Ball drawing ──────────────────────────────────────────────────────────

    private void drawBalls(Graphics2D g) {
        for (Ball b : balls) {
            drawBall(g, b);
        }
    }

    private void drawBall(Graphics2D g, Ball ball) {
        int cx = (int) Math.round(ball.x);
        int cy = (int) Math.round(ball.y);
        int r  = (int) Math.round(ball.radius);

        boolean dragging = (ball == dragBall);

        // Body — radial gradient for 3-D sphere look
        float[] fractions = {0f, 0.7f, 1f};
        Color mid  = interpolateColor(ball.color, ball.color.darker(), 0.4f);
        Color edge = ball.color.darker().darker();
        Color[] colors = {ball.color.brighter(), mid, edge};
        float offX = -r * 0.25f;
        float offY = -r * 0.30f;
        RadialGradientPaint body = new RadialGradientPaint(
                new Point2D.Float(cx + offX, cy + offY),
                r * 1.05f,
                fractions, colors,
                MultipleGradientPaint.CycleMethod.NO_CYCLE);
        g.setPaint(body);
        g.fillOval(cx - r, cy - r, 2 * r, 2 * r);

        // Outline
        g.setColor(dragging ? Color.WHITE : edge.darker());
        g.setStroke(new BasicStroke(dragging ? 2.0f : 1.2f));
        g.drawOval(cx - r, cy - r, 2 * r, 2 * r);

        // Primary specular highlight (upper-left)
        int hlR = Math.max(2, r / 4);
        int hlX = cx - r / 3;
        int hlY = cy - r / 3;
        RadialGradientPaint spec = new RadialGradientPaint(
                new Point2D.Float(hlX, hlY), hlR,
                new float[]{0f, 1f},
                new Color[]{new Color(255, 255, 255, 200), new Color(255, 255, 255, 0)});
        g.setPaint(spec);
        g.fillOval(hlX - hlR, hlY - hlR, hlR * 2, hlR * 2);

        // Tiny secondary specular (opposite quadrant, subtle)
        int s2R = Math.max(1, r / 7);
        g.setColor(new Color(255, 255, 255, 50));
        g.fillOval(cx + r / 3 - s2R, cy + r / 3 - s2R, s2R * 2, s2R * 2);

        // Label (show density category for balls large enough)
        if (r >= 18) {
            String label = densityLabel(ball.density);
            g.setFont(new Font("SansSerif", Font.BOLD, Math.max(8, r / 3)));
            FontMetrics fm = g.getFontMetrics();
            int lw = fm.stringWidth(label);
            g.setColor(new Color(0, 0, 0, 100));
            g.drawString(label, cx - lw / 2 + 1, cy + fm.getAscent() / 2 + 1);
            g.setColor(new Color(255, 255, 255, 180));
            g.drawString(label, cx - lw / 2, cy + fm.getAscent() / 2);
        }
    }

    // ── HUD ──────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g.setColor(new Color(160, 200, 255, 160));
        String info = String.format("Balls: %d   Gravity: %.0f px/s²   Drag window to slosh!",
                balls.size(), gravity);
        g.drawString(info, (int)LEFT + 4, (int)TOP - 4);
    }

    // ── Mouse handling ────────────────────────────────────────────────────────

    private void addMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                pressX = e.getX();
                pressY = e.getY();
                // Find topmost ball under cursor (iterate in reverse = drawn-last on top)
                for (int i = balls.size() - 1; i >= 0; i--) {
                    Ball b = balls.get(i);
                    if (b.contains(e.getX(), e.getY())) {
                        dragBall     = b;
                        dragOffsetX  = b.x - e.getX();
                        dragOffsetY  = b.y - e.getY();
                        lastDragX    = e.getX();
                        lastDragY    = e.getY();
                        lastDragTimeMs = System.currentTimeMillis();
                        b.vx = 0; b.vy = 0;
                        return;
                    }
                }
                dragBall = null;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (dragBall != null) {
                    // Give the ball a throw velocity from the drag motion
                    long dtMs = System.currentTimeMillis() - lastDragTimeMs;
                    if (dtMs > 0 && dtMs < 120) {
                        dragBall.vx = (e.getX() - lastDragX) / (dtMs / 1000.0);
                        dragBall.vy = (e.getY() - lastDragY) / (dtMs / 1000.0);
                    } else {
                        dragBall.vx = 0;
                        dragBall.vy = 0;
                    }
                    dragBall = null;
                } else {
                    // Only add ball if mouse didn't move much (not a pan gesture)
                    int dx = e.getX() - pressX, dy = e.getY() - pressY;
                    if (dx * dx + dy * dy < 25 && isInsideJar(e.getX(), e.getY())) {
                        addBall(e.getX(), e.getY());
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (dragBall != null) {
                    double nx = e.getX() + dragOffsetX;
                    double ny = e.getY() + dragOffsetY;
                    // Clamp to jar interior
                    nx = Math.max(LEFT  + dragBall.radius, Math.min(RIGHT  - dragBall.radius, nx));
                    ny = Math.max(TOP   + dragBall.radius, Math.min(BOTTOM - dragBall.radius, ny));
                    lastDragX      = dragBall.x - dragOffsetX;
                    lastDragY      = dragBall.y - dragOffsetY;
                    lastDragTimeMs = System.currentTimeMillis();
                    dragBall.x     = nx;
                    dragBall.y     = ny;
                }
            }
        });
    }

    private boolean isInsideJar(int px, int py) {
        return px >= LEFT && px <= RIGHT && py >= TOP && py <= BOTTOM;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Tint a palette colour based on the ball's density category. */
    private static Color tintByDensity(Color base, Ball.Density density) {
        return switch (density) {
            case LIGHT  -> base.brighter();
            case MEDIUM -> base;
            case HEAVY  -> interpolateColor(base, new Color(30, 30, 35), 0.40f);
        };
    }

    private static String densityLabel(double density) {
        if (density <= 0.5)  return "L";
        if (density <= 2.0)  return "M";
        return "H";
    }

    private static Color interpolateColor(Color a, Color b, float t) {
        int r = (int)(a.getRed()   + t * (b.getRed()   - a.getRed()));
        int g = (int)(a.getGreen() + t * (b.getGreen() - a.getGreen()));
        int bl = (int)(a.getBlue()  + t * (b.getBlue()  - a.getBlue()));
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, bl)));
    }
}
