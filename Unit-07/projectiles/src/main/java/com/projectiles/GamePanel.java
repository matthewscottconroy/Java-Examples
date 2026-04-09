package com.projectiles;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game canvas: renders and drives the slingshot projectile simulation.
 *
 * <h2>Interaction</h2>
 * <ul>
 *   <li>Click and drag from anywhere near the slingshot fork to aim</li>
 *   <li>Release to launch</li>
 *   <li>Press R to reset (new terrain + full ammo)</li>
 * </ul>
 *
 * <h2>Slingshot geometry</h2>
 * <pre>
 *   LEFT TIP ──── RIGHT TIP
 *        \            /
 *         \          /
 *          \        /
 *           └──────┘   ← Y-fork base
 *               |
 *           TRUNK_X, TRUNK_BASE_Y
 * </pre>
 */
public final class GamePanel extends JPanel {

    // ── Canvas ────────────────────────────────────────────────────────────────
    public static final int WIDTH  = 1100;
    public static final int HEIGHT = 620;

    // ── Physics ───────────────────────────────────────────────────────────────
    private static final double GRAVITY        = 200.0;   // px/s²
    private static final double TARGET_FPS     = 60.0;
    private static final double DT             = 1.0 / TARGET_FPS;
    private static final int    EXPLODE_RADIUS = 55;
    private static final int    DEBRIS_COUNT   = 160;
    private static final double LAUNCH_POWER   = 5.0;     // velocity scale

    // ── Slingshot geometry ────────────────────────────────────────────────────
    private static final int TRUNK_X      = 115;   // centre column of trunk
    private static final int FORK_HEIGHT  = 50;    // how tall the fork opening is
    private static final int FORK_WIDTH   = 36;    // half-width of fork at tips
    private static final int TRUNK_WIDTH  = 14;
    private static final int TRUNK_HEIGHT = 80;

    private static final Color WOOD_DARK   = new Color(101,  67, 33);
    private static final Color WOOD_MID    = new Color(139,  90, 43);
    private static final Color WOOD_LIGHT  = new Color(180, 120, 60);
    private static final Color RUBBER_COL  = new Color(180,  50, 50);

    // ── Game state ────────────────────────────────────────────────────────────
    private enum State { AIMING, FLYING, RELOADING, GAME_OVER }

    private Terrain          terrain;
    private Projectile       projectile;
    private final List<Debris> debrisList = new ArrayList<>();

    private State  state      = State.RELOADING;
    private int    ammo       = 8;

    // ── Slingshot / aiming ────────────────────────────────────────────────────
    private int    trunkBaseY;           // bottom of the trunk (on terrain surface)
    private int    leftTipX, leftTipY;   // fork tip positions
    private int    rightTipX, rightTipY;
    private int    seatX, seatY;         // resting launch position (between tips)

    // Drag state
    private boolean dragging   = false;
    private int     dragX, dragY;
    private static final int   DRAG_RADIUS   = 80;    // max pull distance
    private static final int   GRAB_RADIUS   = 60;    // how close cursor must be to grab

    // ── Off-screen buffer ─────────────────────────────────────────────────────
    private BufferedImage backBuffer;
    private Graphics2D    bg;

