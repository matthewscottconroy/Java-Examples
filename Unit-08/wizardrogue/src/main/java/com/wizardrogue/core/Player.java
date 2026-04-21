package com.wizardrogue.core;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * The player-controlled wizard.
 *
 * <p>The player is the only entity whose movement is driven directly by
 * keyboard input (via {@link com.wizardrogue.ui.DungeonPanel}).  All
 * other aspects — HP, MP, level, spells, equipment — live here so the
 * controller and UI can read them without coupling to the input system.
 *
 * <h2>Levelling</h2>
 * <p>XP thresholds grow by 50 per level.  On level-up: base ATK/DEF increase,
 * max HP/MP increase, and new spells are automatically unlocked.
 *
 * <h2>Facing direction</h2>
 * <p>The player's {@link Direction} is updated whenever they move and is used
 * as the default targeting axis for directional spells (Ignis, Glacies, etc.).
 */
public final class Player extends Entity {

    // ------------------------------------------------------------------ XP table

    private static final int BASE_XP_THRESHOLD = 50;

    // ------------------------------------------------------------------ stats

    private int mp;
    private int maxMp;
    private int level;
    private int xp;
    private int xpToNext;
    private int baseAtk;
    private int baseDef;
    private int gold;
    private int floorNumber;

    private int weaponBonus;
    private int armorBonus;
    private String weaponName;
    private String armorName;

    private Direction facing;

    private final List<Spell> knownSpells = new ArrayList<>();

    // ------------------------------------------------------------------ slow effects

    /** Ticks remaining on a "frozen" status (movement disabled). */
    private int frozenTicks;

    public Player(int x, int y) {
        super(x, y, 30, '@', new Color(50, 220, 255), "Wizard");
        this.mp         = 20;
        this.maxMp      = 20;
        this.level      = 1;
        this.xp         = 0;
        this.xpToNext   = BASE_XP_THRESHOLD;
        this.baseAtk    = 4;
        this.baseDef    = 1;
        this.gold       = 0;
        this.floorNumber = 1;
        this.weaponBonus = 0;
        this.armorBonus  = 0;
        this.weaponName  = "Bare hands";
        this.armorName   = "Apprentice Robe";
        this.facing      = Direction.EAST;
    }

    // ------------------------------------------------------------------ movement

    /**
     * Attempts to move in {@code dir}, returning {@code true} on success.
     * Movement fails if the target tile is impassable or occupied by an enemy.
     * Facing is updated regardless of success so directional spells feel natural.
     */
    public boolean tryMove(Direction dir, DungeonMap map, List<Enemy> enemies) {
        facing = dir;
        if (frozenTicks > 0) {
            frozenTicks--;
            return false;
        }
        int nx = x + dir.dx, ny = y + dir.dy;
        if (!map.isPassable(nx, ny)) return false;
        for (Enemy e : enemies) {
            if (e.isAlive() && e.getX() == nx && e.getY() == ny) return false;
        }
        x = nx;
        y = ny;
        return true;
    }

    // ------------------------------------------------------------------ combat

    /** Total attack power including weapon bonus. */
    public int getTotalAtk() { return baseAtk + weaponBonus; }

    /** Total defence including armour bonus. */
    public int getTotalDef() { return baseDef + armorBonus; }

    // ------------------------------------------------------------------ XP / levelling

    /**
     * Adds {@code amount} XP and levels up as many times as warranted,
     * returning the number of levels gained.
     */
    public int gainXp(int amount, MessageLog log) {
        xp += amount;
        int levelsGained = 0;
        while (xp >= xpToNext) {
            xp       -= xpToNext;
            xpToNext += BASE_XP_THRESHOLD / 2;
            level++;
            levelsGained++;
            // Stat improvements on level-up
            maxHp  += 8;
            hp      = maxHp;
            maxMp  += 5;
            mp      = maxMp;
            baseAtk += 2;
            baseDef += 1;
            log.add("*** LEVEL UP! You are now level " + level + ". HP/MP restored. ***");
        }
        return levelsGained;
    }

    // ------------------------------------------------------------------ MP

    public void spendMp(int amount)    { mp = Math.max(0, mp - amount); }
    public void restoreMp(int amount)  { mp = Math.min(maxMp, mp + amount); }
    public void addMpBonus(int bonus)  { maxMp += bonus; mp += bonus; }
    public void addHpBonus(int bonus)  { maxHp += bonus; heal(bonus); }

    // ------------------------------------------------------------------ spells

    /** Adds a spell to the known list if not already present. */
    public void learnSpell(Spell spell) {
        boolean known = knownSpells.stream()
            .anyMatch(s -> s.getName().equals(spell.getName()));
        if (!known) knownSpells.add(spell);
    }

    public List<Spell> getKnownSpells() { return knownSpells; }

    // ------------------------------------------------------------------ slow / freeze

    public void applyFreeze(int ticks) { frozenTicks = Math.max(frozenTicks, ticks); }
    public boolean isFrozen()          { return frozenTicks > 0; }

    // ------------------------------------------------------------------ accessors

    public int       getMp()         { return mp; }
    public int       getMaxMp()      { return maxMp; }
    public int       getLevel()      { return level; }
    public int       getXp()         { return xp; }
    public int       getXpToNext()   { return xpToNext; }
    public int       getBaseAtk()    { return baseAtk; }
    public int       getBaseDef()    { return baseDef; }
    public int       getGold()       { return gold; }
    public int       getFloor()      { return floorNumber; }
    public int       getWeaponBonus(){ return weaponBonus; }
    public int       getArmorBonus() { return armorBonus; }
    public String    getWeaponName() { return weaponName; }
    public String    getArmorName()  { return armorName; }
    public Direction getFacing()     { return facing; }

    public void setFacing(Direction dir)       { facing = dir; }
    public void setFloor(int f)              { floorNumber = f; }
    public void addGold(int g)               { gold += g; }
    public void setWeaponBonus(int b)        { weaponBonus = b; }
    public void setArmorBonus(int b)         { armorBonus = b; }
    public void setWeaponName(String n)      { weaponName = n; }
    public void setArmorName(String n)       { armorName = n; }
}
