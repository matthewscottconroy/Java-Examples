package com.wizardrogue.core;

/**
 * A single collectable item lying on a dungeon floor tile.
 *
 * <p>Items are picked up by walking onto them and pressing Space, or
 * automatically for consumables ({@link ItemType#autoUse}).  Equipment items
 * are automatically equipped if they improve on the currently-held slot.
 *
 * <p>The {@code value} field provides the magnitude of the item's effect —
 * for potions it is the restoration amount; for equipment it is the stat bonus.
 */
public final class Item {

    private final ItemType type;
    private int x;
    private int y;
    private final int value;
    private boolean pickedUp;

    /** Variant field — for scrolls this holds the index into {@link SpellBook}. */
    private final int variant;

    public Item(ItemType type, int x, int y, int value, int variant) {
        this.type     = type;
        this.x        = x;
        this.y        = y;
        this.value    = value;
        this.variant  = variant;
        this.pickedUp = false;
    }

    public Item(ItemType type, int x, int y, int value) {
        this(type, x, y, value, 0);
    }

    // ------------------------------------------------------------------ accessors

    public ItemType getType()     { return type; }
    public int      getX()        { return x; }
    public int      getY()        { return y; }
    public int      getValue()    { return value; }
    public int      getVariant()  { return variant; }
    public boolean  isPickedUp()  { return pickedUp; }
    public void     setPickedUp() { pickedUp = true; }
    public char     getGlyph()    { return type.glyph; }

    /**
     * Applies this item's effect to {@code player} and logs a message.
     * Also calls {@link #setPickedUp()} so the controller removes it from the map.
     */
    public void applyTo(Player player, MessageLog log) {
        setPickedUp();
        switch (type) {
            case HEALTH_POTION -> {
                int heal = Math.min(value, player.getMaxHp() - player.getHp());
                player.heal(heal);
                log.add("You drink a Health Potion. +" + heal + " HP.");
            }
            case MANA_POTION -> {
                int restore = Math.min(value, player.getMaxMp() - player.getMp());
                player.restoreMp(restore);
                log.add("You drink a Mana Potion. +" + restore + " MP.");
            }
            case STAFF -> {
                if (player.getWeaponBonus() < value) {
                    player.setWeaponBonus(value);
                    player.setWeaponName("Staff +" + value);
                    log.add("You equip the Magic Staff. ATK +" + value + ".");
                } else {
                    log.add("You find a Magic Staff, but yours is better.");
                }
            }
            case ROBE -> {
                if (player.getArmorBonus() < value) {
                    player.setArmorBonus(value);
                    player.setArmorName("Robe +" + value);
                    log.add("You equip the Wizard Robe. DEF +" + value + ".");
                } else {
                    log.add("You find a Wizard Robe, but yours is better.");
                }
            }
            case RING -> {
                player.addMpBonus(value);
                log.add("You equip a Magic Ring. Max MP +" + value + ".");
            }
            case TOME -> {
                player.addHpBonus(value);
                log.add("You read an Ancient Tome. Max HP +" + value + ".");
            }
            case GOLD -> {
                player.addGold(value);
                log.add("You pocket " + value + " gold coins.");
            }
            case SPELL_SCROLL -> {
                // variant holds spell index — handled by GameController with SpellBook access
                log.add("You find a Spell Scroll!");
            }
        }
    }
}