    // ── Timer ─────────────────────────────────────────────────────────────────
    private final Timer gameTimer;
    private long  lastNano = System.nanoTime();

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(30, 30, 50));
        setFocusable(true);

        backBuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        bg         = backBuffer.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        reset();

        // Key bindings
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R) reset();
            }
        });

        // Mouse bindings
        MouseAdapter mouse = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (state == State.RELOADING) {
                    int dx = e.getX() - seatX, dy = e.getY() - seatY;
                    if (dx * dx + dy * dy <= GRAB_RADIUS * GRAB_RADIUS) {
                        state    = State.AIMING;
                        dragging = true;
                        dragX    = seatX;
                        dragY    = seatY;
                    }
                }
            }
            @Override public void mouseDragged(MouseEvent e) {
                if (state == State.AIMING && dragging) {
                    int dx = e.getX() - seatX, dy = e.getY() - seatY;
                    double dist = Math.sqrt(dx * dx + dy * dy);
                    if (dist > DRAG_RADIUS) {
                        dx = (int) (dx * DRAG_RADIUS / dist);
                        dy = (int) (dy * DRAG_RADIUS / dist);
                    }
                    dragX = seatX + dx;
                    dragY = seatY + dy;
                }
            }
            @Override public void mouseReleased(MouseEvent e) {
                if (state == State.AIMING && dragging) {
                    launch();
                    dragging = false;
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);

        gameTimer = new Timer((int) (1000 / TARGET_FPS), e -> tick());
        gameTimer.start();
    }

    // -------------------------------------------------------------------------
    // Reset
    // -------------------------------------------------------------------------

    private void reset() {
        terrain    = new Terrain(WIDTH, HEIGHT);
        debrisList .clear();
        projectile = null;
        ammo       = 8;
        state      = State.RELOADING;
        dragging   = false;
        computeSlingshotGeometry();
    }

    private void computeSlingshotGeometry() {
        trunkBaseY = terrain.getSurfaceY(TRUNK_X);
        int forkBase = trunkBaseY - TRUNK_HEIGHT;
        leftTipX    = TRUNK_X - FORK_WIDTH;
        leftTipY    = forkBase - FORK_HEIGHT;
        rightTipX   = TRUNK_X + FORK_WIDTH;
        rightTipY   = forkBase - FORK_HEIGHT;
        seatX       = TRUNK_X;
        seatY       = forkBase;
    }

    // -------------------------------------------------------------------------
    // Launch
    // -------------------------------------------------------------------------

    private void launch() {
        if (ammo <= 0) { state = State.GAME_OVER; return; }

        // Velocity = opposite of pull vector, scaled
        double vx = (seatX - dragX) * LAUNCH_POWER;
        double vy = (seatY - dragY) * LAUNCH_POWER;

        projectile = new Projectile(seatX, seatY, vx, vy);
        state      = State.FLYING;
        ammo--;
    }

    // -------------------------------------------------------------------------
    // Game loop
    // -------------------------------------------------------------------------

    private void tick() {
        long now  = System.nanoTime();
        double dt = Math.min((now - lastNano) / 1e9, 0.05);
        lastNano  = now;

        switch (state) {
            case FLYING -> {
                boolean hit = projectile.update(dt, GRAVITY, terrain);
                if (hit) {
                    explode((int) projectile.getX(), (int) projectile.getY());
                    state = (ammo > 0) ? State.RELOADING : State.GAME_OVER;
                } else if (!projectile.isActive()) {
                    // Left screen
                    state = (ammo > 0) ? State.RELOADING : State.GAME_OVER;
                }
            }
            case RELOADING, AIMING, GAME_OVER -> { /* input-driven */ }
        }

        // Update debris regardless of state
        debrisList.removeIf(d -> d.update(dt, terrain));

        repaint();
    }

    private void explode(int cx, int cy) {
        List<Color> destroyed = terrain.explode(cx, cy, EXPLODE_RADIUS);
        List<Debris> newDebris = Debris.spawn(destroyed, cx, cy, DEBRIS_COUNT);
        debrisList.addAll(newDebris);
    }

    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);

        // Sky gradient
        GradientPaint sky = new GradientPaint(
            0, 0,   new Color(25, 35, 80),
            0, HEIGHT, new Color(80, 110, 170));
        bg.setPaint(sky);
        bg.fillRect(0, 0, WIDTH, HEIGHT);

        // Stars (subtle)
        bg.setColor(new Color(255, 255, 255, 40));
        long seed = 42;
        for (int i = 0; i < 120; i++) {
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int sx = (int) ((seed >>> 33) % WIDTH);
            seed = seed * 6364136223846793005L + 1442695040888963407L;
            int sy = (int) ((seed >>> 33) % (HEIGHT / 2));
            bg.fillRect(sx, sy, 1, 1);
        }

        terrain.draw(bg);
        drawSlingshot(bg);

        // Rubber bands
        if (state == State.AIMING && dragging) {
            drawRubberBands(bg, dragX, dragY);
            drawTrajectoryPreview(bg, dragX, dragY);
            drawAimBall(bg, dragX, dragY);
        } else if (state == State.RELOADING) {
            drawRubberBands(bg, seatX, seatY);
            drawAimBall(bg, seatX, seatY);
        }

        // Flying projectile
        if (projectile != null) {
            projectile.draw(bg);
        }

        // Debris
        for (Debris d : debrisList) d.draw(bg);

        // HUD
        drawHUD(bg);

        // Blit to screen
        g0.drawImage(backBuffer, 0, 0, null);
    }

    // ── Slingshot ─────────────────────────────────────────────────────────────

    private void drawSlingshot(Graphics2D g) {
        // Trunk
        int tx = TRUNK_X - TRUNK_WIDTH / 2;
        int ty = trunkBaseY - TRUNK_HEIGHT;
        drawWoodRect(g, tx, ty, TRUNK_WIDTH, TRUNK_HEIGHT);

        // Left fork arm
        drawWoodArm(g, TRUNK_X, trunkBaseY - TRUNK_HEIGHT, leftTipX, leftTipY, TRUNK_WIDTH - 4);

        // Right fork arm
        drawWoodArm(g, TRUNK_X, trunkBaseY - TRUNK_HEIGHT, rightTipX, rightTipY, TRUNK_WIDTH - 4);

        // Fork tip caps
        g.setColor(WOOD_DARK);
        g.fillOval(leftTipX  - 6, leftTipY  - 6, 12, 12);
        g.fillOval(rightTipX - 6, rightTipY - 6, 12, 12);
    }

    private void drawWoodRect(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(WOOD_MID);
        g.fillRect(x, y, w, h);
        g.setColor(WOOD_LIGHT);
        g.fillRect(x + 2, y, 3, h);
        g.setColor(WOOD_DARK);
        g.drawRect(x, y, w, h);
    }

    private void drawWoodArm(Graphics2D g, int x1, int y1, int x2, int y2, int thick) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(WOOD_MID);
        g.drawLine(x1, y1, x2, y2);
        g.setStroke(new BasicStroke(thick - 3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(WOOD_LIGHT);
        g.drawLine(x1, y1, x2, y2);
        g.setStroke(old);
    }

    // ── Rubber bands ──────────────────────────────────────────────────────────

    private void drawRubberBands(Graphics2D g, int bx, int by) {
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(RUBBER_COL);
        g.drawLine(leftTipX,  leftTipY,  bx, by);
        g.drawLine(rightTipX, rightTipY, bx, by);
        g.setStroke(old);
    }

    // ── Aim ball (projectile sitting in sling) ────────────────────────────────

    private void drawAimBall(Graphics2D g, int bx, int by) {
        g.setColor(new Color(0, 0, 0, 60));
        g.fillOval(bx - Projectile.RADIUS + 2, by - Projectile.RADIUS + 3,
                   Projectile.RADIUS * 2, Projectile.RADIUS * 2);
        g.setColor(new Color(90, 90, 100));
        g.fillOval(bx - Projectile.RADIUS, by - Projectile.RADIUS,
                   Projectile.RADIUS * 2, Projectile.RADIUS * 2);
        g.setColor(new Color(140, 140, 155));
        int hl = Projectile.RADIUS / 2;
        g.fillOval(bx - hl + 1, by - hl - 1, hl + 2, hl + 2);
    }

    // ── Trajectory preview ────────────────────────────────────────────────────

    private void drawTrajectoryPreview(Graphics2D g, int bx, int by) {
        double vx = (seatX - bx) * LAUNCH_POWER;
        double vy = (seatY - by) * LAUNCH_POWER;
        double[][] pts = Projectile.preview(seatX, seatY, vx, vy,
                                            80, 0.04, GRAVITY, terrain);
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND,
                                    BasicStroke.JOIN_ROUND,
                                    0, new float[]{5, 5}, 0));
        g.setColor(new Color(255, 255, 200, 140));
        for (int i = 1; i < pts.length; i++) {
            g.drawLine((int) pts[i-1][0], (int) pts[i-1][1],
                       (int) pts[i][0],   (int) pts[i][1]);
        }
        g.setStroke(old);
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g) {
        // Ammo row
        g.setColor(new Color(255, 255, 255, 200));
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        g.drawString("Ammo:", WIDTH - 200, 28);
        for (int i = 0; i < ammo; i++) {
            int bx = WIDTH - 135 + i * 18;
            g.setColor(new Color(90, 90, 100));
            g.fillOval(bx, 16, 13, 13);
            g.setColor(new Color(140, 140, 155));
            g.fillOval(bx + 2, 17, 6, 6);
        }

        // State messages
        if (state == State.RELOADING) {
            drawCentredMsg(g, "Click near the sling to aim", HEIGHT - 30, new Color(220, 220, 180, 180));
        } else if (state == State.GAME_OVER) {
            drawLargeMsg(g, "Out of ammo!  Press R to reset", new Color(255, 120, 80));
        }

        // Controls hint
        g.setColor(new Color(200, 200, 200, 120));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.drawString("R = reset", 10, HEIGHT - 10);
    }

    private void drawCentredMsg(Graphics2D g, String msg, int y, Color color) {
        g.setColor(color);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(msg, (WIDTH - fm.stringWidth(msg)) / 2, y);
    }

    private void drawLargeMsg(Graphics2D g, String msg, Color color) {
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
        FontMetrics fm = g.getFontMetrics();
        int tx = (WIDTH - fm.stringWidth(msg)) / 2;
        int ty = HEIGHT / 2 - 20;
        g.setColor(new Color(0, 0, 0, 140));
        g.drawString(msg, tx + 2, ty + 2);
        g.setColor(color);
        g.drawString(msg, tx, ty);
    }
}
