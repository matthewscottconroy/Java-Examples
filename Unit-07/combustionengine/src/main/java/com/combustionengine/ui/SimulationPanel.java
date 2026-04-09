package com.combustionengine.ui;

import com.combustionengine.model.*;
import com.combustionengine.physics.EnginePhysics;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * Main simulation canvas for the combustion engine simulator.
 *
 * <h2>Layout</h2>
 * <ul>
 *   <li><b>Top section</b>: Animated cross-section of all cylinders, pistons,
 *       connecting rods, and the shared crankshaft.</li>
 *   <li><b>Bottom section</b>: P-V diagram showing the idealised Otto cycle
 *       with a live dot tracking cylinder 0's thermodynamic state.</li>
 * </ul>
 *
 * <h2>Keyboard shortcuts</h2>
 * <ul>
 *   <li>{@code Space} — start engine (starter motor pulse)</li>
 *   <li>{@code P} — pause / resume</li>
 *   <li>{@code R} — reset to cold start</li>
 * </ul>
 */
public final class SimulationPanel extends JPanel {

    // ── Canvas dimensions ─────────────────────────────────────────────────────
    public  static final int CANVAS_W   = 780;
    public  static final int CANVAS_H   = 680;
    private static final int CYL_H      = 415;   // height of cylinder section
    private static final int PV_TOP     = 432;   // y-start of P-V diagram panel
    private static final int FPS        = 60;

    // ── Cylinder rendering geometry ───────────────────────────────────────────
    private static final int BORE_PX    = 72;    // inner bore width (px)
    private static final int WALL_PX    = 13;    // wall thickness (px)
    private static final int PISTON_H   = 26;    // piston height (px)
    private static final int STROKE_PX  = 122;   // display stroke (px)
    private static final int ROD_PX     = 136;   // display rod length (px)
    private static final int CRANK_R    = 61;    // crank throw px  = STROKE_PX / 2
    private static final int HEAD_Y     = 52;    // y of top of bore
    private static final int CRANK_CY   = HEAD_Y + STROKE_PX + ROD_PX;  // 310
    private static final int JOURNAL_R  = 11;    // main journal radius (px)
    private static final int PIN_R      = 7;     // crank-pin radius (px)
    private static final int PORT_D     = 12;    // port indicator diameter (px)
    private static final int PORT_Y     = HEAD_Y - 18; // y of port indicators

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG          = new Color(18, 20, 28);
    private static final Color CYL_BG      = new Color(22, 26, 34);
    private static final Color WALL_HI     = new Color(78, 86, 94);
    private static final Color WALL_MID    = new Color(56, 63, 70);
    private static final Color WALL_DARK   = new Color(38, 44, 50);
    private static final Color HEAD_COL    = new Color(48, 54, 62);
    private static final Color BORE_INNER  = new Color(14, 16, 22);
    private static final Color PISTON_HI   = new Color(188, 196, 205);
    private static final Color PISTON_MID  = new Color(148, 156, 165);
    private static final Color ROD_COL     = new Color(98, 108, 116);
    private static final Color SHAFT_COL   = new Color(62, 70, 76);
    private static final Color THROW_COL   = new Color(80, 88, 96);
    private static final Color PIN_COL     = new Color(108, 118, 126);
    private static final Color CW_COL      = new Color(52, 60, 66);

    // Phase label colors
    private static final Color C_INTAKE     = new Color(70, 160, 255);
    private static final Color C_COMPRESS   = new Color(240, 200, 40);
    private static final Color C_POWER      = new Color(255, 100, 40);
    private static final Color C_EXHAUST    = new Color(160, 160, 160);

    // ── State ─────────────────────────────────────────────────────────────────
    private EngineState state;
    private boolean     paused = false;

    // ── Rendering ─────────────────────────────────────────────────────────────
    private final BufferedImage buf;
    private final Graphics2D    bg;
    private final Timer         timer;
    private long                lastNano = System.nanoTime();

    Runnable onTick;

    // ── Constructor ───────────────────────────────────────────────────────────

