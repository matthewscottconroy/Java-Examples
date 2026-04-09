package com.buoyancy.equation.ui;

import com.buoyancy.equation.model.BuoyancyObject;
import com.buoyancy.equation.model.Fluid;
import com.buoyancy.equation.physics.BuoyancyPhysics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Main simulation canvas for the equation-based buoyancy program.
 *
 * <h2>Interaction</h2>
 * <ul>
 *   <li>Click on an object to select it (details appear in the control panel)</li>
 *   <li>Drag a selected object to reposition it; physics resumes on release</li>
 *   <li>Click empty space to deselect</li>
 *   <li>Drag the fluid surface line (thin area near top of fluid) to adjust level</li>
 *   <li>Right-click an object to delete it</li>
 * </ul>
 */
public class SimulationPanel extends JPanel {

    // ── Canvas dimensions ─────────────────────────────────────────────────────
    public static final int CANVAS_W        = 760;
    public static final int CANVAS_H        = 700;
    public static final int DEFAULT_SURF_Y  = 230;
    public static final int FLOOR_Y         = 645;
    private static final int CEIL_Y         = 10;
    private static final int FPS            = 60;

    // ── Colors ─────────────────────────────────────────────────────────────────
    private static final Color SKY_TOP     = new Color(15, 22, 45);
    private static final Color SKY_BOT     = new Color(40, 80, 140);
    private static final Color FLUID_TOP   = new Color(28, 95, 175, 215);
    private static final Color FLUID_DEEP  = new Color(6,  30, 70, 245);
    private static final Color FLOOR_TOP   = new Color(105, 82, 52);
    private static final Color FLOOR_BOT   = new Color(72, 54, 30);
    private static final Color WAVE_LINE   = new Color(130, 215, 255, 180);

    // ── Object color palette ───────────────────────────────────────────────────
    static final Color[] PALETTE = {
        new Color(255, 140,  0),
        new Color( 70, 210, 90),
        new Color(200,  80, 215),
        new Color(255, 205,  0),
        new Color( 70, 200, 255),
        new Color(255,  90,  90),
        new Color(  0, 200, 155),
        new Color(255, 165,  70),
    };

    // ── State ─────────────────────────────────────────────────────────────────
    final List<BuoyancyObject> objects = new ArrayList<>();
    Fluid  fluid   = new Fluid(Fluid.DENSITY_FRESH_WATER, DEFAULT_SURF_Y);
    double gravity = 9.81;
    boolean paused = false;

    private BuoyancyObject selected;
    private int  colorIdx   = 0;
    private int  dragOffX, dragOffY;
    private boolean draggingSurface = false;

    // ── Rendering ──────────────────────────────────────────────────────────────
    private BufferedImage buf;
    private Graphics2D    bg;
    private final Timer   gameTimer;
    private long          lastNano = System.nanoTime();

    Runnable onTick;   // called each tick (e.g. to update control panel metrics)

    // ── Construction ──────────────────────────────────────────────────────────

    public SimulationPanel() {
        setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
        setFocusable(true);

        buf = new BufferedImage(CANVAS_W, CANVAS_H, BufferedImage.TYPE_INT_RGB);
        bg  = buf.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Seed with two demonstration objects
        addObjectAt(CANVAS_W * 0.35, DEFAULT_SURF_Y - 10, 0.25, 0.50, 530.0,  "Pine");
        addObjectAt(CANVAS_W * 0.65, DEFAULT_SURF_Y + 40, 0.20, 0.40, 7850.0, "Steel");

        setupMouse();

        gameTimer = new Timer(1000 / FPS, e -> tick());
        gameTimer.start();
    }

    // ── Physics tick ──────────────────────────────────────────────────────────

    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min((now - lastNano) / 1e9, 0.05);
        lastNano = now;

        fluid.tickWave(dt);

        if (!paused) {
            for (BuoyancyObject obj : objects) {
                BuoyancyPhysics.step(obj, fluid, gravity, dt, FLOOR_Y, CEIL_Y);
            }
        }

