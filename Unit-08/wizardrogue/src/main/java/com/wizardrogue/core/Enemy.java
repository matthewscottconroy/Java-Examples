package com.wizardrogue.core;

import java.util.List;

/**
 * A dungeon monster that pursues and attacks the player in real time.
 *
 * <h2>AI behaviour</h2>
 * <p>Enemies alternate between two states:
 * <ul>
 *   <li><b>Wandering</b> — the player is outside line-of-sight; the enemy
 *       drifts randomly.</li>
 *   <li><b>Pursuing</b> — the player is visible; the enemy moves toward the
 *       player each tick using simple obstacle-aware steering.</li>
 * </ul>
 *
 * <h2>Movement and speed</h2>
 * <p>Each enemy has a {@link EnemyType#moveSpeed} (ticks between steps).
 * The {@link GameController} calls {@link #tick(Player, DungeonMap, List, MessageLog)}
 * every game tick; the enemy only acts when its internal counter fires.
 *
 * <h2>Special attacks</h2>
 * <p>The Lich fires a ranged bolt every {@link EnemyType#specialCooldown} ticks
 * if the player is in line-of-sight but not adjacent.
 */
public final class Enemy extends Entity {

    private final EnemyType type;
    private int moveTick;
    private int specialTick;
    private boolean alerted;

    // Status effects
    private int frozenTicks;  // cannot move while > 0
    private int slowTicks;    // moves every moveSpeed*2 ticks while > 0

    public Enemy(EnemyType type, int x, int y, int floorNumber) {
        super(x, y, type.scaledHp(floorNumber), type.glyph, type.color, type.name);
        this.type         = type;
        this.moveTick     = 0;
        this.specialTick  = 0;
        this.alerted      = false;
    }

    // ------------------------------------------------------------------ per-tick AI

    /**
     * Executes this enemy's AI for one game tick.
     * Should only be called when the enemy is alive.
     */
    public void tick(Player player, DungeonMap map,
                     List<Enemy> others, MessageLog log) {
        if (!alive) return;

        // Tick down status effects
        if (frozenTicks > 0) { frozenTicks--; return; }
        if (slowTicks   > 0) slowTicks--;

        int effectiveSpeed = type.moveSpeed + (slowTicks > 0 ? type.moveSpeed : 0);
        moveTick++;

        // Special attack tick (Lich ranged bolt)
        if (type.specialCooldown > 0) {
            specialTick++;
            if (specialTick >= type.specialCooldown
                    && map.hasLOS(x, y, player.getX(), player.getY())
                    && !isAdjacentTo(player)) {
                specialTick = 0;
                fireSpecial(player, log);
                return;
            }
        }

        if (moveTick < effectiveSpeed) return;
        moveTick = 0;

        boolean canSee = map.hasLOS(x, y, player.getX(), player.getY());
        if (canSee) alerted = true;

        if (alerted) {
            if (isAdjacentTo(player)) {
                // Melee attack
                int dmg = Math.max(1, type.baseAtk - player.getTotalDef());
                player.hit(dmg);
                log.add("The " + name + " hits you for " + dmg + " damage!");
                if (!player.isAlive()) log.add("You have been slain by the " + name + "...");
            } else {
                // Move toward player
                moveToward(player.getX(), player.getY(), map, others, player);
            }
        } else {
            // Wander randomly
            wander(map, others, player);
        }
    }

    // ------------------------------------------------------------------ movement helpers

    /**
     * Steers toward ({@code tx},{@code ty}) using greedy obstacle avoidance:
     * tries the preferred direction, then 90° turns, then retreat.
     */
    private void moveToward(int tx, int ty, DungeonMap map,
                            List<Enemy> others, Player player) {
        int pdx = Integer.signum(tx - x);
        int pdy = Integer.signum(ty - y);

        // Try preferred diagonal axes then fall back
        int[][] tries = {
            {pdx, pdy},
            {pdx, 0},
            {0,   pdy},
            {-pdy, pdx},
            {pdy, -pdx},
            {-pdx, -pdy}
        };
        for (int[] d : tries) {
            if (d[0] == 0 && d[1] == 0) continue;
            int nx = x + d[0], ny = y + d[1];
            if (canStep(nx, ny, map, others, player)) {
                x = nx; y = ny;
                return;
            }
        }
    }

    private void wander(DungeonMap map, List<Enemy> others, Player player) {
        int[][] dirs = {{0,-1},{0,1},{-1,0},{1,0}};
        // Shuffle-like: try a random direction first
        int start = (int)(Math.random() * 4);
        for (int i = 0; i < 4; i++) {
            int[] d = dirs[(start + i) % 4];
            int nx = x + d[0], ny = y + d[1];
            if (canStep(nx, ny, map, others, player)) {
                x = nx; y = ny;
                return;
            }
        }
    }

    private boolean canStep(int nx, int ny, DungeonMap map,
                            List<Enemy> others, Player player) {
        if (!map.isPassable(nx, ny)) return false;
        if (player.getX() == nx && player.getY() == ny) return false;
        for (Enemy o : others) {
            if (o != this && o.isAlive() && o.getX() == nx && o.getY() == ny) return false;
        }
        return true;
    }

    // ------------------------------------------------------------------ special attack

    private void fireSpecial(Player player, MessageLog log) {
        // Lich bolt: ignores defence, 10 flat damage
        int dmg = 10;
        player.hit(dmg);
        log.add("The Lich fires an arcane bolt! You take " + dmg + " damage!");
    }

    // ------------------------------------------------------------------ status effects

    public void freeze(int ticks) { frozenTicks = Math.max(frozenTicks, ticks); }
    public void slow(int ticks)   { slowTicks   = Math.max(slowTicks,   ticks); }
    public boolean isFrozen()     { return frozenTicks > 0; }

    // ------------------------------------------------------------------ helpers

    private boolean isAdjacentTo(Entity other) {
        return Math.abs(x - other.getX()) <= 1 && Math.abs(y - other.getY()) <= 1;
    }

    public EnemyType getType() { return type; }
}