    public SimulationPanel(EnginePreset preset) {
        setPreferredSize(new Dimension(CANVAS_W, CANVAS_H));
        setFocusable(true);
        state = new EngineState(preset.config());

        buf = new BufferedImage(CANVAS_W, CANVAS_H, BufferedImage.TYPE_INT_RGB);
        bg  = buf.createGraphics();
        bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);
        bg.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        timer = new Timer(1000 / FPS, e -> tick());
        timer.start();

        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_SPACE -> startEngine();
                    case KeyEvent.VK_P     -> togglePause();
                    case KeyEvent.VK_R     -> reset(null);
                }
            }
        });
    }

    // ── Game loop ─────────────────────────────────────────────────────────────

    private void tick() {
        long now = System.nanoTime();
        double dt = Math.min((now - lastNano) / 1e9, 0.05) * state.simSpeed;
        lastNano = now;

        if (!paused) {
            EnginePhysics.step(state, dt);
        }
        render();
        repaint();
        if (onTick != null) onTick.run();
    }

    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(buf, 0, 0, null);
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private void render() {
        Graphics2D g2 = bg;

        // Background
        g2.setColor(BG);
        g2.fillRect(0, 0, CANVAS_W, CANVAS_H);

        // Cylinder section backdrop
        g2.setColor(CYL_BG);
        g2.fillRect(0, 0, CANVAS_W, CYL_H);

        drawCylinders(g2);
        drawPVDiagram(g2);
        drawHUD(g2);
    }

    // ── Cylinder section ──────────────────────────────────────────────────────

    private void drawCylinders(Graphics2D g2) {
        EngineConfig cfg  = state.config;
        int n             = cfg.numCylinders();
        int slotW         = CANVAS_W / n;
        double scaleM2Px  = (double) STROKE_PX / cfg.strokeM();

        // Draw shared crankshaft journal bar first (behind crank throws)
        drawCrankshaftBar(g2, n, slotW);

        // Draw each cylinder
        for (CylinderState cyl : state.cylinders) {
            int cx = slotW * cyl.index + slotW / 2;
            drawCylinder(g2, cyl, cx, scaleM2Px);
        }
    }

    private void drawCrankshaftBar(Graphics2D g2, int n, int slotW) {
        int x1 = slotW / 2 - JOURNAL_R;
        int x2 = (n - 1) * slotW + slotW / 2 + JOURNAL_R;
        int y  = CRANK_CY - JOURNAL_R;
        int h  = 2 * JOURNAL_R;
        g2.setColor(SHAFT_COL);
        g2.fillRoundRect(x1, y, x2 - x1, h, h, h);
    }

    private void drawCylinder(Graphics2D g2, CylinderState cyl, int cx, double scaleM2Px) {
        // ── Geometry ──────────────────────────────────────────────────────────
        double phi           = cyl.crankAngleRad();
        double pistonDispPx  = cyl.pistonDispM * scaleM2Px;
        int pistonTopY       = HEAD_Y + (int) pistonDispPx;
        int pistonBotY       = pistonTopY + PISTON_H;

        int crankPinX = cx + (int)(CRANK_R * Math.sin(phi));
        int crankPinY = CRANK_CY - (int)(CRANK_R * Math.cos(phi));

        int boreLeft  = cx - BORE_PX / 2;
        int boreRight = cx + BORE_PX / 2;
        int wallLeft  = boreLeft  - WALL_PX;
        int wallRight = boreRight + WALL_PX;
        int headTop   = HEAD_Y - 28;

        // ── Bore interior (drawn first so parts appear in front) ──────────────
        g2.setColor(BORE_INNER);
        g2.fillRect(boreLeft, HEAD_Y, BORE_PX, CRANK_CY - CRANK_R - HEAD_Y + 10);

        // ── Cylinder head block ────────────────────────────────────────────────
        g2.setColor(HEAD_COL);
        g2.fillRect(wallLeft, headTop, BORE_PX + 2 * WALL_PX, 28);
        // Subtle head-to-bore seam
        g2.setColor(WALL_DARK);
        g2.fillRect(wallLeft, HEAD_Y - 2, BORE_PX + 2 * WALL_PX, 2);

        // ── Cylinder walls (left and right) ───────────────────────────────────
        int wallBottom = CRANK_CY + CRANK_R + 28;
        GradientPaint leftGrad = new GradientPaint(
                wallLeft, 0, WALL_HI, boreLeft, 0, WALL_MID);
        g2.setPaint(leftGrad);
        g2.fillRect(wallLeft, HEAD_Y, WALL_PX, wallBottom - HEAD_Y);

        GradientPaint rightGrad = new GradientPaint(
                boreRight, 0, WALL_MID, wallRight, 0, WALL_HI);
        g2.setPaint(rightGrad);
        g2.fillRect(boreRight, HEAD_Y, WALL_PX, wallBottom - HEAD_Y);

        // ── Port indicators (intake, spark plug, exhaust) ─────────────────────
        // Intake port (left)
        g2.setColor(cyl.intakeOpen ? new Color(40, 210, 100) : new Color(28, 58, 44));
        g2.fillOval(boreLeft + 4, PORT_Y, PORT_D, PORT_D);
        if (cyl.intakeOpen) {
            g2.setColor(new Color(40, 210, 100, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(boreLeft + 3, PORT_Y - 1, PORT_D + 2, PORT_D + 2);
            g2.setStroke(new BasicStroke(1.0f));
        }

        // Spark plug (centre)
        boolean sparking = cyl.combustionGlow > 0.08;
        g2.setColor(sparking ? new Color(255, 245, 60) : new Color(50, 52, 58));
        g2.fillOval(cx - 5, PORT_Y + 1, 10, 10);
        if (sparking) {
            g2.setColor(new Color(255, 200, 0, (int)(cyl.combustionGlow * 120)));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(cx - 7, PORT_Y - 1, 14, 14);
            g2.setStroke(new BasicStroke(1.0f));
        }

        // Exhaust port (right)
        g2.setColor(cyl.exhaustOpen ? new Color(220, 80, 40) : new Color(60, 30, 28));
        g2.fillOval(boreRight - PORT_D - 4, PORT_Y, PORT_D, PORT_D);
        if (cyl.exhaustOpen) {
            g2.setColor(new Color(220, 80, 40, 80));
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawOval(boreRight - PORT_D - 5, PORT_Y - 1, PORT_D + 2, PORT_D + 2);
            g2.setStroke(new BasicStroke(1.0f));
        }

        // ── Gas fill (intake = blue-green charge, exhaust = orange-grey fumes) ──
        int boreH = Math.max(1, pistonTopY - HEAD_Y);
        if (cyl.gasLevel > 0.01) {
            Shape oldClip = g2.getClip();
            g2.clipRect(boreLeft, HEAD_Y, BORE_PX, boreH + 2);

            CyclePhase gasPhase = cyl.currentPhase();
            Color gasCol;
            if (gasPhase == CyclePhase.INTAKE) {
                // Fresh blue-green air-fuel charge
                int alpha = (int)(cyl.gasLevel * 130);
                gasCol = new Color(40, 160, 220, alpha);
            } else if (gasPhase == CyclePhase.EXHAUST) {
                // Brownish exhaust fumes
                int alpha = (int)(cyl.gasLevel * 100);
                gasCol = new Color(160, 110, 50, alpha);
            } else {
                // Compressed/burning charge — deeper blue held at full
                int alpha = (int)(cyl.gasLevel * 80);
                gasCol = new Color(30, 80, 180, alpha);
            }

            // Gas rises from piston top to cylinder head
            int gasTopY = HEAD_Y + (int)((1.0 - cyl.gasLevel) * boreH);
            g2.setColor(gasCol);
            g2.fillRect(boreLeft, gasTopY, BORE_PX, pistonTopY - gasTopY + 2);
            g2.setClip(oldClip);
        }

        // ── Combustion glow (sustained orange heat after ignition) ────────────
        if (cyl.combustionGlow > 0.0) {
            int glowAlpha = (int)(cyl.combustionGlow * 170);
            int glowH     = Math.max(4, boreH);
            Point2D.Float gc = new Point2D.Float(cx, HEAD_Y + glowH / 2f);
            float glowR = Math.max(BORE_PX / 2f, glowH / 2f);
            RadialGradientPaint glow = new RadialGradientPaint(gc, glowR,
                    new float[]{0f, 1f},
                    new Color[]{
                        new Color(255, 235, 60, glowAlpha),
                        new Color(255, 80, 0, 0)
                    });
            Shape oldClip = g2.getClip();
            g2.clipRect(boreLeft, HEAD_Y, BORE_PX, glowH + 2);
            g2.setPaint(glow);
            g2.fillRect(boreLeft, HEAD_Y, BORE_PX, glowH + 2);
            g2.setClip(oldClip);
        }

        // ── Explosion flash (shockwave ring at spark moment) ──────────────────
        if (cyl.explosionFlash > 0.01) {
            int flashAlpha = (int)(cyl.explosionFlash * 220);
            int ringR = (int)((1.0 - cyl.explosionFlash) * BORE_PX / 2);
            int ringThick = Math.max(2, (int)(cyl.explosionFlash * 8));
            Shape oldClip = g2.getClip();
            g2.clipRect(boreLeft, HEAD_Y, BORE_PX, boreH + 4);
            g2.setColor(new Color(255, 255, 200, flashAlpha));
            g2.setStroke(new BasicStroke(ringThick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawOval(cx - ringR, HEAD_Y + boreH / 2 - ringR, ringR * 2, ringR * 2);
            g2.setStroke(new BasicStroke(1.0f));
            g2.setClip(oldClip);
        }

        // ── Connecting rod ────────────────────────────────────────────────────
        g2.setColor(ROD_COL);
        g2.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, pistonBotY, crankPinX, crankPinY);
        g2.setStroke(new BasicStroke(1.0f));

        // ── Crank counterweight and throw ─────────────────────────────────────
        int cwX = 2 * cx - crankPinX;
        int cwY = 2 * CRANK_CY - crankPinY;
        g2.setColor(CW_COL);
        g2.setStroke(new BasicStroke(12f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, CRANK_CY, cwX, cwY);
        g2.setStroke(new BasicStroke(1.0f));

        g2.setColor(THROW_COL);
        g2.setStroke(new BasicStroke(7.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(cx, CRANK_CY, crankPinX, crankPinY);
        g2.setStroke(new BasicStroke(1.0f));

        // Journal at this cylinder position
        g2.setColor(SHAFT_COL);
        g2.fillOval(cx - JOURNAL_R, CRANK_CY - JOURNAL_R, 2 * JOURNAL_R, 2 * JOURNAL_R);

        // Crank pin
        g2.setColor(PIN_COL);
        g2.fillOval(crankPinX - PIN_R, crankPinY - PIN_R, 2 * PIN_R, 2 * PIN_R);

        // ── Piston ────────────────────────────────────────────────────────────
        GradientPaint pistonGrad = new GradientPaint(
                boreLeft, 0, PISTON_HI, boreRight, 0, PISTON_MID);
        g2.setPaint(pistonGrad);
        g2.fillRect(boreLeft + 2, pistonTopY, BORE_PX - 4, PISTON_H);

        // Piston rings
        g2.setColor(new Color(55, 62, 70));
        for (int ring = 0; ring < 3; ring++) {
            int ry = pistonTopY + 4 + ring * 7;
            g2.drawLine(boreLeft + 2, ry, boreRight - 2, ry);
        }

        // Piston edge highlight
        g2.setColor(new Color(210, 218, 228, 120));
        g2.drawLine(boreLeft + 2, pistonTopY, boreRight - 2, pistonTopY);

        // ── Stroke label ──────────────────────────────────────────────────────
        CyclePhase ph = cyl.currentPhase();
        Color labelCol = switch (ph) {
            case INTAKE      -> C_INTAKE;
            case COMPRESSION -> C_COMPRESS;
            case POWER       -> C_POWER;
            case EXHAUST     -> C_EXHAUST;
        };
        String labelTxt = ph.name();
        g2.setColor(labelCol);
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(labelTxt, cx - fm.stringWidth(labelTxt) / 2, CYL_H - 10);

        // Cylinder number
        g2.setColor(new Color(120, 130, 145));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        String numTxt = "CYL " + (cyl.index + 1);
        g2.drawString(numTxt, cx - g2.getFontMetrics().stringWidth(numTxt) / 2, CYL_H - 22);
    }

    // ── P-V Diagram ───────────────────────────────────────────────────────────

    private void drawPVDiagram(Graphics2D g2) {
        EngineConfig cfg = state.config;

        // Panel background
        g2.setColor(new Color(14, 16, 24));
        g2.fillRect(0, PV_TOP, CANVAS_W, CANVAS_H - PV_TOP);
        g2.setColor(new Color(35, 42, 55));
        g2.drawLine(0, PV_TOP, CANVAS_W, PV_TOP);

        // Plot area insets
        int px = 72, py = PV_TOP + 22;
        int pw = CANVAS_W - px - 18;
        int ph = CANVAS_H - py - 38;

        double Vc   = cfg.clearanceVolumeM3();
        double Vmax = cfg.maxVolumeM3();
        double Pmax = EnginePhysics.peakPressureRef(cfg) * 1.08;

        // Coordinate helpers (lambdas in local scope via method helpers)
        // vToX: V → screen x;  pToY: P → screen y
        double vRange = Vmax - Vc;

        // Grid
        g2.setColor(new Color(32, 38, 52));
        for (int gx = 0; gx <= 5; gx++) {
            int sx = px + gx * pw / 5;
            g2.drawLine(sx, py, sx, py + ph);
        }
        for (int gy = 0; gy <= 4; gy++) {
            int sy = py + gy * ph / 4;
            g2.drawLine(px, sy, px + pw, sy);
        }

        // Axis labels
        g2.setColor(new Color(140, 150, 170));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2.drawString("V (m³)", px + pw / 2 - 15, py + ph + 28);
        // Rotated P axis label
        Graphics2D g2r = (Graphics2D) g2.create();
        g2r.setColor(new Color(140, 150, 170));
        g2r.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2r.translate(14, py + ph / 2 + 20);
        g2r.rotate(-Math.PI / 2);
        g2r.drawString("P (MPa)", 0, 0);
        g2r.dispose();

        // Tick values
        g2.setColor(new Color(110, 120, 140));
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 9));
        for (int i = 0; i <= 4; i++) {
            double pVal = Pmax * (4 - i) / 4.0;
            int sy = py + i * ph / 4;
            g2.drawString(String.format("%.1f", pVal / 1e6), 2, sy + 4);
        }

        // ── Otto cycle curves ─────────────────────────────────────────────────
        double rc     = cfg.compressionRatio();
        double P_comp = EnginePhysics.P_ATM * Math.pow(rc, EnginePhysics.GAMMA);
        double P_peak = EnginePhysics.peakPressureRef(cfg);
        double P_4    = EnginePhysics.P_ATM * (P_peak / P_comp);

        int STEPS = 120;

        // 1 → 2: Compression (adiabatic, V_max → V_c), blue
        g2.setColor(C_INTAKE.darker());
        g2.setStroke(new BasicStroke(1.8f));
        int[] compX = new int[STEPS + 1], compY = new int[STEPS + 1];
        for (int i = 0; i <= STEPS; i++) {
            double V = Vmax - (double) i / STEPS * (Vmax - Vc);
            double P = EnginePhysics.P_ATM * Math.pow(Vmax / V, EnginePhysics.GAMMA);
            compX[i] = vToX(V, Vc, vRange, px, pw);
            compY[i] = pToY(P, Pmax, py, ph);
        }
        g2.drawPolyline(compX, compY, STEPS + 1);

        // 2 → 3: Isochoric heat addition (vertical line at V_c), red
        g2.setColor(C_POWER);
        int vcX = vToX(Vc, Vc, vRange, px, pw);
        g2.drawLine(vcX, pToY(P_comp, Pmax, py, ph), vcX, pToY(P_peak, Pmax, py, ph));

        // 3 → 4: Expansion (adiabatic, V_c → V_max), orange
        g2.setColor(C_COMPRESS.darker());
        int[] expX = new int[STEPS + 1], expY = new int[STEPS + 1];
        for (int i = 0; i <= STEPS; i++) {
            double V = Vc + (double) i / STEPS * (Vmax - Vc);
            double P = P_peak * Math.pow(Vc / V, EnginePhysics.GAMMA);
            expX[i] = vToX(V, Vc, vRange, px, pw);
            expY[i] = pToY(P, Pmax, py, ph);
        }
        g2.drawPolyline(expX, expY, STEPS + 1);

        // 4 → 1: Isochoric heat rejection (vertical line at V_max), grey
        g2.setColor(C_EXHAUST.darker());
        int vmaxX = vToX(Vmax, Vc, vRange, px, pw);
        g2.drawLine(vmaxX, pToY(P_4, Pmax, py, ph), vmaxX, pToY(EnginePhysics.P_ATM, Pmax, py, ph));

        // P_atm baseline
        g2.setColor(new Color(60, 70, 90));
        g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
                0, new float[]{4, 4}, 0));
        int patmY = pToY(EnginePhysics.P_ATM, Pmax, py, ph);
        g2.drawLine(px, patmY, px + pw, patmY);
        g2.setStroke(new BasicStroke(1.0f));

        // ── Live dot for cylinder 0 ───────────────────────────────────────────
        if (!state.cylinders.isEmpty()) {
            CylinderState c0 = state.cylinders.get(0);
            int dotX = vToX(c0.volumeM3,  Vc, vRange, px, pw);
            int dotY = pToY(c0.pressurePa, Pmax, py, ph);

            CyclePhase dotPhase = c0.currentPhase();
            Color dotCol = switch (dotPhase) {
                case INTAKE      -> C_INTAKE;
                case COMPRESSION -> C_COMPRESS;
                case POWER       -> C_POWER;
                case EXHAUST     -> C_EXHAUST;
            };

            // Glow halo
            g2.setColor(new Color(dotCol.getRed(), dotCol.getGreen(), dotCol.getBlue(), 60));
            g2.fillOval(dotX - 7, dotY - 7, 14, 14);
            // Dot
            g2.setColor(dotCol);
            g2.fillOval(dotX - 4, dotY - 4, 8, 8);
        }

        // ── Diagram title ─────────────────────────────────────────────────────
        g2.setColor(new Color(160, 170, 195));
        g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        g2.drawString("P–V Diagram  (Cyl 1)", px + 4, py - 5);

        // Efficiency annotation
        g2.setColor(new Color(120, 200, 120));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2.drawString(String.format("η_th = %.1f%%", state.thermalEfficiency * 100),
                px + pw - 80, py - 5);
    }

    /** Maps a volume value to a screen x coordinate within the plot area. */
    private static int vToX(double V, double Vc, double vRange, int px, int pw) {
        return px + (int)((V - Vc) / vRange * pw);
    }

    /** Maps a pressure value to a screen y coordinate within the plot area. */
    private static int pToY(double P, double Pmax, int py, int ph) {
        double frac = Math.max(0.0, Math.min(1.0, P / Pmax));
        return py + ph - (int)(frac * ph);
    }

    // ── HUD ───────────────────────────────────────────────────────────────────

    private void drawHUD(Graphics2D g2) {
        EngineConfig cfg = state.config;

        g2.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));

        // RPM (large)
        double rpm = state.rpm();
        String rpmStr = String.format("%.0f RPM", rpm);
        g2.setColor(rpm > 1 ? new Color(80, 200, 255) : new Color(80, 90, 110));
        g2.drawString(rpmStr, 10, 22);

        // Power and torque
        g2.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        g2.setColor(new Color(160, 175, 200));
        g2.drawString(String.format("Power:  %.1f kW", state.powerW / 1000.0), 10, 38);
        g2.drawString(String.format("Torque: %.0f N·m", state.combustionTorqueNm), 10, 52);

        // Displacement info
        g2.setColor(new Color(110, 120, 140));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        g2.drawString(String.format("%s   %.0f cc   r_c=%.1f",
                cfg.numCylinders() + "-cyl",
                cfg.totalDisplacementCC(),
                cfg.compressionRatio()), 10, 66);

        // Paused indicator
        if (paused) {
            g2.setColor(new Color(255, 200, 60, 200));
            g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
            String p = "⏸  PAUSED";
            g2.drawString(p, CANVAS_W / 2 - 40, 22);
        }

        // Key hints (bottom-left of cylinder section)
        g2.setColor(new Color(70, 80, 100));
        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 9));
        g2.drawString("Space=Start  P=Pause  R=Reset", 8, CYL_H - 2);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /** Gives the crankshaft a starter-motor kick to get the engine running. */
    public void startEngine() {
        if (state.omegaRadS < EnginePhysics.STALL_OMEGA * 2) {
            state.omegaRadS = EnginePhysics.STALL_OMEGA * 2;
        }
    }

    /** Resets the engine to the given preset (or the current config if null). */
    public void reset(EnginePreset preset) {
        if (preset != null) {
            state = new EngineState(preset.config());
        } else {
            state.reset();
        }
        state.throttle = 0.0;
    }

    public void setThrottle(double t) {
        state.throttle = Math.max(0.0, Math.min(1.0, t));
    }

    public void setSimSpeed(double s) {
        state.simSpeed = Math.max(0.05, Math.min(2.0, s));
    }

    public double getSimSpeed() { return state.simSpeed; }

    public void togglePause()    { paused = !paused; }
    public boolean isPaused()    { return paused; }
    public EngineState getState(){ return state; }
    public void setOnTick(Runnable r) { onTick = r; }
}
