# Wizard Rogue

A real-time ASCII roguelike that demonstrates every major category of keyboard
input processing in Java Swing.  You play a wizard exploring procedurally
generated dungeons, casting spells via fighting-game-style key sequences, and
fighting escalating hordes across five floors.

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK         | 17+     |
| Maven       | 3.6+    |

---

## Quick Start

```bash
mvn compile       # compile
mvn exec:java     # play
mvn test          # 24 unit tests
mvn javadoc:javadoc   # docs → target/site/apidocs/
```

---

## Controls

| Key | Action |
|-----|--------|
| **W A S D** | Move (hold for continuous real-time movement) |
| **Arrow keys** | Rotate facing direction without moving |
| **Q E R** | Spell keys — type sequences to cast spells |
| **Space** | Pick up item on your tile |
| **,** (comma) | Descend stairs (`>` tile) |
| **Esc** | Pause / resume |
| **N** | New game (from Game Over or Victory screen) |

---

## The Three Keyboard-Input Techniques

This project illustrates three distinct approaches to handling keyboard input
in a real-time game.  All three are implemented in `DungeonPanel.java`.

### 1. Key-State Tracking (Movement)

```java
private final Set<Integer> heldKeys = new HashSet<>();

@Override
public void keyPressed(KeyEvent e)  { heldKeys.add(e.getKeyCode()); }
@Override
public void keyReleased(KeyEvent e) { heldKeys.remove(e.getKeyCode()); }

// In the 100 ms game loop:
for (Direction dir : Direction.values()) {
    if (heldKeys.contains(dir.vk)) { controller.playerMove(dir); break; }
}
```

A `Set<Integer>` records every key that is *currently* held.  The game timer
checks the set each tick, producing smooth movement at up to 10 steps/second
regardless of the OS key-repeat rate or the order keys were pressed.

### 2. Sequence Detection with a Timing Window (Spell Casting)

```java
// On Q / E / R keypress:
controller.handleSpellKey(vk);   // → InputBuffer.push(vk)

// InputBuffer stores timestamped presses and checks:
//   1. Do the last N presses match the spell's pattern?
//   2. Did they all occur within the 2.5-second combo window?
```

`InputBuffer` is the centrepiece of the project.  Each spell key is stored with
a `System.currentTimeMillis()` timestamp.  After every push the controller
scans the buffer tail against every known spell's sequence.  Old entries expire
automatically — no "spell mode" to enter or exit.  The stats panel shows the
live buffer state so the player can see exactly what has been typed.

### 3. Immediate Single-Key Actions

```java
case KeyEvent.VK_SPACE  -> controller.tryPickup();
case KeyEvent.VK_COMMA  -> controller.tryDescend();
case KeyEvent.VK_ESCAPE -> controller.togglePause();
```

Some actions need no state tracking and no history — a single keypress fires
them exactly once.  Handled directly in `keyPressed` with a plain `switch`.

---

## Spell Compendium

Spells are cast by typing their Q / E / R sequence within the 2.5-second window.
The stats panel highlights spells whose sequence starts with what you've already typed.

| Spell | Sequence | MP | Lvl | Effect |
|-------|----------|----|-----|--------|
| **Ignis** | Q Q | 5 | 1 | Fire dart — 20+ dmg, range 8, facing direction |
| **Glacies** | E E | 5 | 1 | Ice shard — 12+ dmg + slow, range 6 |
| **Fulgur** | Q E Q | 10 | 2 | Lightning — 20+ dmg to all enemies in entire column |
| **Sanatio** | R R | 8 | 2 | Heal self — 20 + level×3 HP |
| **Umbra** | Q Q E E | 12 | 3 | Shadow step — teleport 5 tiles forward |
| **Inferno** | E Q E Q | 15 | 3 | Ring of fire — 18+ dmg to all adjacent enemies |
| **Glacialis** | E E Q E E | 20 | 4 | Blizzard — freeze + 10 dmg to ALL visible enemies |
| **Nex** | Q E R | 18 | 4 | Death bolt — 45+ dmg single target, range 15 |
| **Lux Aeterna** | R R R | 25 | 5 | Radiant burst — full heal + 30 dmg all visible |
| **Arcana Nova** | Q E R Q E R | 30 | 5 | Ultimate — 50+ dmg to every visible enemy |

