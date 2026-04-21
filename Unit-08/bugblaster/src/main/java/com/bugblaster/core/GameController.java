package com.bugblaster.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Central controller that owns all game entities and processes both the
 * simulation tick and mouse-input actions.
 *
 * <p>The controller is deliberately free of any Swing imports so that the
 * model layer remains independent of the view.
 *
 * <p>Mouse actions surfaced by {@link com.bugblaster.ui.GamePanel}:
 * <ul>
 *   <li>{@link #spray(double, double)}          – single left click</li>
 *   <li>{@link #stomp(double, double)}           – left double-click</li>
 *   <li>{@link #chargeBlast(double, double, long)} – hold-and-release (≥400 ms)</li>
 *   <li>{@link #dragSpray(double, double, double, double)} – left drag segment</li>
 *   <li>{@link #placeTrap(double, double)}       – single right click</li>
 *   <li>{@link #getTrapAt(double, double)}       – returns trap for right-drag grab</li>
 * </ul>
 */
public final class GameController {

    // ------------------------------------------------------------------ constants

    /** Spray radius in pixels for a single left click. */
    private static final int SPRAY_RADIUS = 28;

    /** Maximum hold duration (ms) considered for blast radius scaling. */
    private static final long MAX_HOLD_MS = 2_000;

    /** Minimum and maximum blast radii. */
    private static final int MIN_BLAST_RADIUS = 22;
    private static final int MAX_BLAST_RADIUS = 105;

    /** How many ticks between wave-transition announcements showing on screen. */
    private static final int WAVE_ANNOUNCE_TICKS = 180;

    // ------------------------------------------------------------------ state

    private final List<Bug>          bugs          = new ArrayList<>();
    private final List<Trap>         traps         = new ArrayList<>();
    private final List<Effect>       effects       = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private final GameState          state         = new GameState();
    private final BugSpawner         spawner       = new BugSpawner();

    private int  waveAnnounceTick;
    private String waveAnnounceText = "";

    public GameController() {
        spawner.loadWave(1);
        announceWave(1);
    }

    // ------------------------------------------------------------------ simulation tick

    /**
     * Advances the entire simulation by one tick.
     * Must be called from the Swing EDT (via a {@code javax.swing.Timer}).
     *
     * @param cursorX current cursor X in game coordinates
     * @param cursorY current cursor Y in game coordinates
     */
    public void update(double cursorX, double cursorY) {
        if (state.isGameOver() || state.isPaused()) return;

        spawner.update(bugs);

        // Update each bug; collect those that reached the target
        List<Bug> arrivals = new ArrayList<>();
        for (Bug bug : bugs) {
            bug.onCursorNear(cursorX, cursorY);
            bug.update();
            if (bug.hasReached()) arrivals.add(bug);
        }

        // Process arrivals — lose a life per bug
        for (Bug arrived : arrivals) {
            state.loseLife();
            floatingTexts.add(new FloatingText(
                "THEY'RE IN THE PIE!", arrived.getX(), arrived.getY(),
                new Color(255, 80, 80), 16));
            bugs.remove(arrived);
        }

        // Check trap catches
        for (Trap trap : traps) {
            for (Bug bug : bugs) {
                if (bug.isAlive() && !bug.isCaught()) trap.tryToCatch(bug);
            }
            trap.update();
        }

        // Remove dead bugs, award score
        bugs.removeIf(bug -> {
            if (!bug.isAlive() && !bug.hasReached()) {
                int awarded = state.recordKill(bug.getScoreValue());
                int combo   = state.getCombo();
                // Last-words floating text
                floatingTexts.add(new FloatingText(
                    bug.getLastWords(), bug.getX(), bug.getY() - 10,
                    new Color(255, 220, 80)));
                // Score + combo text
                String pts = "+" + awarded + (combo > 1 ? "  ×" + combo + "!" : "");
                floatingTexts.add(new FloatingText(
                    pts, bug.getX(), bug.getY() - 28,
                    combo > 2 ? new Color(255, 160, 40) : new Color(180, 255, 160)));
                return true;
            }
            return false;
        });

        // Advance effects and floating text
        effects.forEach(Effect::update);
        effects.removeIf(Effect::isExpired);
        floatingTexts.forEach(FloatingText::update);
        floatingTexts.removeIf(FloatingText::isExpired);

        // Wave progression
        if (waveAnnounceTick > 0) waveAnnounceTick--;

        if (spawner.isWaveComplete(bugs)) {
            state.advanceWave();
            spawner.loadWave(state.getWave());
            announceWave(state.getWave());
        }
    }

    // ------------------------------------------------------------------ mouse actions

    /**
     * Quick spray at ({@code x}, {@code y}) — radius {@value #SPRAY_RADIUS} px,
     * 1 damage to each bug in range.
     * Triggered by a single left click.
     */
    public void spray(double x, double y) {
        effects.add(new SprayEffect(x, y, SPRAY_RADIUS));
        boolean hit = applyAreaDamage(x, y, SPRAY_RADIUS, 1);
        if (!hit) floatingTexts.add(new FloatingText("MISS!", x, y, new Color(160, 160, 160)));
    }

    /**
     * Boot stomp centred at ({@code x}, {@code y}) — radius {@value StompEffect#RADIUS} px,
     * 3 damage to each bug in range.
     * Triggered by a left double-click.
     */
    public void stomp(double x, double y) {
        effects.add(new StompEffect(x, y));
        applyAreaDamage(x, y, StompEffect.RADIUS, 3);
    }

    /**
     * Charged aerosol blast — radius and damage scale with how long the button
     * was held. Holding for {@value #MAX_HOLD_MS} ms gives the maximum radius
     * ({@value #MAX_BLAST_RADIUS} px) and 8 damage.
     * Triggered by holding the left button ≥400 ms and releasing.
     *
     * @param holdMs milliseconds the button was held
     */
    public void chargeBlast(double x, double y, long holdMs) {
        double t      = Math.min(1.0, (double) holdMs / MAX_HOLD_MS);
        int    radius = (int)(MIN_BLAST_RADIUS + t * (MAX_BLAST_RADIUS - MIN_BLAST_RADIUS));
        int    damage = (int)(2 + t * 6);
        effects.add(new ChargeBlastEffect(x, y, radius));
        applyAreaDamage(x, y, radius, damage);
    }

    /**
     * Spray segment from ({@code x1},{@code y1}) to ({@code x2},{@code y2}) — kills
     * any bug within 14 px of the line. Good for wiping out columns of ants.
     * Triggered continuously while left-dragging.
     */
    public void dragSpray(double x1, double y1, double x2, double y2) {
        effects.add(new DragTrailEffect(x1, y1, x2, y2));
        bugs.removeIf(bug -> {
            if (!bug.isAlive()) return false;
            if (distToSegment(bug.getX(), bug.getY(), x1, y1, x2, y2) < 14) {
                bug.hit(1);
                if (!bug.isAlive()) {
                    int awarded = state.recordKill(bug.getScoreValue());
                    int combo   = state.getCombo();
                    floatingTexts.add(new FloatingText(
                        bug.getLastWords(), bug.getX(), bug.getY() - 10,
                        new Color(255, 220, 80)));
                    String pts = "+" + awarded + (combo > 1 ? "  ×" + combo + "!" : "");
                    floatingTexts.add(new FloatingText(pts, bug.getX(), bug.getY() - 28,
                        combo > 2 ? new Color(255, 160, 40) : new Color(180, 255, 160)));
                    return true;
                }
            }
            return false;
        });
    }

    /**
     * Places a new sticky trap at ({@code x}, {@code y}).
     * Capped at {@link GameState#MAX_TRAPS} traps on screen at once.
     * Triggered by a single right click.
     */
    public void placeTrap(double x, double y) {
        if (traps.size() >= GameState.MAX_TRAPS) {
            floatingTexts.add(new FloatingText(
                "MAX TRAPS (" + GameState.MAX_TRAPS + ")", x, y, new Color(255, 200, 60)));
            return;
        }
        traps.add(new Trap(x, y));
    }

    /**
     * Returns the trap whose centre is closest to ({@code px},{@code py}) and
     * within its grab radius, or {@code null} if no trap is nearby.
     * Used to initiate a right-drag reposition.
     */
    public Trap getTrapAt(double px, double py) {
        for (Trap t : traps) {
            if (t.containsPoint(px, py)) return t;
        }
        return null;
    }

    /** Starts a new game from wave 1. */
    public void newGame() {
        bugs.clear();
        traps.clear();
        effects.clear();
        floatingTexts.clear();
        state.reset();
        spawner.loadWave(1);
        announceWave(1);
    }

    // ------------------------------------------------------------------ read-only views

    public List<Bug>          getBugs()         { return bugs; }
    public List<Trap>         getTraps()        { return traps; }
    public List<Effect>       getEffects()      { return effects; }
    public List<FloatingText> getFloatingTexts(){ return floatingTexts; }
    public GameState          getState()        { return state; }

    public int    getWaveAnnounceTick() { return waveAnnounceTick; }
    public String getWaveAnnounceText() { return waveAnnounceText; }

    // ------------------------------------------------------------------ helpers

    /**
     * Deals {@code damage} to every living bug within {@code radius} pixels of
     * ({@code x},{@code y}), removing killed bugs and awarding score.
     *
     * @return {@code true} if at least one bug was hit
     */
    private boolean applyAreaDamage(double x, double y, double radius, int damage) {
        boolean anyHit = false;
        List<Bug> toRemove = new ArrayList<>();
        for (Bug bug : bugs) {
            if (!bug.isAlive()) continue;
            double dx = bug.getX() - x, dy = bug.getY() - y;
            if (dx * dx + dy * dy <= radius * radius) {
                anyHit = true;
                bug.hit(damage);
                if (!bug.isAlive()) toRemove.add(bug);
            }
        }
        for (Bug bug : toRemove) {
            int awarded = state.recordKill(bug.getScoreValue());
            int combo   = state.getCombo();
            floatingTexts.add(new FloatingText(
                bug.getLastWords(), bug.getX(), bug.getY() - 10,
                new Color(255, 220, 80)));
            String pts = "+" + awarded + (combo > 1 ? "  ×" + combo + "!" : "");
            floatingTexts.add(new FloatingText(pts, bug.getX(), bug.getY() - 28,
                combo > 2 ? new Color(255, 160, 40) : new Color(180, 255, 160)));
            bugs.remove(bug);
        }
        return anyHit;
    }

    /** Perpendicular distance from point (px,py) to segment (x1,y1)-(x2,y2). */
    private static double distToSegment(double px, double py,
                                        double x1, double y1,
                                        double x2, double y2) {
        double dx = x2 - x1, dy = y2 - y1;
        if (dx == 0 && dy == 0) return Math.hypot(px - x1, py - y1);
        double t = Math.max(0, Math.min(1, ((px - x1) * dx + (py - y1) * dy) / (dx * dx + dy * dy)));
        return Math.hypot(px - (x1 + t * dx), py - (y1 + t * dy));
    }

    private static final String[] WAVE_NAMES = {
        "Wave 1: The Scout Party",
        "Wave 2: The Great Cockroach Migration",
        "Wave 3: Operation Airborne Assault",
        "Wave 4: Eight-Legged Invasion",
        "Wave 5: The Armoured Division",
        "Wave 6: They've Called for Reinforcements",
        "Wave 7: The Bug Apocalypse",
        "Wave 8: There Is No Hope. Only Bugs.",
    };

    private void announceWave(int wave) {
        int idx = Math.min(wave - 1, WAVE_NAMES.length - 1);
        waveAnnounceText  = WAVE_NAMES[idx];
        waveAnnounceTick  = WAVE_ANNOUNCE_TICKS;
    }
}
