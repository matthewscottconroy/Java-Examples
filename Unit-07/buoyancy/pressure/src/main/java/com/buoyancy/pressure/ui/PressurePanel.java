package com.buoyancy.pressure.ui;

import com.buoyancy.pressure.model.FluidMedium;
import com.buoyancy.pressure.model.PressureBody;
import com.buoyancy.pressure.physics.PressurePhysics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Main canvas for the pressure-differential buoyancy simulator.
 *
 * <h2>Visualisation layers (bottom to top)</h2>
 * <ol>
 *   <li>Sky (above fluid surface)</li>
 *   <li>Pressure heat-map — every pixel row in the fluid is coloured by its
 *       pressure: light cyan at the surface, deep purple/indigo at depth</li>
 *   <li>Optional grid overlay showing cell boundaries</li>
 *   <li>Cylinder objects (semi-transparent so the pressure field shows through)</li>
 *   <li>Pressure face-arrows on top and bottom faces of selected object</li>
 *   <li>Net force arrow + equilibrium indicator</li>
 *   <li>Colour-bar legend (right edge)</li>
 *   <li>HUD / key hints</li>
 * </ol>
 *
 * <h2>Interaction</h2>
 * <ul>
 *   <li>Click object → select; drag → move (physics resumes on release)</li>
 *   <li>Click empty space → deselect; drag near surface line → adjust level</li>
 *   <li>Right-click object → delete</li>
 *   <li>Keyboard: A = add, Del = remove, P = toggle physics, G = toggle grid,
 *       F = toggle face arrows, R = reset physics</li>
 * </ul>
 */
public class PressurePanel extends JPanel {

    // ── Canvas ────────────────────────────────────────────────────────────────
    public static final int CANVAS_W       = 760;
    public static final int CANVAS_H       = 700;
    public static final int DEFAULT_SURF_Y = 200;
    public static final int FLOOR_Y        = 645;
    private static final int CEIL_Y        = 10;
    private static final int LEGEND_W      = 22;
    private static final int FPS           = 60;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color SKY_TOP  = new Color(15, 22, 45);
    private static final Color SKY_BOT  = new Color(38, 72, 128);
    private static final Color FLOOR_COL= new Color(95, 72, 45);

    static final Color[] PALETTE = {
        new Color(255, 140,  0),
        new Color( 70, 210, 90),
        new Color(200,  80, 215),
        new Color(255, 205,  0),
        new Color( 70, 200, 255),
        new Color(255,  90,  90),
        new Color(  0, 200, 155),
    };

    // ── State ─────────────────────────────────────────────────────────────────
    final List<PressureBody> bodies = new ArrayList<>();
    FluidMedium fluid    = new FluidMedium(1000.0, DEFAULT_SURF_Y);
    double gravity       = 9.81;
    boolean physicsOn    = false;   // toggle physics animation
    boolean showGrid     = false;   // overlay grid lines
    boolean showFaceArrows = true;  // show distributed face arrows
    int  cellSizePx      = 20;      // grid cell size for face arrows

    private PressureBody  selected;
    private int   colorIdx = 0;
    private int   dragOffX, dragOffY;
    private boolean draggingSurface = false;

    // ── Rendering ─────────────────────────────────────────────────────────────
    private BufferedImage buf;
    private Graphics2D    bg;
    private BufferedImage pressureImg;   // cached heatmap
    private boolean       heatmapDirty = true;
    private final Timer   gameTimer;
    private long          lastNano = System.nanoTime();

    Runnable onTick;

    // ── Construction ──────────────────────────────────────────────────────────

    public PressurePanel() {
        setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
        setFocusable(true);

        buf = new BufferedImage(CANVAS_W, CANVAS_H, BufferedImage.TYPE_INT_RGB);
        bg  = buf.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        bg.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Seed objects
        addBodyAt(CANVAS_W * 0.38, DEFAULT_SURF_Y + 60, 0.24, 0.45, 530.0, "Pine");
        addBodyAt(CANVAS_W * 0.67, DEFAULT_SURF_Y + 140, 0.18, 0.36, 2700.0, "Aluminium");

        setupMouse();

        gameTimer = new Timer(1000 / FPS, e -> tick());
        gameTimer.start();
    }

