package com.wizardrogue.core;

import java.awt.Color;

/**
 * Every collectable item type in the dungeon.
 *
 * <p>Items come in two categories:
 * <ul>
 *   <li><b>Consumables</b> — take effect immediately when picked up
 *       ({@link #autoUse} is {@code true}).</li>
 *   <li><b>Equipment</b> — added to the player's inventory; automatically
 *       equipped if better than the current slot ({@link #autoUse} is
 *       {@code false}).</li>
 * </ul>
 */
public enum ItemType {

    HEALTH_POTION('!', new Color(220,  70,  70), "Health Potion", true),
    MANA_POTION  ('!', new Color( 70, 120, 250), "Mana Potion",   true),
    STAFF        ('/', new Color(220, 200,  60), "Magic Staff",   false),
    ROBE         (']', new Color( 60, 200, 220), "Wizard Robe",   false),
    SPELL_SCROLL ('?', new Color(210,  80, 210), "Spell Scroll",  true),
    GOLD         ('$', new Color(220, 190,  30), "Gold Coins",    true),
    RING         ('=', new Color(180, 240, 180), "Magic Ring",    false),
    TOME         ('+', new Color(160, 130, 220), "Ancient Tome",  false);

    public final char   glyph;
    public final Color  color;
    public final String displayName;
    /** Whether this item triggers its effect immediately on pickup. */
    public final boolean autoUse;

    ItemType(char g, Color c, String n, boolean auto) {
        this.glyph       = g;
        this.color       = c;
        this.displayName = n;
        this.autoUse     = auto;
    }
}
