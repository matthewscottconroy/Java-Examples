package com.bugblaster.ui;

import com.bugblaster.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * The main game rendering panel and the hub for all mouse input.
 *
 * <p>This panel owns the {@code javax.swing.Timer} that drives the 60 fps game
 * loop, translates raw mouse events into high-level game actions, and renders
 * the entire scene each frame.
 *
 * <h2>Mouse input handled</h2>
 * <pre>
 *   Left click (quick)      → spray()          – small burst
 *   Left double-click       → stomp()           – boot slam
 *   Hold left ≥ 400 ms      → chargeBlast()     – big area blast on release
 *   Left drag               → dragSpray()        – continuous spray trail
 *   Right click             → placeTrap()        – sticky glue trap
 *   Right drag on trap      → move trap          – reposition existing trap
 *   Mouse move              → cursor tracking    – flies dodge; crosshair drawn
 * </pre>
 *
 * <h2>Charge mechanic</h2>
 * When the player presses and holds the left button <em>without dragging</em>,
 * a charge meter grows (shown in the HUD). Releasing after {@value #HOLD_THRESHOLD_MS} ms
 * fires a scaled blast instead of a normal spray. Releasing early (or dragging
 * first) treats the action as a regular click/drag.
 */
public final class GamePanel extends JPanel {

    // ------------------------------------------------------------------ constants

    static final int GAME_W = 900;
    static final int GAME_H = 620;

    /** Minimum hold duration (ms) to trigger a charged blast on release. */
    private static final long HOLD_THRESHOLD_MS = 400;

    /** Maximum hold duration used for charge-meter scaling. */
    private static final long MAX_HOLD_MS = 2_000;

    private static final int TARGET_FPS = 60;

    // ------------------------------------------------------------------ colours

    private static final Color BG_DARK    = new Color(28, 22, 14);
    private static final Color FLOOR_LINE = new Color(40, 32, 20);
    private static final Color TARGET_BG  = new Color(50, 35, 18);
    private static final Color TARGET_RIM = new Color(210, 170, 80);

    // ------------------------------------------------------------------ model

    private final GameController controller;
    private final HUDPanel        hud;

    // ------------------------------------------------------------------ mouse state

    /** Whether the left button is currently held down. */
    private boolean leftHeld;
    /** Whether the mouse moved enough during left-held to count as a drag. */
    private boolean wasDragged;
    /** Whether a charged blast was already fired on this press (suppress mouseClicked). */
    private boolean chargeConsumed;
    /** Timestamp of the last mousePressed(left). */
    private long pressStart;
    /** Position of the last mouseMoved/mouseDragged event. */
    private Point cursorPos = new Point(GAME_W / 2, GAME_H / 2);
    /** Last drag position used to compute drag segments. */
    private Point lastDragPos;
    /** Trap currently being repositioned by right-drag (null if none). */
    private Trap grabbedTrap;

    // ------------------------------------------------------------------ game loop

    private final Timer gameTimer;

    public GamePanel(GameController controller, HUDPanel hud) {
        this.controller = controller;
        this.hud        = hud;

        setPreferredSize(new Dimension(GAME_W, GAME_H));
        setBackground(BG_DARK);
        setFocusable(true);

        attachMouseListeners();

        gameTimer = new Timer(1000 / TARGET_FPS, e -> tick());
        gameTimer.start();
    }

    // ------------------------------------------------------------------ game loop

    private void tick() {
        controller.update(cursorPos.x, cursorPos.y);

        // Push charge progress to HUD
        if (leftHeld && !wasDragged) {
            long elapsed = System.currentTimeMillis() - pressStart;
            float progress = Math.min(1f, (float) elapsed / MAX_HOLD_MS);
            hud.setChargeProgress(progress);
        } else {
            hud.setChargeProgress(0f);
        }

        hud.repaint();
        repaint();
    }

    // ------------------------------------------------------------------ painting

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        drawBackground(g);
        drawTarget(g);

        // Traps beneath bugs
        controller.getTraps().forEach(t -> t.draw(g));

        // Effects beneath bugs
        controller.getEffects().forEach(e -> e.draw(g));

        // Bugs
        controller.getBugs().forEach(b -> b.draw(g));

        // Floating text on top
        controller.getFloatingTexts().forEach(ft -> ft.draw(g));

        drawCrosshair(g);
        drawChargeIndicator(g);
        drawWaveAnnouncement(g);

        if (controller.getState().isGameOver()) drawGameOver(g);
        else if (controller.getState().isPaused()) drawPaused(g);
    }

    // ------------------------------------------------------------------ scene elements

    private void drawBackground(Graphics2D g) {
        g.setColor(BG_DARK);
        g.fillRect(0, 0, GAME_W, GAME_H);

        // Wooden-floor plank lines
        g.setColor(FLOOR_LINE);
        g.setStroke(new BasicStroke(1f));
        for (int y = 40; y < GAME_H; y += 40) g.drawLine(0, y, GAME_W, y);
        for (int x = 90; x < GAME_W; x += 90) {
            for (int seg = 0; seg < GAME_H; seg += 80) {
                g.drawLine(x, seg, x, seg + 40);
            }
        }
    }

    private void drawTarget(Graphics2D g) {
        int cx = (int) Bug.TARGET_X, cy = (int) Bug.TARGET_Y;

        // Glow danger zone when bugs are very close
        boolean bugNear = controller.getBugs().stream()
            .anyMatch(b -> Math.hypot(b.getX() - cx, b.getY() - cy) < 80);
        if (bugNear) {
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f));
            g.setColor(Color.RED);
            g.fillOval(cx - 80, cy - 80, 160, 160);
            g.setComposite(old);
        }

        // Pie tin
        g.setColor(TARGET_BG);
        g.fillOval(cx - 38, cy - 20, 76, 40);
        g.setColor(new Color(200, 160, 60));
        g.fillOval(cx - 35, cy - 17, 70, 34);
        // Crust edge
        g.setColor(TARGET_RIM);
        g.setStroke(new BasicStroke(3f));
        g.drawOval(cx - 35, cy - 17, 70, 34);
        // Lattice lines
        g.setColor(new Color(180, 140, 50));
        g.setStroke(new BasicStroke(1.5f));
        g.drawLine(cx - 25, cy, cx + 25, cy);
        g.drawLine(cx, cy - 12, cx, cy + 12);
        // Label
        g.setFont(new Font("SansSerif", Font.BOLD, 9));
        g.setColor(new Color(255, 230, 160));
        g.drawString("MOM'S PIE", cx - 24, cy + 32);
    }

    private void drawCrosshair(Graphics2D g) {
        int cx = cursorPos.x, cy = cursorPos.y;
        g.setColor(new Color(200, 255, 200, 180));
        g.setStroke(new BasicStroke(1.5f));
        int r = 14;
        g.drawOval(cx - r, cy - r, r * 2, r * 2);
        g.drawLine(cx - r - 6, cy,     cx - r + 3, cy);
        g.drawLine(cx + r - 3, cy,     cx + r + 6, cy);
        g.drawLine(cx,         cy - r - 6, cx, cy - r + 3);
        g.drawLine(cx,         cy + r - 3, cx, cy + r + 6);
    }

    private void drawChargeIndicator(Graphics2D g) {
        if (!leftHeld || wasDragged) return;
        long elapsed = System.currentTimeMillis() - pressStart;
        if (elapsed < HOLD_THRESHOLD_MS / 2) return;

        float progress = Math.min(1f, (float) elapsed / MAX_HOLD_MS);
        int cx = cursorPos.x, cy = cursorPos.y;

        // Predicted blast radius circle
        int radius = (int)(22 + progress * (105 - 22));
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        g.setColor(progress < 0.6f ? new Color(60, 180, 255) : new Color(255, 200, 50));
        g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setComposite(old);

        g.setColor(progress < 0.6f ? new Color(100, 210, 255) : new Color(255, 230, 80));
        g.setStroke(new BasicStroke(2f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND,
            0, new float[]{6, 4}, 0));
        g.drawOval(cx - radius, cy - radius, radius * 2, radius * 2);
        g.setStroke(new BasicStroke(1.5f));
    }

    private void drawWaveAnnouncement(Graphics2D g) {
        int ticks = controller.getWaveAnnounceTick();
        if (ticks <= 0) return;

        float alpha = ticks > 120
            ? Math.min(1f, (180 - ticks) / 30f)
            : ticks / 60f;

        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setFont(new Font("SansSerif", Font.BOLD, 28));
        String text = controller.getWaveAnnounceText();
        FontMetrics fm = g.getFontMetrics();
        int tx = (GAME_W - fm.stringWidth(text)) / 2;
        int ty = GAME_H / 3;

        // Shadow
        g.setColor(Color.BLACK);
        g.drawString(text, tx + 2, ty + 2);
        // Text
        g.setColor(new Color(255, 220, 80));
        g.drawString(text, tx, ty);
        g.setComposite(old);
    }

    private void drawGameOver(Graphics2D g) {
        drawOverlay(g, "GAME OVER",
            "The bugs have taken over. They're in charge now. Literally.",
            "Press R to play again.");
    }

    private void drawPaused(Graphics2D g) {
        drawOverlay(g, "PAUSED", "The bugs are waiting. Patiently.", "Press P to resume.");
    }

    private void drawOverlay(Graphics2D g, String title, String sub1, String sub2) {
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.72f));
        g.setColor(new Color(8, 6, 14));
        g.fillRect(0, 0, GAME_W, GAME_H);
        g.setComposite(old);

        g.setFont(new Font("SansSerif", Font.BOLD, 52));
        FontMetrics fm = g.getFontMetrics();
        int tx = (GAME_W - fm.stringWidth(title)) / 2;
        g.setColor(Color.BLACK);
        g.drawString(title, tx + 3, GAME_H / 2 - 30 + 3);
        g.setColor(new Color(255, 80, 80));
        g.drawString(title, tx, GAME_H / 2 - 30);

        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        fm = g.getFontMetrics();
        g.setColor(new Color(200, 200, 200));
        g.drawString(sub1, (GAME_W - fm.stringWidth(sub1)) / 2, GAME_H / 2 + 20);
        g.setColor(new Color(160, 220, 160));
        g.drawString(sub2, (GAME_W - fm.stringWidth(sub2)) / 2, GAME_H / 2 + 50);

        // Final score
        String score = "Final score: " + controller.getState().getScore();
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        fm = g.getFontMetrics();
        g.setColor(new Color(80, 220, 130));
        g.drawString(score, (GAME_W - fm.stringWidth(score)) / 2, GAME_H / 2 + 90);
    }

    // ------------------------------------------------------------------ mouse input

    private void attachMouseListeners() {
        addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (SwingUtilities.isLeftMouseButton(e)) {
                    leftHeld      = true;
                    wasDragged    = false;
                    chargeConsumed = false;
                    pressStart    = System.currentTimeMillis();
                    lastDragPos   = e.getPoint();
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    // Grab an existing trap for repositioning
                    grabbedTrap = controller.getTrapAt(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (leftHeld && !wasDragged) {
                        long elapsed = System.currentTimeMillis() - pressStart;
                        if (elapsed >= HOLD_THRESHOLD_MS) {
                            // Held long enough → charged blast
                            controller.chargeBlast(e.getX(), e.getY(), elapsed);
                            chargeConsumed = true;
                        }
                        // Short press falls through to mouseClicked
                    }
                    leftHeld   = false;
                    wasDragged = false;
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    grabbedTrap = null;
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // Suppress if a charge blast was already fired on this press
                if (chargeConsumed) { chargeConsumed = false; return; }
                if (controller.getState().isGameOver()) return;

                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (e.getClickCount() == 2) {
                        controller.stomp(e.getX(), e.getY());
                    } else {
                        controller.spray(e.getX(), e.getY());
                    }
                } else if (SwingUtilities.isRightMouseButton(e) && grabbedTrap == null) {
                    controller.placeTrap(e.getX(), e.getY());
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                Point prev = cursorPos;
                cursorPos  = e.getPoint();

                if (SwingUtilities.isLeftMouseButton(e)) {
                    wasDragged = true;
                    if (lastDragPos != null) {
                        controller.dragSpray(lastDragPos.x, lastDragPos.y, e.getX(), e.getY());
                    }
                    lastDragPos = e.getPoint();
                } else if (SwingUtilities.isRightMouseButton(e) && grabbedTrap != null) {
                    // Reposition grabbed trap
                    grabbedTrap.setPosition(e.getX(), e.getY());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                cursorPos = e.getPoint();
            }
        });

        // Keyboard: P = pause, R = restart
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_P -> controller.getState().togglePause();
                    case KeyEvent.VK_R -> controller.newGame();
                }
            }
        });
    }
}