    // ── Tick ─────────────────────────────────────────────────────────────────

    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min((now - lastNano) / 1e9, 0.05);
        lastNano = now;

        if (physicsOn) {
            for (PressureBody b : bodies) {
                PressurePhysics.step(b, fluid, gravity, dt, FLOOR_Y, CEIL_Y);
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
        drawPressureField(g);
        if (showGrid) drawGrid(g);
        drawFloor(g);
        for (PressureBody b : bodies) drawBody(g, b);
        if (selected != null) {
            drawEquilibriumLine(g, selected);
            if (showFaceArrows) drawFaceArrows(g, selected);
            drawNetForceArrow(g, selected);
        }
        drawColorLegend(g);
        drawHUD(g);
    }

    // ── Sky ──────────────────────────────────────────────────────────────────

    private void drawSky(Graphics2D g) {
        int surf = fluid.getSurfaceY();
        GradientPaint gp = new GradientPaint(0, 0, SKY_TOP, 0, surf, SKY_BOT);
        g.setPaint(gp);
        g.fillRect(0, 0, CANVAS_W, surf + 5);

        // Depth label at surface
        g.setColor(new Color(200, 230, 255, 140));
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.drawString("← fluid surface   depth = 0", 8, surf - 4);
    }

    // ── Pressure heat-map ────────────────────────────────────────────────────

    private void drawPressureField(Graphics2D g) {
        if (heatmapDirty || pressureImg == null
                || pressureImg.getWidth() != CANVAS_W || pressureImg.getHeight() != CANVAS_H) {
            buildHeatmap();
        }
        g.drawImage(pressureImg, 0, 0, null);
    }

    private void buildHeatmap() {
        pressureImg = new BufferedImage(CANVAS_W, CANVAS_H, BufferedImage.TYPE_INT_ARGB);
        double maxP = fluid.maxPressure(FLOOR_Y, gravity, PressureBody.PPM);

        for (int py = 0; py < CANVAS_H; py++) {
            double pressure = PressurePhysics.pressureAtPixelY(py, fluid, gravity);
            int argb = py < fluid.getSurfaceY() ? 0x00000000 : pressureArgb(pressure, maxP);
            for (int px = 0; px < CANVAS_W - LEGEND_W - 4; px++) {
                pressureImg.setRGB(px, py, argb);
            }
        }
        // Depth scale labels
        heatmapDirty = false;
    }

    /** Maps pressure [0, maxP] to an ARGB colour: cyan (0) → blue → indigo → deep purple (maxP). */
    private int pressureArgb(double p, double maxP) {
        double t = maxP > 0 ? Math.min(1.0, p / maxP) : 0;
        int r = (int)(20  + t * 55);
        int gr= (int)(130 - t * 120);
        int b = (int)(210 - t * 30);
        return (220 << 24) | (r << 16) | (gr << 8) | b;
    }

    // ── Grid overlay ─────────────────────────────────────────────────────────

    private void drawGrid(Graphics2D g) {
        g.setColor(new Color(255, 255, 255, 20));
        Stroke s0 = g.getStroke();
        g.setStroke(new BasicStroke(0.5f));
        int lx = CANVAS_W - LEGEND_W - 4;
        for (int y = fluid.getSurfaceY(); y < FLOOR_Y; y += cellSizePx) {
            g.drawLine(0, y, lx, y);
        }
        for (int x = 0; x < lx; x += cellSizePx) {
            g.drawLine(x, fluid.getSurfaceY(), x, FLOOR_Y);
        }
        g.setStroke(s0);
    }

    // ── Floor ────────────────────────────────────────────────────────────────

    private void drawFloor(Graphics2D g) {
        GradientPaint gp = new GradientPaint(0, FLOOR_Y, FLOOR_COL, 0, CANVAS_H,
                                              FLOOR_COL.darker().darker());
        g.setPaint(gp);
        g.fillRect(0, FLOOR_Y, CANVAS_W - LEGEND_W - 4, CANVAS_H - FLOOR_Y);
    }

    // ── Cylinder body (semi-transparent) ─────────────────────────────────────

    private void drawBody(Graphics2D g, PressureBody body) {
        int x   = (int) body.getLeftX();
        int top = (int) body.getY();
        int w   = body.getWidthPx();
        int h   = body.getHeightPx();
        int ry  = Math.max(5, w / 9);

        Color base  = body.getColor();
        Color light = blend(base, Color.WHITE, 0.3f);
        Color dark  = blend(base, Color.BLACK, 0.35f);

        // Semi-transparent body so pressure field shows through
        Composite oldComp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.72f));

