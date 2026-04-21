package com.wizardrogue.ui;

import com.wizardrogue.core.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * The main game panel: renders the dungeon and is the single entry point for
 * all keyboard input.
 *
 * <h2>Three distinct keyboard-input techniques demonstrated here</h2>
 *
 * <h3>1. Key-state tracking (movement)</h3>
 * <p>A {@code Set<Integer>} called {@link #heldKeys} records every currently-
 * pressed key.  The 100 ms game-loop timer checks this set to produce smooth,
 * real-time movement that is <em>independent of the OS key-repeat rate</em>.
 * Diagonal movement is not supported by design (only one direction per tick),
 * but two keys held simultaneously would both be in the set, demonstrating the
 * technique even when only one is acted on.
 *
 * <h3>2. Sequence detection with a timing window (spell casting)</h3>
 * <p>Q, E, and R presses are forwarded to {@link com.wizardrogue.core.InputBuffer}
 * via {@link GameController#handleSpellKey(int)} in {@code keyPressed}.  The
 * buffer stores each press with a millisecond timestamp; a spell fires when the
 * most recent N presses match a spell's pattern <em>and</em> all fall within the
 * 2.5-second combo window — exactly like a fighting-game move system.  No
 * separate "spell mode" is needed; the sequence just expires naturally.
 *
 * <h3>3. Immediate single-key actions</h3>
 * <p>Space (pickup), comma (descend stairs), Escape (pause), and R-new-game are
 * handled directly in {@code keyPressed} without state tracking or buffering.
 * This shows the contrast between the three techniques clearly.
 *
 * <h2>Rendering</h2>
 * <p>The dungeon is an ASCII grid drawn with a monospaced font.  Each cell is
 * {@value #CELL_W} × {@value #CELL_H} pixels.  Tiles are rendered in three
 * brightness levels: unexplored (hidden), explored-but-dark (50% brightness),
 * and fully visible (full colour).
 */
public final class DungeonPanel extends JPanel {

    // ------------------------------------------------------------------ dimensions

    static final int CELL_W  = 10;
    static final int CELL_H  = 17;
    static final int MAP_PX_W = DungeonMap.WIDTH  * CELL_W;
    static final int MAP_PX_H = DungeonMap.HEIGHT * CELL_H;
    static final int LOG_H    = 88;

    // ------------------------------------------------------------------ colours

    private static final Color BG          = new Color( 8,  7, 12);
    private static final Color LOG_BG      = new Color(12, 10, 20);
    private static final Color LOG_BORDER  = new Color(50, 45, 75);
    private static final Color LOG_TEXT    = new Color(170, 185, 200);
    private static final Color LOG_RECENT  = new Color(230, 240, 255);
    private static final Color OVERLAY_BG  = new Color(0, 0, 0, 180);
    private static final Color CURSOR_CLR  = new Color(255, 255, 100, 120);

    // ------------------------------------------------------------------ fonts

    private static final Font DUNGEON_FONT = new Font("Monospaced", Font.PLAIN, 13);
    private static final Font LOG_FONT     = new Font("SansSerif",  Font.PLAIN, 11);
    private static final Font OVERLAY_FONT = new Font("SansSerif",  Font.BOLD,  48);
    private static final Font OVERLAY_SUB  = new Font("SansSerif",  Font.PLAIN, 16);

    // ------------------------------------------------------------------ state

    private final GameController controller;
    private final StatsPanel      statsPanel;

    /**
     * Keys currently held down — drives continuous movement in the game loop.
     * Updated in {@code keyPressed} / {@code keyReleased}.
     */
    private final Set<Integer> heldKeys = new HashSet<>();

    /** Ticks between movement steps while a direction key is held. */
    private static final int MOVE_REPEAT_TICKS = 1;
    private int moveTick;

    private final Timer gameTimer;

    // ------------------------------------------------------------------ constructor

    public DungeonPanel(GameController controller, StatsPanel statsPanel) {
        this.controller = controller;
        this.statsPanel = statsPanel;
        setPreferredSize(new Dimension(MAP_PX_W, MAP_PX_H + LOG_H));
        setBackground(BG);
        setFocusable(true);
        attachKeyListener();

        // 100 ms tick — enemies move, MP regens, UI refreshes
        gameTimer = new Timer(100, e -> gameTick());
        gameTimer.start();
    }

    // ------------------------------------------------------------------ game loop

    private void gameTick() {
        controller.tick();

        // Held-key movement — fires every MOVE_REPEAT_TICKS ticks
        if (controller.getState() == GameController.State.PLAYING) {
            moveTick++;
            if (moveTick >= MOVE_REPEAT_TICKS) {
                moveTick = 0;
                for (Direction dir : Direction.values()) {
                    if (heldKeys.contains(dir.vk)) {
                        controller.playerMove(dir);
                        break; // one direction per tick
                    }
                }
            }
        }

        statsPanel.repaint();
        repaint();
    }

    // ------------------------------------------------------------------ keyboard input

    /**
     * Attaches the {@code KeyListener} that implements all three input techniques.
     *
     * <p>Only one listener is needed because:
     * <ul>
     *   <li>{@code keyPressed} is fired by the OS for every new key-down event
     *       (and repeated events when held, though we don't rely on that).</li>
     *   <li>{@code keyReleased} is fired exactly once when the key is let go.</li>
     * </ul>
     */
    private void attachKeyListener() {
        addKeyListener(new KeyAdapter() {

            // ── keyPressed ──────────────────────────────────────────────────
            @Override
            public void keyPressed(KeyEvent e) {
                int vk = e.getKeyCode();

                // Technique 1: Key-state tracking — update the held set.
                // Set.add returns false when the key was already present, which
                // means this is an OS key-repeat event, not a genuine new press.
                boolean isNewPress = heldKeys.add(vk);

                // Technique 3: Immediate single-key actions
                switch (vk) {
                    case KeyEvent.VK_ESCAPE -> controller.togglePause();
                    case KeyEvent.VK_SPACE  -> controller.tryPickup();
                    case KeyEvent.VK_COMMA  -> controller.tryDescend();
                    case KeyEvent.VK_N -> {
                        if (controller.getState() == GameController.State.DEAD
                                || controller.getState() == GameController.State.VICTORY) {
                            controller.newGame();
                            heldKeys.clear();
                        }
                    }
                }

                // Technique 2: Sequence detection — spell keys go into the buffer
                if (InputBuffer.isSpellKey(vk)) {
                    controller.handleSpellKey(vk);
                }

                // Arrow keys: rotate facing direction without moving
                Direction arrowDir = Direction.fromArrowKey(vk);
                if (arrowDir != null) {
                    controller.playerFace(arrowDir);
                    repaint();
                }

                // Technique 1 (also): Immediate first step for direction keys so
                // movement feels responsive before the timer fires.  Guard on
                // isNewPress so OS key-repeat events don't fire extra moves and
                // push the player past item tiles before they can pick them up.
                Direction dir = Direction.fromKey(vk);
                if (dir != null && isNewPress
                        && controller.getState() == GameController.State.PLAYING) {
                    controller.playerMove(dir);
                    moveTick = 0; // reset repeat counter so held movement starts fresh
                    repaint();    // immediate redraw so movement isn't delayed by the timer
                }
            }

            // ── keyReleased ─────────────────────────────────────────────────
            @Override
            public void keyReleased(KeyEvent e) {
                // Technique 1: Remove from held set — movement stops
                heldKeys.remove(e.getKeyCode());
            }
        });
    }

    // ------------------------------------------------------------------ rendering

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        DungeonMap map    = controller.getMap();
        Player     player = controller.getPlayer();
        if (map == null || player == null) return;

        drawMap(g, map, player);
        drawItems(g, map);
        drawEnemies(g, map);
        drawSpellEffects(g);
        drawPlayer(g, player);
        drawMessageLog(g);

        GameController.State state = controller.getState();
        if (state == GameController.State.DEAD)    drawOverlay(g, "YOU DIED",
            "The dungeon claims another soul.", "Press N for new game");
        else if (state == GameController.State.VICTORY) drawOverlay(g, "VICTORY!",
            "You have mastered the dungeon!", "Press N to play again");
        else if (state == GameController.State.PAUSED)  drawOverlay(g, "PAUSED",
            "The monsters hold their breath.", "Press Esc to resume");
    }

    // ------------------------------------------------------------------ map rendering

    private void drawMap(Graphics2D g, DungeonMap map, Player player) {
        g.setFont(DUNGEON_FONT);
        FontMetrics fm = g.getFontMetrics();
        int charW = fm.charWidth('#');

        for (int row = 0; row < DungeonMap.HEIGHT; row++) {
            for (int col = 0; col < DungeonMap.WIDTH; col++) {
                boolean vis  = map.isVisible(col, row);
                boolean expl = map.isExplored(col, row);
                if (!vis && !expl) continue;

                Tile tile  = map.getTile(col, row);
                Color base = tile.color;
                Color fg   = vis ? base : dim(base, 0.35f);

                int px = col * CELL_W + (CELL_W - charW) / 2;
                int py = row * CELL_H + CELL_H - 3;

                g.setColor(fg);
                g.drawString(String.valueOf(tile.glyph), px, py);
            }
        }
    }

    private void drawItems(Graphics2D g, DungeonMap map) {
        g.setFont(DUNGEON_FONT);
        FontMetrics fm = g.getFontMetrics();
        int charW = fm.charWidth('@');

        for (Item item : controller.getItems()) {
            if (item.isPickedUp()) continue;
            int col = item.getX(), row = item.getY();
            if (!map.isVisible(col, row)) continue;

            int px = col * CELL_W + (CELL_W - charW) / 2;
            int py = row * CELL_H + CELL_H - 3;
            g.setColor(item.getType().color);
            g.drawString(String.valueOf(item.getGlyph()), px, py);
        }
    }

    private void drawEnemies(Graphics2D g, DungeonMap map) {
        g.setFont(DUNGEON_FONT);
        FontMetrics fm = g.getFontMetrics();
        int charW = fm.charWidth('O');

        for (Enemy enemy : controller.getEnemies()) {
            if (!enemy.isAlive()) continue;
            int col = enemy.getX(), row = enemy.getY();
            if (!map.isVisible(col, row)) continue;

            int px = col * CELL_W + (CELL_W - charW) / 2;
            int py = row * CELL_H + CELL_H - 3;

            // Frozen enemies drawn in icy blue
            Color c = enemy.isFrozen() ? new Color(150, 200, 255) : enemy.getColor();
            g.setColor(c);
            g.drawString(String.valueOf(enemy.getGlyph()), px, py);

            // HP bar for damaged enemies
            if (enemy.getHp() < enemy.getMaxHp()) {
                int bx = col * CELL_W, by = row * CELL_H;
                g.setColor(new Color(180, 40, 40));
                g.fillRect(bx, by, CELL_W, 2);
                g.setColor(new Color(60, 200, 60));
                int fw = (int)((double) enemy.getHp() / enemy.getMaxHp() * CELL_W);
                g.fillRect(bx, by, fw, 2);
            }
        }
    }

    private void drawPlayer(Graphics2D g, Player player) {
        int col = player.getX(), row = player.getY();
        g.setFont(DUNGEON_FONT);
        FontMetrics fm = g.getFontMetrics();
        int charW = fm.charWidth('@');
        int px = col * CELL_W + (CELL_W - charW) / 2;
        int py = row * CELL_H + CELL_H - 3;

        // Facing-direction cursor tick
        int fx = col + player.getFacing().dx;
        int fy = row + player.getFacing().dy;
        g.setColor(CURSOR_CLR);
        g.fillRect(fx * CELL_W, fy * CELL_H, CELL_W, CELL_H);

        // Player glyph
        g.setColor(player.getColor());
        g.setFont(new Font("Monospaced", Font.BOLD, 13));
        g.drawString("@", px, py);
    }

    // ------------------------------------------------------------------ message log

    private void drawMessageLog(Graphics2D g) {
        int logY = MAP_PX_H;
        g.setColor(LOG_BG);
        g.fillRect(0, logY, MAP_PX_W, LOG_H);
        g.setColor(LOG_BORDER);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(0, logY, MAP_PX_W, logY);

        List<String> msgs = controller.getLog().getRecent();
        g.setFont(LOG_FONT);
        int ty = logY + 14;
        for (int i = 0; i < msgs.size(); i++) {
            g.setColor(i == 0 ? LOG_RECENT : LOG_TEXT);
            g.drawString(msgs.get(i), 8, ty);
            ty += 15;
        }
    }

    // ------------------------------------------------------------------ overlays

    private void drawOverlay(Graphics2D g, String title, String sub, String action) {
        g.setColor(OVERLAY_BG);
        g.fillRect(0, 0, MAP_PX_W, MAP_PX_H);

        g.setFont(OVERLAY_FONT);
        FontMetrics fm = g.getFontMetrics();
        int tx = (MAP_PX_W - fm.stringWidth(title)) / 2;
        int ty = MAP_PX_H / 2 - 20;
        g.setColor(Color.BLACK);
        g.drawString(title, tx + 3, ty + 3);
        g.setColor(title.equals("YOU DIED") ? new Color(230, 60, 60) :
                   title.equals("VICTORY!") ? new Color(255, 210, 50) :
                                              new Color(160, 170, 255));
        g.drawString(title, tx, ty);

        g.setFont(OVERLAY_SUB);
        fm = g.getFontMetrics();
        g.setColor(new Color(200, 210, 225));
        g.drawString(sub, (MAP_PX_W - fm.stringWidth(sub)) / 2, ty + 40);
        g.setColor(new Color(120, 210, 130));
        g.drawString(action, (MAP_PX_W - fm.stringWidth(action)) / 2, ty + 65);
    }

    // ------------------------------------------------------------------ spell effects

    private void drawSpellEffects(Graphics2D g) {
        List<SpellEffect> effects = controller.getActiveEffects();
        if (effects.isEmpty()) return;

        g.setFont(DUNGEON_FONT);
        FontMetrics fm = g.getFontMetrics();
        int charW = fm.charWidth('*');

        for (SpellEffect fx : effects) {
            switch (fx.getKind()) {
                case PROJECTILE -> drawProjectileFx(g, fx, charW);
                case LINE       -> drawLineFx(g, fx);
                case AREA       -> drawAreaFx(g, fx);
                case SELF       -> drawSelfFx(g, fx);
            }
        }
        // Always restore full opacity after effects
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    }

    private void drawProjectileFx(Graphics2D g, SpellEffect fx, int charW) {
        int[][] tiles = fx.getTiles();
        if (tiles == null || tiles.length == 0) return;

        int idx = fx.projectileTileIndex();

        if (idx >= 0) {
            // Draw faint trail (up to 2 tiles behind the head)
            for (int i = Math.max(0, idx - 2); i < idx; i++) {
                float trailAlpha = 0.15f + 0.20f * ((float)(i - idx + 3) / 3f);
                setAlpha(g, Math.max(0.05f, trailAlpha));
                g.setColor(fx.getColor());
                drawGlyph(g, tiles[i][0], tiles[i][1], '*', charW);
            }
            // Bright head
            setAlpha(g, 1f);
            g.setColor(fx.getColor().brighter());
            drawGlyph(g, tiles[idx][0], tiles[idx][1], '*', charW);

        } else if (fx.isImpactPhase()) {
            // Impact flash: fill the last tile with a fading colour block
            int[] last = tiles[tiles.length - 1];
            float impactAlpha = 1f - fx.progress();
            setAlpha(g, impactAlpha);
            g.setColor(fx.getColor().brighter());
            g.fillRect(last[0] * CELL_W, last[1] * CELL_H, CELL_W, CELL_H);
        }
    }

    private void drawLineFx(Graphics2D g, SpellEffect fx) {
        int[][] tiles = fx.getTiles();
        if (tiles == null) return;
        setAlpha(g, (1f - fx.progress()) * 0.80f);
        g.setColor(fx.getColor());
        for (int[] cell : tiles)
            g.fillRect(cell[0] * CELL_W, cell[1] * CELL_H, CELL_W, CELL_H);
    }

    private void drawAreaFx(Graphics2D g, SpellEffect fx) {
        int[][] tiles = fx.getTiles();
        if (tiles == null) return;
        setAlpha(g, (1f - fx.progress()) * 0.65f);
        g.setColor(fx.getColor());
        for (int[] cell : tiles)
            g.fillRect(cell[0] * CELL_W, cell[1] * CELL_H, CELL_W, CELL_H);
    }

    private void drawSelfFx(Graphics2D g, SpellEffect fx) {
        int cx = fx.getSelfX(), cy = fx.getSelfY();
        int radius = (int)(fx.progress() * 4);   // expands 0→4 tiles
        float alpha = (1f - fx.progress()) * 0.75f;
        setAlpha(g, alpha);
        g.setColor(fx.getColor());
        // Manhattan-distance ring at current radius (cross arms)
        for (int r = 0; r <= radius; r++) {
            for (int[] off : new int[][]{{r,0},{-r,0},{0,r},{0,-r}})
                g.fillRect((cx + off[0]) * CELL_W, (cy + off[1]) * CELL_H, CELL_W, CELL_H);
        }
    }

    private void setAlpha(Graphics2D g, float alpha) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
            Math.max(0f, Math.min(1f, alpha))));
    }

    private void drawGlyph(Graphics2D g, int col, int row, char ch, int charW) {
        int px = col * CELL_W + (CELL_W - charW) / 2;
        int py = row * CELL_H + CELL_H - 3;
        g.drawString(String.valueOf(ch), px, py);
    }

    // ------------------------------------------------------------------ colour helper

    /** Returns a darkened version of {@code c} at the given brightness fraction. */
    private static Color dim(Color c, float factor) {
        return new Color(
            (int)(c.getRed()   * factor),
            (int)(c.getGreen() * factor),
            (int)(c.getBlue()  * factor));
    }
}
