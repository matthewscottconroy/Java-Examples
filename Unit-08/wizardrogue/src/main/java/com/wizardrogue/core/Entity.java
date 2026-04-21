package com.wizardrogue.core;

import java.awt.Color;

/**
 * Base class for every living thing in the dungeon — the player and all monsters.
 *
 * <p>Entities occupy a single tile, have a display glyph and colour, and track
 * hit-points.  Combat is resolved by the {@link GameController}; entities
 * themselves only expose the data needed and a {@link #hit(int)} helper.
 */
public abstract class Entity {

    protected int   x;
    protected int   y;
    protected int   hp;
    protected int   maxHp;
    protected boolean alive;

    protected final char  glyph;
    protected final Color color;
    protected final String name;

    protected Entity(int x, int y, int hp, char glyph, Color color, String name) {
        this.x      = x;
        this.y      = y;
        this.hp     = hp;
        this.maxHp  = hp;
        this.alive  = true;
        this.glyph  = glyph;
        this.color  = color;
        this.name   = name;
    }

    /** Applies raw damage after defence reduction. Sets {@link #alive} to {@code false} when HP hits 0. */
    public void hit(int damage) {
        if (!alive) return;
        hp = Math.max(0, hp - damage);
        if (hp == 0) alive = false;
    }

    /** Heals by {@code amount} up to {@link #maxHp}. */
    public void heal(int amount) { hp = Math.min(maxHp, hp + amount); }

    // ------------------------------------------------------------------ accessors

    public int     getX()      { return x; }
    public int     getY()      { return y; }
    public int     getHp()     { return hp; }
    public int     getMaxHp()  { return maxHp; }
    public boolean isAlive()   { return alive; }
    public char    getGlyph()  { return glyph; }
    public Color   getColor()  { return color; }
    public String  getName()   { return name; }

    /** Teleports to the given map coordinates without checking passability. */
    public void setPosition(int x, int y) { this.x = x; this.y = y; }
}
