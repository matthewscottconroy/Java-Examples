package com.wizardrogue.core;

import java.util.Arrays;
import java.util.List;

/**
 * A wizard spell defined by its Q/E/R key sequence, mana cost, level
 * requirement, and effect.
 *
 * <h2>Spell casting and the combo-window system</h2>
 * <p>The player casts spells by pressing a sequence of spell keys (Q, E, R)
 * within the 2.5-second combo window maintained by {@link InputBuffer}.  After
 * each keypress the controller checks all known spells for a sequence match.
 * If one is found — and the player has enough MP and meets the level requirement
 * — the spell fires.
 *
 * <h2>Spell sequences</h2>
 * <pre>
 *   Spell        Sequence     MP   Lvl  Description
 *   -----------  -----------  ---  ---  --------------------------------------
 *   Ignis        Q Q           5    1   Fire dart — 20+ dmg, range 8
 *   Glacies      E E           5    1   Ice shard — 12 dmg + slow, range 6
 *   Fulgur       Q E Q        10    2   Lightning bolt — hits entire column
 *   Sanatio      R R           8    2   Heals 20 HP
 *   Umbra        Q Q E E      12    3   Shadow step — teleport 5 tiles forward
 *   Inferno      E Q E Q      15    3   Ring of fire — 18 dmg all adjacent
 *   Glacialis    E E Q E E    20    4   Blizzard — freeze all visible enemies
 *   Nex          Q E R        18    4   Death bolt — 45 dmg single target
 *   Lux Aeterna  R R R        25    5   Radiant burst — full heal + AoE 30 dmg
 *   Arcana Nova  Q E R Q E R  30    5   Ultimate — 50 dmg all visible enemies
 * </pre>
 */
public final class Spell {

    /** Functional interface for the actual spell effect. */
    @FunctionalInterface
    public interface Effect {
        /**
         * Executes the spell.
         *
         * @param player  the casting player
         * @param map     the current dungeon floor
         * @param enemies all living enemies on the floor
         * @param log     message log for narration
         */
        void cast(Player player, DungeonMap map, List<Enemy> enemies, MessageLog log);
    }

    private final String   name;
    private final String   description;
    private final int[]    sequence;
    private final int      mpCost;
    private final int      minLevel;
    private final Effect   effect;

    public Spell(String name, String description,
                 int[] sequence, int mpCost, int minLevel,
                 Effect effect) {
        this.name        = name;
        this.description = description;
        this.sequence    = Arrays.copyOf(sequence, sequence.length);
        this.mpCost      = mpCost;
        this.minLevel    = minLevel;
        this.effect      = effect;
    }

    // ------------------------------------------------------------------ accessors

    public String getName()       { return name; }
    public String getDescription(){ return description; }
    public int[]  getSequence()   { return Arrays.copyOf(sequence, sequence.length); }
    public int    getMpCost()     { return mpCost; }
    public int    getMinLevel()   { return minLevel; }
    public int    sequenceLength(){ return sequence.length; }

    /**
     * Returns {@code true} when the player has enough MP and meets the level
     * requirement.  Called by the controller before casting.
     */
    public boolean canCast(Player player) {
        return player.getMp() >= mpCost && player.getLevel() >= minLevel;
    }

    /** Deducts mana and invokes the spell effect. */
    public void cast(Player player, DungeonMap map, List<Enemy> enemies, MessageLog log) {
        player.spendMp(mpCost);
        effect.cast(player, map, enemies, log);
    }

    // ------------------------------------------------------------------ display helpers

    /**
     * Returns the sequence as a human-readable string like {@code "Q E Q"}.
     * Used in the spell reference list in the stats panel.
     */
    public String sequenceLabel() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sequence.length; i++) {
            if (i > 0) sb.append(' ');
            sb.append(switch (sequence[i]) {
                case java.awt.event.KeyEvent.VK_Q -> "Q";
                case java.awt.event.KeyEvent.VK_E -> "E";
                case java.awt.event.KeyEvent.VK_R -> "R";
                default                           -> "?";
            });
        }
        return sb.toString();
    }

    @Override
    public String toString() { return name + " [" + sequenceLabel() + "]"; }
}
