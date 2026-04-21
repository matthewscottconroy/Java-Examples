package com.wizardrogue.core;

import java.awt.Color;

/**
 * Every distinct terrain type that can occupy a dungeon cell.
 *
 * <p>Each tile carries its display glyph, foreground colour, whether it blocks
 * movement, and whether it blocks line-of-sight (important for the fog-of-war
 * visibility computation).
 */
public enum Tile {

    WALL        ('#', new Color(70,  68,  88), true,  true),
    FLOOR       ('.', new Color(90,  75,  55), false, false),
    DOOR        ('+', new Color(160, 120, 60), false, false),
    STAIR_DOWN  ('>', new Color(255, 200, 50), false, false),
    STAIR_UP    ('<', new Color(200, 210, 100),false, false);

    /** The ASCII character rendered for this tile. */
    public final char glyph;

    /** Full-brightness foreground colour when the tile is visible. */
    public final Color color;

    /** {@code true} if entities cannot walk through this tile. */
    public final boolean blocksMove;

    /** {@code true} if this tile blocks the visibility ray cast. */
    public final boolean blocksLight;

    Tile(char glyph, Color color, boolean blocksMove, boolean blocksLight) {
        this.glyph       = glyph;
        this.color       = color;
        this.blocksMove  = blocksMove;
        this.blocksLight = blocksLight;
    }

    /** Shorthand passability check. */
    public boolean isPassable() { return !blocksMove; }
}