        if (onTick != null) onTick.run();
        repaint();
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        renderFrame(bg);
        g0.drawImage(buf, 0, 0, null);
    }

    private void renderFrame(Graphics2D g) {
        drawSky(g);
        drawFluidBody(g);
        drawFloor(g);
        for (BuoyancyObject obj : objects) drawObject(g, obj);
        if (selected != null) {
            drawEquilibriumLine(g, selected);
            drawForceArrows(g, selected);
        }
        for (BuoyancyObject obj : objects) drawLabel(g, obj);
        drawWaveSurface(g);
        drawHUD(g);
    }

    // ── Sky ──────────────────────────────────────────────────────────────────

    private void drawSky(Graphics2D g) {
        int surf = fluid.getSurfaceY();
        GradientPaint gp = new GradientPaint(0, 0, SKY_TOP, 0, surf, SKY_BOT);
        g.setPaint(gp);
        g.fillRect(0, 0, CANVAS_W, surf + 10);

        // Subtle stars using deterministic LCG
        g.setColor(new Color(255, 255, 255, 55));
        long s = 12345L;
        for (int i = 0; i < 90; i++) {
            s = s * 6364136223846793005L + 1;
            int sx = (int) ((s >>> 34) % CANVAS_W);
            s = s * 6364136223846793005L + 1;
            int sy = (int) ((s >>> 34) % Math.max(1, surf - 20));
            g.fillRect(sx, sy, 1, 1);
        }
    }

    // ── Fluid body ────────────────────────────────────────────────────────────

    private void drawFluidBody(Graphics2D g) {
        // Build wave polygon
        int step = 3;
        int n = CANVAS_W / step + 2;
        int[] xs = new int[n + 4];
        int[] ys = new int[n + 4];
        for (int i = 0; i < n; i++) {
            xs[i] = i * step;
            ys[i] = (int) fluid.getWaveY(xs[i]);
        }
        xs[n] = CANVAS_W;  ys[n] = FLOOR_Y;
        xs[n+1] = CANVAS_W; ys[n+1] = CANVAS_H;
        xs[n+2] = 0;        ys[n+2] = CANVAS_H;
        xs[n+3] = 0;        ys[n+3] = FLOOR_Y;

        GradientPaint gp = new GradientPaint(0, fluid.getSurfaceY(), FLUID_TOP,
                                              0, FLOOR_Y, FLUID_DEEP);
        g.setPaint(gp);
        g.fillPolygon(xs, ys, n + 4);
    }

    private void drawWaveSurface(Graphics2D g) {
        g.setColor(WAVE_LINE);
        g.setStroke(new BasicStroke(1.5f));
        Path2D path = new Path2D.Double();
        path.moveTo(0, fluid.getWaveY(0));
        for (int x = 1; x < CANVAS_W; x++) path.lineTo(x, fluid.getWaveY(x));
        g.draw(path);
        // Shimmer
        g.setColor(new Color(220, 250, 255, 70));
        long s = (long)(fluid.getWavePhase() * 1000) & 0xFFFF;
        for (int i = 0; i < 18; i++) {
            s = s * 6364136223846793005L + 1;
            int wx = (int) ((s >>> 33) % CANVAS_W);
            int wy = (int) fluid.getWaveY(wx);
            g.fillOval(wx - 2, wy - 1, 5, 2);
        }
    }

    // ── Floor ─────────────────────────────────────────────────────────────────

    private void drawFloor(Graphics2D g) {
        GradientPaint gp = new GradientPaint(0, FLOOR_Y, FLOOR_TOP, 0, CANVAS_H, FLOOR_BOT);
        g.setPaint(gp);
        g.fillRect(0, FLOOR_Y, CANVAS_W, CANVAS_H - FLOOR_Y);
        // Pebble texture
        long s = 77777L;
        g.setColor(new Color(85, 65, 40, 120));
        for (int i = 0; i < 40; i++) {
            s = s * 6364136223846793005L + 1; int px = (int)((s>>>33) % CANVAS_W);
            s = s * 6364136223846793005L + 1; int py = FLOOR_Y + (int)((s>>>34) % 30);
            s = s * 6364136223846793005L + 1; int pr = 2 + (int)((s>>>35) % 5);
            g.fillOval(px, py, pr * 2, pr);
        }
    }

    // ── Cylinder rendering ────────────────────────────────────────────────────

    private void drawObject(Graphics2D g, BuoyancyObject obj) {
        int x   = (int) obj.getLeftX();
        int top = (int) obj.getY();
        int w   = obj.getWidthPx();
        int h   = obj.getHeightPx();
        int ry  = Math.max(5, w / 9);   // perspective ellipse y-radius

        Color base  = obj.getColor();
        Color light = blend(base, Color.WHITE, 0.32f);
        Color dark  = blend(base, Color.BLACK, 0.35f);
        Color vdark = blend(base, Color.BLACK, 0.55f);

        // ── Body (horizontal gradient = cylindrical sheen) ────────────────
        GradientPaint gp = new GradientPaint(x, top, light, x + w, top, dark);
        g.setPaint(gp);
        g.fillRect(x, top + ry, w, h - ry);

        // ── Bottom rim ────────────────────────────────────────────────────
        g.setColor(vdark);
        g.fillOval(x, top + h - ry, w, 2 * ry);

        // ── Submerged overlay (blue tint) ─────────────────────────────────
        double surfY = fluid.getSurfaceY();
        if (obj.getBottomY() > surfY) {
            double ovTop = Math.max(top + ry, surfY);
            double ovBot = obj.getBottomY() + ry;
            Shape oldClip = g.getClip();
            g.clip(new Rectangle(x - 1, (int) ovTop, w + 2, (int)(ovBot - ovTop + 1)));
            g.setColor(new Color(15, 60, 180, 75));
            g.fillRect(x, top, w, h + 2 * ry);
            g.setClip(oldClip);
        }

        // ── Top face (ellipse) ────────────────────────────────────────────
        g.setPaint(new GradientPaint(x, top, light, x + w, top + ry, base));
        g.fillOval(x, top, w, 2 * ry);
        // Specular highlight on top
        g.setColor(new Color(255, 255, 255, 50));
        g.fillOval(x + w / 4, top + ry / 4, w / 2, ry / 2);

        // ── Outline ───────────────────────────────────────────────────────
        Stroke s0 = g.getStroke();
        if (obj.isSelected()) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            // Glow
            g.setColor(new Color(255, 255, 200, 60));
            g.setStroke(new BasicStroke(6.0f));
            g.drawRect(x, top + ry, w, h - ry);
            g.drawOval(x, top, w, 2 * ry);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        } else {
            g.setColor(vdark);
            g.setStroke(new BasicStroke(1.2f));
        }
        g.drawRect(x, top + ry, w, h - ry);
        g.drawOval(x, top, w, 2 * ry);
        g.setStroke(s0);
    }

    private void drawLabel(Graphics2D g, BuoyancyObject obj) {
        int cx  = (int) obj.getCx();
        int cy  = (int) (obj.getY() + obj.getHeightPx() / 2.0);
        String line1 = obj.getName();
        String line2 = String.format("%.0f kg/m³", obj.getDensityKgM3());

        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        int w1 = fm.stringWidth(line1);
        int w2 = fm.stringWidth(line2);

        // Shadow
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(line1, cx - w1 / 2 + 1, cy - 5 + 1);
        g.drawString(line2, cx - w2 / 2 + 1, cy + 9 + 1);
        g.setColor(Color.WHITE);
        g.drawString(line1, cx - w1 / 2, cy - 5);
        g.drawString(line2, cx - w2 / 2, cy + 9);
    }

    // ── Force arrows ──────────────────────────────────────────────────────────

    private void drawForceArrows(Graphics2D g, BuoyancyObject obj) {
        double cx   = obj.getCx();
        double midY = obj.getY() + obj.getHeightPx() / 2.0;

        double fb = BuoyancyPhysics.buoyantForce(obj, fluid, gravity);
        double fg = BuoyancyPhysics.gravitationalForce(obj, gravity);
        double fn = fg - fb;

        // Arrow length: sqrt scale, capped at 160px
        double fbLen = Math.min(160, Math.sqrt(fb) * 7);
        double fgLen = Math.min(160, Math.sqrt(fg) * 7);
        double fnLen = Math.min(160, Math.sqrt(Math.abs(fn)) * 7);

        // Buoyancy — green, left of center
        if (fb > 0.5) {
            drawArrowV(g, cx - 22, midY, midY - fbLen,
                new Color(50, 230, 90), String.format("F_b = %.1f N", fb));
        }
        // Gravity — red, right of center
        drawArrowV(g, cx + 22, midY, midY + fgLen,
            new Color(230, 60, 60), String.format("F_g = %.1f N", fg));
        // Net — yellow, center
        if (Math.abs(fn) > 0.5) {
            Color nc = new Color(255, 215, 0);
            String nl = String.format("F_net = %.1f N", Math.abs(fn));
            drawArrowV(g, cx, midY, fn < 0 ? midY - fnLen : midY + fnLen, nc, nl);
        }
    }

    private void drawArrowV(Graphics2D g, double x, double from, double to, Color color, String label) {
        boolean up = to < from;
        int ix = (int) x, iy1 = (int) from, iy2 = (int) to;

        Stroke s0 = g.getStroke();
        g.setColor(color);
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.drawLine(ix, iy1, ix, iy2);

        // Arrowhead
        int dir = up ? -1 : 1;
        int[] ax = {ix - 7, ix + 7, ix};
        int[] ay = {iy2 + dir * 13, iy2 + dir * 13, iy2};
        g.fillPolygon(ax, ay, 3);
        g.setStroke(s0);

        // Label
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        int lx = ix + 14;
        int ly = (int) ((from + to) / 2) + 4;
        int lw = fm.stringWidth(label);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(lx - 2, ly - 13, lw + 6, 17, 4, 4);
        g.setColor(color);
        g.drawString(label, lx + 1, ly);
    }

    // ── Equilibrium line ─────────────────────────────────────────────────────

    private void drawEquilibriumLine(Graphics2D g, BuoyancyObject obj) {
        double eqY = BuoyancyPhysics.equilibriumY(obj, fluid);
        if (Double.isNaN(eqY)) return;

        int lx = (int) obj.getLeftX() - 12;
        int rx = (int) (obj.getLeftX() + obj.getWidthPx()) + 12;
        int ey = (int) eqY;

        Stroke s0 = g.getStroke();
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                    10, new float[]{6, 5}, 0));
        g.setColor(new Color(255, 255, 100, 180));
        g.drawLine(lx, ey, rx, ey);
        g.setStroke(s0);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(new Color(255, 255, 100, 200));
        g.drawString("equilibrium", lx, ey - 3);
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g) {
        String pauseHint = paused ? "▶ [P] Resume" : "[P] Pause";
        String info = pauseHint + "   [Del] Remove   [R] Reset physics   [A] Add object";
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        g.setColor(new Color(200, 200, 200, 160));
        g.drawString(info, 10, CANVAS_H - 8);
    }

    // ── Object management ─────────────────────────────────────────────────────

    /** Adds a new cylinder with default size at the given screen position. */
    public void addObjectAt(double cx, double y, double r, double h, double density, String name) {
        if (objects.size() >= PALETTE.length) return;
        Color c = PALETTE[colorIdx % PALETTE.length];
        colorIdx++;
        BuoyancyObject obj = new BuoyancyObject(cx, y, r, h, density, name, c);
        objects.add(obj);
    }

    /** Adds a default new Pine cylinder just below the surface at canvas center. */
    public void addObject() {
        double cx = CANVAS_W / 2.0 + (objects.size() % 3 - 1) * 120;
        addObjectAt(cx, DEFAULT_SURF_Y - 5, 0.22, 0.44, 530.0, "Object " + (objects.size() + 1));
        selectObject(objects.get(objects.size() - 1));
    }

    public void removeSelected() {
        if (selected == null) return;
        objects.remove(selected);
        selected = null;
    }

    public void resetPhysics() {
        for (BuoyancyObject obj : objects) obj.setVy(0.0);
    }

    public void deselectAll() {
        if (selected != null) selected.setSelected(false);
        selected = null;
    }

    public void selectObject(BuoyancyObject obj) {
        deselectAll();
        selected = obj;
        if (obj != null) obj.setSelected(true);
    }

    // ── Mouse ─────────────────────────────────────────────────────────────────

    private void setupMouse() {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                // Check objects back-to-front
                for (int i = objects.size() - 1; i >= 0; i--) {
                    BuoyancyObject obj = objects.get(i);
                    if (obj.contains(e.getX(), e.getY())) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            selectObject(obj);
                            removeSelected();
                            return;
                        }
                        selectObject(obj);
                        obj.setPinned(true);
                        dragOffX = (int)(e.getX() - obj.getCx());
                        dragOffY = (int)(e.getY() - obj.getY());
                        return;
                    }
                }
                // Check fluid surface drag zone
                int surf = fluid.getSurfaceY();
                if (Math.abs(e.getY() - surf) < 18) {
                    draggingSurface = true;
                    return;
                }
                deselectAll();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (draggingSurface) {
                    int ny = Math.max(80, Math.min(FLOOR_Y - 60, e.getY()));
                    fluid.setSurfaceY(ny);
                    return;
                }
                if (selected != null && selected.isPinned()) {
                    selected.setCx(e.getX() - dragOffX);
                    double newY = e.getY() - dragOffY;
                    selected.setY(Math.max(CEIL_Y, Math.min(FLOOR_Y - selected.getHeightPx(), newY)));
                    selected.setVy(0.0);
                }
            }

            @Override public void mouseReleased(MouseEvent e) {
                draggingSurface = false;
                if (selected != null) selected.setPinned(false);
            }
        };
        addMouseListener(ma);
        addMouseMotionListener(ma);

        // Keyboard
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_P      -> paused = !paused;
                    case KeyEvent.VK_A      -> addObject();
                    case KeyEvent.VK_R      -> resetPhysics();
                    case KeyEvent.VK_DELETE,
                         KeyEvent.VK_BACK_SPACE -> removeSelected();
                }
            }
        });
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public BuoyancyObject getSelectedObject() { return selected; }
    public Fluid getFluid()                   { return fluid; }
    public double getGravity()                { return gravity; }
    public void   setGravity(double g)        { this.gravity = g; }
    public boolean isPaused()                 { return paused; }
    public void   togglePause()               { paused = !paused; }
    public void   setOnTick(Runnable r)       { this.onTick = r; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static Color blend(Color a, Color b, float t) {
        float ti = 1f - t;
        return new Color(
            clamp((int)(a.getRed()   * ti + b.getRed()   * t)),
            clamp((int)(a.getGreen() * ti + b.getGreen() * t)),
            clamp((int)(a.getBlue()  * ti + b.getBlue()  * t)));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