**Strategy tips:**
- Q Q and E E are your bread-and-butter; learn their muscle memory first.
- Q E R (Nex) uses only three keys but note it is a *suffix* of Q E R Q E R (Arcana Nova) — type quickly for Nex, or keep going for the ultimate.
- Glacialis (E E Q E E) freezes everything on screen — great panic button.
- Umbra (Q Q E E) escapes dangerous rooms instantly; face the direction first.

---

## Bestiary

| Symbol | Name | HP | ATK | DEF | Speed | XP | Notes |
|--------|------|----|-----|-----|-------|----|-------|
| `g` | Goblin | 8 | 3 | 1 | Fast | 12 | Wanders until alerted |
| `O` | Orc | 22 | 7 | 3 | Medium | 30 | Pursues relentlessly |
| `T` | Troll | 45 | 10 | 6 | Slow | 55 | High HP; use Nex |
| `D` | Demon | 18 | 14 | 2 | Fast | 70 | Fragile but hits hard |
| `L` | Lich | 60 | 18 | 8 | Medium | 120 | Fires arcane bolts at range |

---

## Level Progression

| Level | New Spells Unlocked |
|-------|---------------------|
| 1 | Ignis, Glacies |
| 2 | Fulgur, Sanatio |
| 3 | Umbra, Inferno |
| 4 | Glacialis, Nex |
| 5 | Lux Aeterna, Arcana Nova |

Spell Scrolls found in the dungeon teach a random spell regardless of level.

---

## Architecture

```
com.wizardrogue
├── Main                     Entry point
├── core/
│   ├── Direction            Enum: NORTH/SOUTH/EAST/WEST with dx, dy, VK code
│   ├── Tile                 Enum: terrain types with glyph, colour, passability
│   ├── DungeonMap           Procedural BSP-style room-and-corridor generator;
│   │                        Bresenham LOS for fog-of-war visibility
│   ├── Entity               Abstract base: position, HP, glyph, colour
│   ├── Player               Wizard stats, movement, XP/level, spell list
│   ├── Enemy                Monster AI: wander → pursue → melee/ranged attack
│   ├── EnemyType            Enum: per-species stats + floor appearance table
│   ├── Item                 Collectable with effect applied on pickup
│   ├── ItemType             Enum: consumables vs equipment
│   ├── Spell                Sequence + MP cost + lambda effect
│   ├── SpellBook            Factory for all 10 spells; level-unlock logic
│   ├── InputBuffer          ★ Timestamped combo-window sequence detector
│   ├── MessageLog           Scrolling message queue (last 5 shown)
│   └── GameController       Owns all entities; drives tick/move/cast/pickup
└── ui/
    ├── GameFrame            JFrame layout
    ├── DungeonPanel         ★ All 3 keyboard techniques; ASCII rendering
    └── StatsPanel           Character sheet; live input-buffer display
```

### Key Design Decisions

**`InputBuffer` is timing-aware, not event-count-aware.**
Sequence matching requires both correct key order *and* that all presses fall
within `WINDOW_MS` = 2500 ms of each other.  This is implemented by storing
`System.currentTimeMillis()` alongside each key code in a `Deque<TimedKey>`.
Stale entries are pruned lazily on each push.

**Movement is decoupled from spell input.**
WASD feeds the `heldKeys` set; Q/E/R feed the `InputBuffer`.  The two systems
never share state, so a player can hold W (walking) and type Q E Q (Fulgur)
simultaneously without either interfering with the other.

**Enemy AI uses greedy obstacle-aware steering, not full pathfinding.**
Each enemy tries its preferred direction, then 90° turns, then retreat.  This
produces natural dungeon-following behaviour without a BFS on every tick.

**Fog of war uses Bresenham LOS from the player.**
`DungeonMap.computeVisibility()` casts a ray to every tile within radius 9.  If
any wall lies between the player and the tile the tile is not visible (but may
remain explored/dimmed from a previous visit).
