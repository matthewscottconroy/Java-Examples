package com.wizardrogue.core;

import java.awt.Color;

/**
 * A short-lived visual animation triggered when the player casts a spell.
 *
 * <h2>Four animation kinds</h2>
 * <ul>
 *   <li><b>PROJECTILE</b> — a single {@code *} glyph travels tile-by-tile along
 *       a precomputed path, then flashes on impact.  Used by Ignis, Glacies, Nex.</li>
 *   <li><b>LINE</b> — every tile in a column/row flashes simultaneously for a
 *       few ticks.  Used by Fulgur.</li>
 *   <li><b>AREA</b> — a set of tiles (enemy positions, adjacent cells, or all
 *       visible cells) flashes and fades.  Used by Inferno, Glacialis, Arcana Nova,
 *       Lux Aeterna.</li>
 *   <li><b>SELF</b> — a cross/ring expands outward from the player's tile.
 *       Used by Sanatio, Umbra.</li>
 * </ul>
 *
 * <p>Effects are created in {@link GameController#handleSpellKey(int)} BEFORE the
 * spell lambda executes, so that enemy positions are still intact for path
 * computation.  {@link GameController#tick()} advances and garbage-collects them;
 * {@link com.wizardrogue.ui.DungeonPanel} renders them.
 */
public final class SpellEffect {

    public enum Kind { PROJECTILE, LINE, AREA, SELF }

    private final Kind    kind;
    private final Color   color;
    private final int     maxTicks;
    private int           tick;

    // PROJECTILE / LINE / AREA: list of [col, row] tile coordinates
    private final int[][] tiles;

    // SELF: origin tile
    private final int selfX, selfY;

    private SpellEffect(Kind kind, Color color, int maxTicks,
                        int[][] tiles, int selfX, int selfY) {
        this.kind     = kind;
        this.color    = color;
        this.maxTicks = maxTicks;
        this.tiles    = tiles;
        this.selfX    = selfX;
        this.selfY    = selfY;
    }

    // ------------------------------------------------------------------ factories

    /**
     * Projectile traveling along {@code path} (array of [col,row] tiles).
     * Each tile takes one game tick; three extra ticks are added for the
     * impact flash at the destination.
     */
    public static SpellEffect projectile(int[][] path, Color color) {
        int ticks = Math.max(1, path.length) + 3;
        return new SpellEffect(Kind.PROJECTILE, color, ticks, path, 0, 0);
    }

    /** All tiles in a line flash simultaneously for 7 ticks and fade. */
    public static SpellEffect line(int[][] tiles, Color color) {
        return new SpellEffect(Kind.LINE, color, 7, tiles, 0, 0);
    }

    /** An arbitrary set of tiles flashes for 8 ticks and fades. */
    public static SpellEffect area(int[][] tiles, Color color) {
        return new SpellEffect(Kind.AREA, color, 8, tiles, 0, 0);
    }

    /** Cross/ring expanding outward from ({@code x}, {@code y}) over 10 ticks. */
    public static SpellEffect self(int x, int y, Color color) {
        return new SpellEffect(Kind.SELF, color, 10, null, x, y);
    }

    // ------------------------------------------------------------------ lifecycle

    /** Advances the animation by one tick. */
    public void advance() { tick++; }

    /** Returns {@code true} once the animation is finished. */
    public boolean isExpired() { return tick >= maxTicks; }

    // ------------------------------------------------------------------ accessors

    public Kind    getKind()     { return kind; }
    public Color   getColor()    { return color; }
    public int     getTick()     { return tick; }
    public int     getMaxTicks() { return maxTicks; }
    public int[][] getTiles()    { return tiles; }
    public int     getSelfX()    { return selfX; }
    public int     getSelfY()    { return selfY; }

    /** Linear progress from 0.0 (just created) to 1.0 (expired). */
    public float progress() { return Math.min(1f, (float) tick / maxTicks); }

    /**
     * For {@link Kind#PROJECTILE}: the index into {@link #getTiles()} where the
     * projectile head currently sits.  Returns {@code -1} once the projectile has
     * reached the end (impact-flash phase begins).
     */
    public int projectileTileIndex() {
        if (tiles == null || tiles.length == 0) return -1;
        return tick < tiles.length ? tick : -1;
    }

    /**
     * For {@link Kind#PROJECTILE}: {@code true} after the head has reached the
     * last tile and the impact flash is playing.
     */
    public boolean isImpactPhase() {
        return tiles != null && tick >= tiles.length;
    }
}
