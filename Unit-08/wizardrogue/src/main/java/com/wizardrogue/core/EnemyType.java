package com.wizardrogue.core;

import java.awt.Color;

/**
 * Catalogue of every monster species in the dungeon.
 *
 * <p>Each entry encodes display data, base statistics, and movement speed
 * (expressed as ticks between moves).  The Lich is special: it has a ranged
 * attack that fires every {@link #specialCooldown} ticks.
 *
 * <pre>
 *  Type     Glyph  Colour          HP   Atk  Def  Speed  XP   Floor
 *  -------  -----  --------------  ---  ---  ---  -----  ---  -----
 *  Goblin   g      green           8    3    1    2      12   1+
 *  Orc      O      yellow-orange   22   7    3    3      30   2+
 *  Troll    T      moss-green      45   10   6    5      55   3+
 *  Demon    D      crimson         18   14   2    2      70   4+
 *  Lich     L      violet          60   18   8    3      120  5
 * </pre>
 */
public enum EnemyType {

    GOBLIN('g', new Color(100, 210, 60),  "Goblin",  8,  3,  1,  2,  12, 0),
    ORC   ('O', new Color(220, 160, 50),  "Orc",    22,  7,  3,  3,  30, 0),
    TROLL ('T', new Color(130, 185, 70),  "Troll",  45, 10,  6,  5,  55, 0),
    DEMON ('D', new Color(230,  55, 55),  "Demon",  18, 14,  2,  2,  70, 0),
    LICH  ('L', new Color(190,  80, 230), "Lich",   60, 18,  8,  3, 120, 5);

    public final char  glyph;
    public final Color color;
    public final String name;

    /** Base hit-points. */
    public final int baseHp;
    /** Base melee damage per hit. */
    public final int baseAtk;
    /** Base damage-reduction. */
    public final int baseDef;
    /** Ticks between each movement step (lower = faster). */
    public final int moveSpeed;
    /** XP awarded when killed. */
    public final int xpValue;
    /** Ticks between special (ranged) attacks; 0 = no special attack. */
    public final int specialCooldown;

    EnemyType(char g, Color c, String n,
              int hp, int atk, int def,
              int spd, int xp, int spc) {
        this.glyph          = g;
        this.color          = c;
        this.name           = n;
        this.baseHp         = hp;
        this.baseAtk        = atk;
        this.baseDef        = def;
        this.moveSpeed      = spd;
        this.xpValue        = xp;
        this.specialCooldown = spc;
    }

    /**
     * Returns a scaled HP value appropriate for {@code floorNumber}.
     * Enemies grow roughly 20% tougher per floor beyond their first appearance.
     */
    public int scaledHp(int floorNumber) {
        int bonus = Math.max(0, floorNumber - 1);
        return baseHp + bonus * (baseHp / 5);
    }

    /**
     * Returns the set of enemy types that can appear on the given floor.
     */
    public static EnemyType[] forFloor(int floor) {
        return switch (floor) {
            case 1  -> new EnemyType[]{GOBLIN};
            case 2  -> new EnemyType[]{GOBLIN, ORC};
            case 3  -> new EnemyType[]{GOBLIN, ORC, TROLL};
            case 4  -> new EnemyType[]{ORC, TROLL, DEMON};
            default -> new EnemyType[]{TROLL, DEMON, LICH};
        };
    }
}
