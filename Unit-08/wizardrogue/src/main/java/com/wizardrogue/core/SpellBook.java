package com.wizardrogue.core;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory and registry for all wizard spells in the game.
 *
 * <p>Spells are keyed on Q, E, R sequences (see {@link Spell} Javadoc for the
 * full table).  {@link #spellsForLevel(int)} returns every spell the player
 * should know at a given level; the controller calls this on level-up to
 * auto-learn newly unlocked spells.
 *
 * <h2>Spell targeting conventions</h2>
 * <ul>
 *   <li><b>Directional</b> (Ignis, Glacies, Fulgur, Nex) — scans tiles in the
 *       player's {@link Direction#getFacing() facing direction} up to a range
 *       limit, stopping at walls.</li>
 *   <li><b>Area</b> (Inferno, Glacialis, Arcana Nova) — affects all enemies
 *       in a radius or within line-of-sight.</li>
 *   <li><b>Self</b> (Sanatio, Umbra, Lux Aeterna) — affects only the player.</li>
 * </ul>
 */
public final class SpellBook {

    private static final int Q = KeyEvent.VK_Q;
    private static final int E = KeyEvent.VK_E;
    private static final int R = KeyEvent.VK_R;

    private SpellBook() {}

    /** Returns the complete list of all spells in the game. */
    public static List<Spell> allSpells() {
        List<Spell> spells = new ArrayList<>();

        // ── Level 1 ──────────────────────────────────────────────────────────

        spells.add(new Spell(
            "Ignis",
            "Fire dart — scorches one enemy in facing direction (range 8)",
            new int[]{Q, Q}, 5, 1,
            (player, map, enemies, log) -> {
                Enemy target = firstInDirection(player, map, enemies, 8);
                if (target == null) {
                    log.add("Ignis! Your fire dart sizzles into the dark.");
                } else {
                    int dmg = 20 + player.getLevel() * 3;
                    target.hit(dmg);
                    log.add("Ignis! The " + target.getName()
                        + " takes " + dmg + " fire damage!"
                        + (target.isAlive() ? "" : " It burns to cinders!"));
                }
            }));

        spells.add(new Spell(
            "Glacies",
            "Ice shard — 12 dmg + slow in facing direction (range 6)",
            new int[]{E, E}, 5, 1,
            (player, map, enemies, log) -> {
                Enemy target = firstInDirection(player, map, enemies, 6);
                if (target == null) {
                    log.add("Glacies! The ice shard shatters on the wall.");
                } else {
                    int dmg = 12 + player.getLevel() * 2;
                    target.hit(dmg);
                    target.slow(8);
                    log.add("Glacies! The " + target.getName()
                        + " takes " + dmg + " cold damage and is slowed!");
                }
            }));

        // ── Level 2 ──────────────────────────────────────────────────────────

        spells.add(new Spell(
            "Fulgur",
            "Lightning bolt — 20 dmg to ALL enemies in facing column",
            new int[]{Q, E, Q}, 10, 2,
            (player, map, enemies, log) -> {
                Direction d = player.getFacing();
                int hit = 0;
                for (int dist = 1; dist <= 12; dist++) {
                    int tx = player.getX() + d.dx * dist;
                    int ty = player.getY() + d.dy * dist;
                    if (!map.isPassable(tx, ty)) break;
                    for (Enemy e : enemies) {
                        if (e.isAlive() && e.getX() == tx && e.getY() == ty) {
                            int dmg = 20 + player.getLevel() * 2;
                            e.hit(dmg);
                            hit++;
                            log.add("Fulgur! " + e.getName() + " is struck for " + dmg + "!");
                        }
                    }
                }
                if (hit == 0) log.add("Fulgur! Lightning cracks the dungeon walls.");
            }));

        spells.add(new Spell(
            "Sanatio",
            "Heals 20 + level × 3 HP",
            new int[]{R, R}, 8, 2,
            (player, map, enemies, log) -> {
                int amount = 20 + player.getLevel() * 3;
                int before = player.getHp();
                player.heal(amount);
                int healed = player.getHp() - before;
                log.add("Sanatio! You recover " + healed + " hit points.");
            }));

        // ── Level 3 ──────────────────────────────────────────────────────────

        spells.add(new Spell(
            "Umbra",
            "Shadow step — teleport 5 tiles forward (stops at walls)",
            new int[]{Q, Q, E, E}, 12, 3,
            (player, map, enemies, log) -> {
                Direction d = player.getFacing();
                int destX = player.getX(), destY = player.getY();
                for (int i = 1; i <= 5; i++) {
                    int tx = player.getX() + d.dx * i;
                    int ty = player.getY() + d.dy * i;
                    if (!map.isPassable(tx, ty)) break;
                    boolean occupied = enemies.stream()
                        .anyMatch(e -> e.isAlive() && e.getX() == tx && e.getY() == ty);
                    if (!occupied) { destX = tx; destY = ty; }
                }
                player.setPosition(destX, destY);
                log.add("Umbra! You dissolve into shadow and reappear.");
            }));

        spells.add(new Spell(
            "Inferno",
            "Ring of fire — 18 + lvl×2 dmg to all adjacent enemies",
            new int[]{E, Q, E, Q}, 15, 3,
            (player, map, enemies, log) -> {
                int hit = 0;
                for (Enemy e : enemies) {
                    if (!e.isAlive()) continue;
                    int dx = Math.abs(e.getX() - player.getX());
                    int dy = Math.abs(e.getY() - player.getY());
                    if (dx <= 1 && dy <= 1) {
                        int dmg = 18 + player.getLevel() * 2;
                        e.hit(dmg);
                        hit++;
                        log.add("Inferno! " + e.getName() + " is engulfed for " + dmg + " damage!");
                    }
                }
                if (hit == 0) log.add("Inferno! The flames find no targets.");
            }));

        // ── Level 4 ──────────────────────────────────────────────────────────

        spells.add(new Spell(
            "Glacialis",
            "Blizzard — freezes ALL visible enemies for 5 ticks, 10 dmg",
            new int[]{E, E, Q, E, E}, 20, 4,
            (player, map, enemies, log) -> {
                int hit = 0;
                for (Enemy e : enemies) {
                    if (e.isAlive() && map.hasLOS(
                            player.getX(), player.getY(), e.getX(), e.getY())) {
                        e.freeze(5);
                        e.hit(10);
                        hit++;
                    }
                }
                log.add("Glacialis! " + hit + " enem" + (hit == 1 ? "y" : "ies")
                    + " caught in the blizzard!");
            }));

        spells.add(new Spell(
            "Nex",
            "Death bolt — 45 + lvl×5 dmg to first enemy in facing direction",
            new int[]{Q, E, R}, 18, 4,
            (player, map, enemies, log) -> {
                Enemy target = firstInDirection(player, map, enemies, 15);
                if (target == null) {
                    log.add("Nex! The death bolt fades into darkness.");
                } else {
                    int dmg = 45 + player.getLevel() * 5;
                    target.hit(dmg);
                    log.add("Nex! The " + target.getName()
                        + " is struck by death itself for " + dmg + " damage!"
                        + (target.isAlive() ? "" : " It collapses."));
                }
            }));

        // ── Level 5 ──────────────────────────────────────────────────────────

        spells.add(new Spell(
            "Lux Aeterna",
            "Radiant burst — full heal + 30 dmg to all visible enemies",
            new int[]{R, R, R}, 25, 5,
            (player, map, enemies, log) -> {
                player.heal(player.getMaxHp());
                log.add("Lux Aeterna! Pure light fills the dungeon!");
                int hit = 0;
                for (Enemy e : enemies) {
                    if (e.isAlive() && map.hasLOS(
                            player.getX(), player.getY(), e.getX(), e.getY())) {
                        e.hit(30);
                        hit++;
                    }
                }
                log.add(hit + " enem" + (hit == 1 ? "y" : "ies")
                    + " seared by holy radiance. HP fully restored.");
            }));

        spells.add(new Spell(
            "Arcana Nova",
            "Ultimate — 50 + lvl×6 dmg to EVERY visible enemy",
            new int[]{Q, E, R, Q, E, R}, 30, 5,
            (player, map, enemies, log) -> {
                int hit = 0;
                for (Enemy e : enemies) {
                    if (e.isAlive() && map.hasLOS(
                            player.getX(), player.getY(), e.getX(), e.getY())) {
                        int dmg = 50 + player.getLevel() * 6;
                        e.hit(dmg);
                        hit++;
                    }
                }
                log.add("ARCANA NOVA! " + hit + " enem"
                    + (hit == 1 ? "y" : "ies") + " obliterated!");
            }));

        return spells;
    }

    /**
     * Returns the subset of spells the player should know at {@code level}.
     * Called on level-up to auto-learn newly unlocked spells.
     */
    public static List<Spell> spellsForLevel(int level) {
        return allSpells().stream()
            .filter(s -> s.getMinLevel() <= level)
            .toList();
    }

    // ------------------------------------------------------------------ targeting helper

    /**
     * Returns the first living enemy within {@code range} tiles in the player's
     * facing direction, stopping at impassable tiles.
     */
    static Enemy firstInDirection(Player player, DungeonMap map,
                                  List<Enemy> enemies, int range) {
        Direction d = player.getFacing();
        for (int dist = 1; dist <= range; dist++) {
            int tx = player.getX() + d.dx * dist;
            int ty = player.getY() + d.dy * dist;
            if (!map.isPassable(tx, ty)) break;
            for (Enemy e : enemies) {
                if (e.isAlive() && e.getX() == tx && e.getY() == ty) return e;
            }
        }
        return null;
    }
}
