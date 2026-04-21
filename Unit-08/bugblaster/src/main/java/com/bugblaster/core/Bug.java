package com.bugblaster.core;

import java.awt.Graphics2D;

/**
 * Abstract base class for every bug species marching toward the Snack Bowl.
 *
 * <p>Bugs spawn at the edges of the game area and walk toward a fixed target
 * ({@link #TARGET_X}, {@link #TARGET_Y}) at the centre of the room. When a bug
 * reaches the target the player loses a life; when its HP drops to zero it is
 * dead and awards the player points.
 *
 * <p>Subclasses define visual appearance, movement speed, HP, and score value.
 * The {@link #onCursorNear(double, double)} hook lets subclasses react to the
 * player's cursor (e.g. flies dodge when the cursor gets close).
 */
public abstract class Bug {

    /** X coordinate of the target (centre of the play area). */
    public static final double TARGET_X = 450;
    /** Y coordinate of the target (centre of the play area). */
    public static final double TARGET_Y = 310;

    protected double x;
    protected double y;
    protected double vx;
    protected double vy;

    protected int hp;
    protected final int maxHp;
    protected boolean alive;
    protected boolean reached;
    protected boolean caught;

    /** Humorous last words shown when the bug is killed. */
    protected final String lastWords;

    protected int animTick;

    protected Bug(double x, double y, int hp, String lastWords) {
        this.x        = x;
        this.y        = y;
        this.hp       = hp;
        this.maxHp    = hp;
        this.alive    = true;
        this.reached  = false;
        this.caught   = false;
        this.lastWords = lastWords;
        this.animTick = 0;
        pointTowardTarget(getBaseSpeed());
    }

    // ------------------------------------------------------------------ abstract API

    /** Base movement speed in pixels per tick. */
    protected abstract double getBaseSpeed();

    /** Renders this bug at its current position. */
    public abstract void draw(Graphics2D g);

    /** Radius (pixels) used for hit-testing against player actions. */
    public abstract int getHitRadius();

    /** Points awarded when this bug is killed. */
    public abstract int getScoreValue();

    // ------------------------------------------------------------------ movement

    /**
     * Advances this bug by one simulation tick.
     *
     * <p>Caught bugs (stuck in a sticky trap) do not move. All other bugs
     * march forward; if they reach within 20 px of the target they trigger
     * the {@link #reached} flag and go off the board.
     */
    public void update() {
        if (caught) return;
        animTick++;
        x += vx;
        y += vy;
        if (distanceToTarget() < 20) {
            reached = true;
            alive   = false;
        }
    }

    /**
     * Called every tick with the current cursor position so that subclasses
     * that react to the cursor (e.g. {@link FlyBug}) can adjust velocity.
     * The default implementation does nothing.
     */
    public void onCursorNear(double cursorX, double cursorY) {}

    /** Sets velocity toward the target at the given speed. */
    protected void pointTowardTarget(double speed) {
        double dx   = TARGET_X - x;
        double dy   = TARGET_Y - y;
        double dist = Math.hypot(dx, dy);
        if (dist > 0) {
            vx = (dx / dist) * speed;
            vy = (dy / dist) * speed;
        }
    }

    // ------------------------------------------------------------------ combat

    /** Applies {@code damage} hit-points of damage, killing this bug if HP reaches zero. */
    public void hit(int damage) {
        if (!alive) return;
        hp = Math.max(0, hp - damage);
        if (hp == 0) alive = false;
    }

    // ------------------------------------------------------------------ geometry

    /** Returns the straight-line distance from this bug to the target. */
    protected double distanceToTarget() {
        return Math.hypot(TARGET_X - x, TARGET_Y - y);
    }

    /** Returns {@code true} if the point ({@code px}, {@code py}) is within the hit radius. */
    public boolean contains(double px, double py) {
        int r = getHitRadius();
        return (px - x) * (px - x) + (py - y) * (py - y) <= (double) r * r;
    }

    // ------------------------------------------------------------------ accessors

    public boolean isAlive()      { return alive; }
    public boolean hasReached()   { return reached; }
    public boolean isCaught()     { return caught; }
    public double  getX()         { return x; }
    public double  getY()         { return y; }
    public int     getHp()        { return hp; }
    public int     getMaxHp()     { return maxHp; }
    public String  getLastWords() { return lastWords; }

    /** Marks this bug as caught/released by a sticky trap, halting or resuming movement. */
    public void setCaught(boolean caught) {
        this.caught = caught;
        if (!caught) {
            // Resume marching toward the target
            pointTowardTarget(getBaseSpeed());
        }
    }

    // ------------------------------------------------------------------ helpers for subclasses

    /** Draws a small HP bar centred above the bug (only when HP &lt; max). */
    protected void drawHpBar(Graphics2D g, int cx, int cy) {
        if (hp >= maxHp) return;
        int barW = 24, barH = 3;
        int bx = cx - barW / 2, by = cy - 18;
        g.setColor(java.awt.Color.DARK_GRAY);
        g.fillRect(bx, by, barW, barH);
        g.setColor(new java.awt.Color(60, 200, 60));
        g.fillRect(bx, by, (int)(barW * (double) hp / maxHp), barH);
    }
}