        GradientPaint gp = new GradientPaint(x, top, light, x + w, top, dark);
        g.setPaint(gp);
        g.fillRect(x, top + ry, w, h - ry);
        g.setColor(dark.darker());
        g.fillOval(x, top + h - ry, w, 2 * ry);
        g.setPaint(new GradientPaint(x, top, light, x + w, top + ry, base));
        g.fillOval(x, top, w, 2 * ry);

        g.setComposite(oldComp);

        // Outline
        Stroke s0 = g.getStroke();
        if (body.isSelected()) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2.5f));
        } else {
            g.setColor(new Color(dark.getRed(), dark.getGreen(), dark.getBlue(), 200));
            g.setStroke(new BasicStroke(1.2f));
        }
        g.drawRect(x, top + ry, w, h - ry);
        g.drawOval(x, top, w, 2 * ry);
        g.setStroke(s0);

        // Label
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        String lbl = body.getName();
        int lw = fm.stringWidth(lbl);
        g.setColor(Color.WHITE);
        g.drawString(lbl, (int) body.getCx() - lw / 2, top + h / 2 + 4);
    }

    // ── Distributed face arrows ───────────────────────────────────────────────

    /**
     * Draws distributed pressure arrows on the top and bottom faces of the
     * selected body, and lateral arrows on the side faces (to show they cancel).
     *
     * <p>Each arrow represents one {@link #cellSizePx}-wide column of the face.
     * Arrow length ∝ pressure at that face depth.
     */
    private void drawFaceArrows(Graphics2D g, PressureBody body) {
        double pBottom = PressurePhysics.bottomFacePressure(body, fluid, gravity);
        double pTop    = PressurePhysics.topFacePressure(body, fluid, gravity);
        double maxP    = fluid.maxPressure(FLOOR_Y, gravity, PressureBody.PPM);
        if (maxP == 0) return;

        int x   = (int) body.getLeftX();
        int w   = body.getWidthPx();
        int bot = (int) body.getBottomY();
        int top = (int) body.getY();

        double lenBottom = 10 + (pBottom / maxP) * 70;
        double lenTop    = 10 + (pTop    / maxP) * 70;

        // ── Bottom face: upward green arrows ─────────────────────────────
        g.setColor(new Color(60, 230, 90, 220));
        Stroke s0 = g.getStroke();
        g.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
        for (int ax = x + cellSizePx / 2; ax < x + w; ax += cellSizePx) {
            int ay = bot;
            int endY = (int)(ay - lenBottom);
            g.drawLine(ax, ay, ax, endY);
            // Arrowhead
            g.fillPolygon(new int[]{ax-4, ax+4, ax}, new int[]{endY+8, endY+8, endY}, 3);
        }

        // ── Top face: downward red arrows (if submerged) ──────────────────
        if (body.getY() > fluid.getSurfaceY()) {
            g.setColor(new Color(230, 70, 70, 220));
            for (int ax = x + cellSizePx / 2; ax < x + w; ax += cellSizePx) {
                int ay = top;
                int endY = (int)(ay + lenTop);
                g.drawLine(ax, ay, ax, endY);
                g.fillPolygon(new int[]{ax-4, ax+4, ax}, new int[]{endY-8, endY-8, endY}, 3);
            }
        }

        // ── Side face arrows (show they cancel) ───────────────────────────
        int ry = Math.max(5, w / 9);
        g.setStroke(new BasicStroke(1.2f));
        int sideSteps = Math.max(2, (int) body.getHeightPx() / cellSizePx);
        double dY = body.getHeightPx() / (double) sideSteps;
        for (int i = 0; i < sideSteps; i++) {
            double fy = body.getY() + ry + i * dY + dY / 2;
            if (fy < fluid.getSurfaceY()) continue;
            double p = PressurePhysics.pressureAtPixelY(fy, fluid, gravity);
            double len = 5 + (p / maxP) * 30;
            // Left face → rightward arrow
            g.setColor(new Color(200, 190, 60, 150));
            int lx = x, ly = (int) fy;
            g.drawLine(lx, ly, (int)(lx + len), ly);
            // Right face → leftward arrow
            int rx = x + w, ry2 = (int) fy;
            g.drawLine(rx, ry2, (int)(rx - len), ry2);
        }

        g.setStroke(s0);

        // Pressure labels on faces
        drawFaceLabel(g, x + w / 2, bot + (int)lenBottom + 12,
                      String.format("P_bottom = %.0f Pa", pBottom), new Color(60, 230, 90));
        if (body.getY() > fluid.getSurfaceY()) {
            drawFaceLabel(g, x + w / 2, top - (int)lenTop - 6,
                          String.format("P_top = %.0f Pa", pTop), new Color(230, 70, 70));
        }
    }

    private void drawFaceLabel(Graphics2D g, int cx, int y, String text, Color c) {
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 10));
        FontMetrics fm = g.getFontMetrics();
        int lw = fm.stringWidth(text);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(cx - lw/2 - 3, y - 12, lw + 6, 15, 4, 4);
        g.setColor(c);
        g.drawString(text, cx - lw / 2, y);
    }

    // ── Net force arrow ───────────────────────────────────────────────────────

    private void drawNetForceArrow(Graphics2D g, PressureBody body) {
        double fb  = PressurePhysics.netBuoyancyArchimedes(body, fluid, gravity);
        double fg  = PressurePhysics.gravitationalForce(body, gravity);
        double fn  = fg - fb;  // positive = down

        double cx  = body.getCx() + body.getWidthPx() / 2.0 + 30;
        double midY = body.getY() + body.getHeightPx() / 2.0;
        double len  = Math.min(130, Math.sqrt(Math.abs(fn)) * 7);

        if (Math.abs(fn) > 0.5) {
            Color nc = new Color(255, 215, 0);
            String label = String.format("F_net = %.1f N %s", Math.abs(fn), fn < 0 ? "↑" : "↓");
            double endY = fn < 0 ? midY - len : midY + len;
            Stroke s0 = g.getStroke();
            g.setColor(nc);
            g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine((int)cx, (int)midY, (int)cx, (int)endY);
            int dir = fn < 0 ? -1 : 1;
            g.fillPolygon(new int[]{(int)cx-8,(int)cx+8,(int)cx},
                          new int[]{(int)endY+dir*14,(int)endY+dir*14,(int)endY}, 3);
            g.setStroke(s0);
            drawFaceLabel(g, (int)cx + 50, (int)((midY + endY) / 2), label, nc);
        }
    }

    // ── Equilibrium line ─────────────────────────────────────────────────────

    private void drawEquilibriumLine(Graphics2D g, PressureBody body) {
        double eqY = PressurePhysics.equilibriumY(body, fluid);
        if (Double.isNaN(eqY)) return;
        int lx = (int) body.getLeftX() - 10, rx = (int) body.getRightX() + 10, ey = (int) eqY;
        Stroke s0 = g.getStroke();
        g.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                    10, new float[]{6, 4}, 0));
        g.setColor(new Color(255, 255, 100, 160));
        g.drawLine(lx, ey, rx, ey);
        g.setStroke(s0);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g.drawString("equilibrium", lx, ey - 3);
    }

    // ── Colour legend ─────────────────────────────────────────────────────────

    private void drawColorLegend(Graphics2D g) {
        int lx = CANVAS_W - LEGEND_W;
        int ly = fluid.getSurfaceY();
        int lh = FLOOR_Y - ly;
        double maxP = fluid.maxPressure(FLOOR_Y, gravity, PressureBody.PPM);

        // Gradient bar
        for (int dy = 0; dy < lh; dy++) {
            double t = (double) dy / lh;
            int argb = pressureArgb(t * maxP, maxP);
            g.setColor(new Color(argb, true));
            g.fillRect(lx, ly + dy, LEGEND_W, 1);
        }
        g.setColor(new Color(100, 120, 160));
        g.drawRect(lx, ly, LEGEND_W, lh);

        // Labels
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g.setColor(new Color(200, 220, 255));
        g.drawString("0", lx + 2, ly + 10);
        g.drawString(String.format("%.0f", maxP / 1000) + "kPa", lx + 1, ly + lh - 2);
        g.drawString("P", lx + 6, ly + lh / 2);
    }

    // ── HUD ──────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g) {
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g.setColor(new Color(180, 200, 200, 160));
        String hint = "[A] Add   [Del] Remove   [P] " + (physicsOn ? "Stop Physics" : "Start Physics")
                    + "   [G] Grid   [F] Arrows   [R] Reset";
        g.drawString(hint, 6, CANVAS_H - 7);
    }

    // ── Object management ─────────────────────────────────────────────────────

    public void addBodyAt(double cx, double y, double r, double h, double density, String name) {
        if (bodies.size() >= PALETTE.length) return;
        PressureBody b = new PressureBody(cx, y, r, h, density, name,
                                          PALETTE[colorIdx % PALETTE.length]);
        colorIdx++;
        bodies.add(b);
    }

    public void addBody() {
        double cx = CANVAS_W / 2.0 + (bodies.size() % 3 - 1) * 120;
        addBodyAt(cx, DEFAULT_SURF_Y + 60, 0.22, 0.40, 530.0, "Object " + (bodies.size() + 1));
        selectBody(bodies.get(bodies.size() - 1));
    }

    public void removeSelected() {
        if (selected == null) return;
        bodies.remove(selected);
        selected = null;
    }

    public void resetPhysics() {
        for (PressureBody b : bodies) b.setVy(0.0);
    }

    public void deselectAll() {
        if (selected != null) selected.setSelected(false);
        selected = null;
    }

    public void selectBody(PressureBody b) {
        deselectAll();
        selected = b;
        if (b != null) b.setSelected(true);
    }

    // ── Mouse ────────────────────────────────────────────────────────────────

    private void setupMouse() {
        MouseAdapter ma = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                for (int i = bodies.size() - 1; i >= 0; i--) {
                    PressureBody b = bodies.get(i);
                    if (b.contains(e.getX(), e.getY())) {
                        if (SwingUtilities.isRightMouseButton(e)) {
                            selectBody(b); removeSelected(); return;
                        }
                        selectBody(b);
                        b.setPinned(true);
                        dragOffX = (int)(e.getX() - b.getCx());
                        dragOffY = (int)(e.getY() - b.getY());
                        return;
                    }
                }
                if (Math.abs(e.getY() - fluid.getSurfaceY()) < 18) {
                    draggingSurface = true; return;
                }
                deselectAll();
            }

            @Override public void mouseDragged(MouseEvent e) {
                if (draggingSurface) {
                    int ny = Math.max(60, Math.min(FLOOR_Y - 60, e.getY()));
                    fluid.setSurfaceY(ny);
                    heatmapDirty = true;
                    return;
                }
                if (selected != null && selected.isPinned()) {
                    selected.setCx(e.getX() - dragOffX);
                    double ny = e.getY() - dragOffY;
                    selected.setY(Math.max(CEIL_Y, Math.min(FLOOR_Y - selected.getHeightPx(), ny)));
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

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_P -> { physicsOn = !physicsOn; for (PressureBody b : bodies) b.setVy(0); }
                    case KeyEvent.VK_A -> addBody();
                    case KeyEvent.VK_R -> resetPhysics();
                    case KeyEvent.VK_G -> showGrid = !showGrid;
                    case KeyEvent.VK_F -> showFaceArrows = !showFaceArrows;
                    case KeyEvent.VK_DELETE, KeyEvent.VK_BACK_SPACE -> removeSelected();
                }
            }
        });
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public PressureBody getSelectedBody()    { return selected; }
    public FluidMedium  getFluid()           { return fluid; }
    public double       getGravity()         { return gravity; }
    public void         setGravity(double g) { this.gravity = g; heatmapDirty = true; }
    public void         setOnTick(Runnable r){ this.onTick = r; }
    public void         markHeatmapDirty()   { heatmapDirty = true; }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private static Color blend(Color a, Color b, float t) {
        float ti = 1f - t;
        return new Color(
            clamp((int)(a.getRed()   * ti + b.getRed()   * t)),
            clamp((int)(a.getGreen() * ti + b.getGreen() * t)),
            clamp((int)(a.getBlue()  * ti + b.getBlue()  * t)));
    }

    private static int clamp(int v) { return Math.max(0, Math.min(255, v)); }
}
