package com.wizardrogue.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Central game-logic controller: owns all entities and drives the simulation.
 *
 * <p>The controller is Swing-free.  The UI calls three methods:
 * <ul>
 *   <li>{@link #tick()} — called every 100 ms by the panel's game timer to
 *       advance enemy AI, tick down status effects, and regenerate MP.</li>
 *   <li>{@link #playerMove(Direction)} — called immediately on each direction
 *       keypress for responsive movement; also handles bump-to-attack.</li>
 *   <li>{@link #handleSpellKey(int)} — called for Q, E, or R keypress; updates
 *       the {@link InputBuffer} and checks for a completed spell sequence.</li>
 * </ul>
 * All other immediate actions (pickup, stairs, pause) are handled inline in
 * the panel's {@code keyPressed} and then delegated here.
 *
 * <h2>Game states</h2>
 * <pre>
 *   PLAYING  → normal gameplay
 *   PAUSED   → Escape pressed; no ticks or movement
 *   DEAD     → player HP reached 0
 *   VICTORY  → all 5 floors cleared
 * </pre>
 */
public final class GameController {

    public enum State { PLAYING, PAUSED, DEAD, VICTORY }

    private static final int MAX_FLOORS     = 5;
    private static final int VIS_RADIUS     = 18;
    private static final int MP_REGEN_TICKS = 8;   // ticks between +1 MP regen

    private DungeonMap         map;
    private Player             player;
    private List<Enemy>        enemies;
    private List<Item>         items;
    private MessageLog         log;
    private InputBuffer        inputBuffer;
    private State              state;
    private int                floorNumber;
    private int                mpRegenTick;
    private final List<SpellEffect> activeEffects = new ArrayList<>();

    private final Random rng = new Random();

    public GameController() {
        log         = new MessageLog();
        inputBuffer = new InputBuffer();
        newGame();
    }

    // ------------------------------------------------------------------ init

    /** Starts a fresh game from floor 1. */
    public void newGame() {
        floorNumber = 1;
        state       = State.PLAYING;
        player      = null;   // force generateFloor() to create a fresh, living player
        inputBuffer.clear();
        log.clear();
        activeEffects.clear();
        generateFloor();
        log.add("Welcome, Wizard.  The dungeon beckons.  Reach floor " + MAX_FLOORS + " to win.");
        log.add("Move: WASD  |  Spells: Q E R sequences  |  Pickup: Space  |  Stairs: ,");
    }

    // ------------------------------------------------------------------ floor generation

    private void generateFloor() {
        activeEffects.clear();
        map = new DungeonMap();
        map.generate(floorNumber, rng);

        // Create or reposition player
        if (player == null) {
            player = new Player(map.getStartX(), map.getStartY());
            // Level 1 spells
            for (Spell s : SpellBook.spellsForLevel(1)) player.learnSpell(s);
        } else {
            player.setPosition(map.getStartX(), map.getStartY());
            player.setFloor(floorNumber);
        }

        enemies = new ArrayList<>();
        items   = new ArrayList<>();

        // Spawn enemies
        EnemyType[] pool = EnemyType.forFloor(floorNumber);
        for (int[] spawn : map.getEnemySpawns()) {
            EnemyType type = pool[rng.nextInt(pool.length)];
            enemies.add(new Enemy(type, spawn[0], spawn[1], floorNumber));
        }

        // Spawn items
        ItemType[] itemPool = {
            ItemType.HEALTH_POTION, ItemType.HEALTH_POTION,
            ItemType.MANA_POTION,
            ItemType.GOLD,
            ItemType.STAFF, ItemType.ROBE,
            ItemType.RING, ItemType.TOME,
            ItemType.SPELL_SCROLL
        };
        for (int[] spawn : map.getItemSpawns()) {
            ItemType type = itemPool[rng.nextInt(itemPool.length)];
            int value = switch (type) {
                case HEALTH_POTION -> 20 + floorNumber * 5;
                case MANA_POTION   -> 12 + floorNumber * 3;
                case GOLD          -> 10 + rng.nextInt(30) + floorNumber * 5;
                case STAFF         -> 2 + floorNumber;
                case ROBE          -> 1 + floorNumber;
                case RING          -> 5 + floorNumber * 2;
                case TOME          -> 8 + floorNumber * 2;
                case SPELL_SCROLL  -> 0;
            };
            items.add(new Item(type, spawn[0], spawn[1], value));
        }

        map.computeVisibility(player.getX(), player.getY(), VIS_RADIUS);
        log.add("You descend to floor " + floorNumber + ".");
    }

    // ------------------------------------------------------------------ game tick

    /**
     * Advances the simulation by one tick (~100 ms).
     * Enemies move, MP regenerates, dead entities are cleaned up.
     */
    public void tick() {
        if (state != State.PLAYING) return;

        // MP regeneration
        mpRegenTick++;
        if (mpRegenTick >= MP_REGEN_TICKS) {
            mpRegenTick = 0;
            player.restoreMp(1);
        }

        // Enemy AI
        for (Enemy e : enemies) {
            if (e.isAlive()) e.tick(player, map, enemies, log);
        }

        // Clean up dead enemies
        enemies.removeIf(e -> !e.isAlive());

        // Check player death
        if (!player.isAlive()) state = State.DEAD;

        // Prune expired input buffer entries (side effect of size())
        inputBuffer.size();

        // Advance and expire visual spell effects
        activeEffects.forEach(SpellEffect::advance);
        activeEffects.removeIf(SpellEffect::isExpired);
    }

    // ------------------------------------------------------------------ player movement

    /**
     * Attempts to move the player in {@code dir}.
     * If the target tile is occupied by an enemy, attacks instead.
     * Returns immediately if the game is paused or the player is dead.
     */
    public void playerMove(Direction dir) {
        if (state != State.PLAYING) return;

        int tx = player.getX() + dir.dx;
        int ty = player.getY() + dir.dy;

        // Bump-to-attack
        Enemy target = enemyAt(tx, ty);
        if (target != null) {
            int dmg = Math.max(1, player.getTotalAtk() - target.getType().baseDef);
            target.hit(dmg);
            log.add("You hit the " + target.getName() + " for " + dmg + " damage."
                + (target.isAlive() ? "" : " It dies!"));
            if (!target.isAlive()) {
                int gained = target.getType().xpValue;
                int levelsGained = player.gainXp(gained, log);
                if (levelsGained > 0) unlockNewSpells();
            }
            enemies.removeIf(e -> !e.isAlive());
        } else {
            player.tryMove(dir, map, enemies);
        }

        map.computeVisibility(player.getX(), player.getY(), VIS_RADIUS);
    }

    // ------------------------------------------------------------------ spell input

    /**
     * Pushes a spell key (Q/E/R) into the input buffer and checks whether the
     * current buffer tail matches any known spell sequence.
     *
     * <p>This is the core of the combo-window spell system: every Q/E/R
     * keypress contributes to a potential spell without requiring the player
     * to explicitly enter "spell mode" — sequences expire automatically.
     */
    public void handleSpellKey(int vk) {
        if (state != State.PLAYING) return;
        inputBuffer.push(vk);

        // Check for a matching spell among those the player knows and can afford
        List<Spell> castable = player.getKnownSpells().stream()
            .filter(s -> s.canCast(player))
            .toList();

        Spell match = inputBuffer.findMatch(castable);
        if (match != null) {
            inputBuffer.clear();
            // Create visual effect BEFORE cast so enemy positions are still intact
            SpellEffect fx = createSpellEffect(match.getName());
            if (fx != null) activeEffects.add(fx);
            match.cast(player, map, enemies, log);
            enemies.removeIf(e -> !e.isAlive());
            if (!player.isAlive()) state = State.DEAD;
            // Recompute visibility in case of teleport spells
            map.computeVisibility(player.getX(), player.getY(), VIS_RADIUS);
        }
    }

    // ------------------------------------------------------------------ facing

    /**
     * Rotates the player to face {@code dir} without moving.
     * Recomputes visibility so the facing cursor is immediately updated.
     */
    public void playerFace(Direction dir) {
        if (state != State.PLAYING) return;
        player.setFacing(dir);
        map.computeVisibility(player.getX(), player.getY(), VIS_RADIUS);
    }

    // ------------------------------------------------------------------ pickup

    /**
     * Attempts to pick up an item on the player's current tile.
     * Auto-use items apply immediately; equipment is auto-equipped if better.
     */
    public void tryPickup() {
        if (state != State.PLAYING) return;
        Item found = null;
        for (Item item : items) {
            if (!item.isPickedUp()
                    && item.getX() == player.getX()
                    && item.getY() == player.getY()) {
                found = item;
                break;
            }
        }
        if (found == null) {
            log.add("Nothing to pick up here.");
            return;
        }

        if (found.getType() == ItemType.SPELL_SCROLL) {
            // Teach a random spell the player doesn't yet know
            List<Spell> all    = SpellBook.allSpells();
            List<Spell> known  = player.getKnownSpells();
            List<Spell> unknown = all.stream()
                .filter(s -> known.stream().noneMatch(k -> k.getName().equals(s.getName())))
                .toList();
            if (!unknown.isEmpty()) {
                Spell learned = unknown.get(rng.nextInt(unknown.size()));
                player.learnSpell(learned);
                log.add("The scroll teaches you: " + learned + "!");
            } else {
                log.add("You already know every spell. The scroll crumbles.");
            }
            found.setPickedUp();
        } else {
            found.applyTo(player, log);
        }

        items.removeIf(Item::isPickedUp);
    }

    // ------------------------------------------------------------------ stairs

    /** Descends to the next floor if standing on {@link Tile#STAIR_DOWN}. */
    public void tryDescend() {
        if (state != State.PLAYING) return;
        if (player.getX() == map.getStairX() && player.getY() == map.getStairY()) {
            if (floorNumber >= MAX_FLOORS) {
                state = State.VICTORY;
                log.add("*** You have cleared all floors! VICTORY! ***");
            } else {
                floorNumber++;
                generateFloor();
            }
        } else {
            log.add("There are no stairs here.");
        }
    }

    // ------------------------------------------------------------------ pause

    public void togglePause() {
        if (state == State.PLAYING) state = State.PAUSED;
        else if (state == State.PAUSED) state = State.PLAYING;
    }

    // ------------------------------------------------------------------ helpers

    private Enemy enemyAt(int x, int y) {
        for (Enemy e : enemies) {
            if (e.isAlive() && e.getX() == x && e.getY() == y) return e;
        }
        return null;
    }

    private void unlockNewSpells() {
        List<Spell> all = SpellBook.spellsForLevel(player.getLevel());
        List<Spell> known = player.getKnownSpells();
        for (Spell s : all) {
            boolean alreadyKnown = known.stream()
                .anyMatch(k -> k.getName().equals(s.getName()));
            if (!alreadyKnown) {
                player.learnSpell(s);
                log.add("You have learned: " + s + "!");
            }
        }
    }

    // ------------------------------------------------------------------ spell effect factory

    private SpellEffect createSpellEffect(String spellName) {
        int px = player.getX(), py = player.getY();
        Direction d = player.getFacing();

        return switch (spellName) {
            case "Ignis"       -> SpellEffect.projectile(
                                    buildProjectilePath(px, py, d, 8), new Color(255, 110, 20));
            case "Glacies"     -> SpellEffect.projectile(
                                    buildProjectilePath(px, py, d, 6), new Color(80, 180, 255));
            case "Nex"         -> SpellEffect.projectile(
                                    buildProjectilePath(px, py, d, 15), new Color(190, 40, 255));
            case "Fulgur"      -> SpellEffect.line(
                                    buildLineTiles(px, py, d, 12), new Color(255, 235, 50));
            case "Inferno"     -> SpellEffect.area(
                                    buildAdjacentTiles(px, py), new Color(255, 70, 10));
            case "Sanatio"     -> SpellEffect.self(px, py, new Color(50, 215, 80));
            case "Umbra"       -> SpellEffect.self(px, py, new Color(110, 50, 210));
            case "Glacialis"   -> SpellEffect.area(
                                    buildVisibleEnemyTiles(), new Color(130, 205, 255));
            case "Lux Aeterna" -> SpellEffect.area(
                                    buildVisibleEnemyTiles(), new Color(255, 248, 160));
            case "Arcana Nova" -> SpellEffect.area(
                                    buildVisibleEnemyTiles(), new Color(175, 70, 255));
            default            -> null;
        };
    }

    /** Tiles from one step past the player in {@code dir}, stopping at the first
     *  impassable tile or the first living enemy (inclusive). */
    private int[][] buildProjectilePath(int px, int py, Direction dir, int range) {
        List<int[]> path = new ArrayList<>();
        for (int dist = 1; dist <= range; dist++) {
            int tx = px + dir.dx * dist;
            int ty = py + dir.dy * dist;
            if (!map.isPassable(tx, ty)) break;
            path.add(new int[]{tx, ty});
            for (Enemy e : enemies) {
                if (e.isAlive() && e.getX() == tx && e.getY() == ty)
                    return path.toArray(new int[0][]);
            }
        }
        return path.toArray(new int[0][]);
    }

    /** All passable tiles in a direction out to {@code range} (for Fulgur). */
    private int[][] buildLineTiles(int px, int py, Direction dir, int range) {
        List<int[]> tiles = new ArrayList<>();
        for (int dist = 1; dist <= range; dist++) {
            int tx = px + dir.dx * dist;
            int ty = py + dir.dy * dist;
            if (!map.isPassable(tx, ty)) break;
            tiles.add(new int[]{tx, ty});
        }
        return tiles.toArray(new int[0][]);
    }

    /** 3×3 area around the player (for Inferno). */
    private int[][] buildAdjacentTiles(int px, int py) {
        List<int[]> tiles = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++)
            for (int dy = -1; dy <= 1; dy++)
                if (dx != 0 || dy != 0) tiles.add(new int[]{px + dx, py + dy});
        return tiles.toArray(new int[0][]);
    }

    /** Positions of all visible living enemies (for AoE spells). */
    private int[][] buildVisibleEnemyTiles() {
        return enemies.stream()
            .filter(e -> e.isAlive()
                && map.hasLOS(player.getX(), player.getY(), e.getX(), e.getY()))
            .map(e -> new int[]{e.getX(), e.getY()})
            .toArray(int[][]::new);
    }

    // ------------------------------------------------------------------ read-only views

    public DungeonMap        getMap()            { return map; }
    public Player            getPlayer()         { return player; }
    public List<Enemy>       getEnemies()        { return enemies; }
    public List<Item>        getItems()          { return items; }
    public MessageLog        getLog()            { return log; }
    public InputBuffer       getInputBuffer()    { return inputBuffer; }
    public State             getState()          { return state; }
    public int               getFloor()          { return floorNumber; }
    public List<SpellEffect> getActiveEffects()  { return activeEffects; }
}
