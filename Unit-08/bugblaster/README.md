# Bug Blaster 3000 — Exterminator Edition

A 60 fps mouse-input game demonstrating every major mouse event type available in
Java Swing. You play as a beleaguered exterminator defending a family's kitchen
from an increasingly absurd invasion of insects. They are after Mom's pie.
They **will not** stop.

---

## Prerequisites

| Requirement | Version |
|-------------|---------|
| JDK         | 17+     |
| Maven       | 3.6+    |

---

## Quick Start

```bash
# Compile
mvn compile

# Play
mvn exec:java

# Run unit tests (11 tests)
mvn test

# Package as JAR
mvn package
java -jar target/bugblaster-1.0-SNAPSHOT.jar

# Generate Javadoc → target/site/apidocs/
mvn javadoc:javadoc
```

---

## Mouse Controls

| Action | Effect | Works Best On |
|--------|--------|---------------|
| **Left click** | Quick spray (radius 28 px, 1 dmg) | Ants, Flies |
| **Left double-click** | Boot stomp! (radius 70 px, 3 dmg) | Cockroaches |
| **Hold left ≥ 0.4 s, then release** | Charged blast — radius and damage scale with hold time (max 2 s = radius 105, 8 dmg) | Beetles |
| **Left drag** | Continuous spray trail (1 dmg per bug hit per segment) | Ant columns |
| **Right click** | Place sticky glue trap (up to 5 on screen) | Spiders, Beetles |
| **Right drag on a trap** | Reposition the trap | All |
| **Mouse movement** | Flies detect the cursor within 90 px and dodge away | (see below) |

### Keyboard
| Key | Action |
|-----|--------|
| `P` | Pause / resume |
| `R` | Restart (new game) |

---

## Bug Field Guide

### Ant `10 pts`
Marches in a straight line with military dedication. HP: 1.
One spray click kills it, one drag segment wipes out a whole column.
**"FOR THE COLONY! …oh no"**

### Cockroach `30 pts`
Changes direction randomly every 0.5–1 s. HP: 3.
Has survived nuclear blasts; a light spray tickles it.
Best killed with a **double-click stomp** while it momentarily stops.
**"Five hundred million years of evolution… foiled by a boot"**

### Fly `50 pts`
Fast, bouncy, and actively dodges within 90 px of the cursor. HP: 1.
The trick is to predict its escape path and click *ahead* of it,
not on it. Worth 50 pts because it absolutely deserves it.
**"BZZZZ—"**

### Spider `40 pts`
Slow, methodical, and pauses periodically to contemplate its web.
HP: 5. Right-click a trap in its path — it will walk into it and be dead
within ~2.5 s. Also has eight eyes and the spatial awareness of a raisin.
**"Do you know how long that web took?"**

### Beetle `60 pts`
A living tank. HP: 8. Moves so slowly it is practically stationary.
A single spray merely gives it a headache. **Hold the left button** until
the charge meter (HUD bottom-right) maxes out, then release for a
105 px blast that one-shots it.
**"I. Am. INDESTRUCTIBLE. Oh."**

---

## Scoring & Combos

Killing bugs in rapid succession (within 1.5 s of the previous kill) builds
a **combo multiplier** shown in the HUD. Combo caps at ×8.

| Multiplier | Points awarded |
|------------|---------------|
| ×1         | base value |
| ×2         | base × 2 |
| … | … |
| ×8 (max)   | base × 8 |

A bug reaching Mom's Pie resets the combo and costs a life.

---

## Wave Progression

| Wave | Theme | Bugs |
|------|-------|------|
| 1 | The Scout Party | 10 ants |
| 2 | The Great Cockroach Migration | 14 ants · 4 cockroaches |
| 3 | Operation Airborne Assault | 16 ants · 5 roaches · 5 flies |
| 4 | Eight-Legged Invasion | + 3 spiders |
| 5 | The Armoured Division | + 2 beetles |
| 6+ | There Is No Hope. Only Bugs. | Scales up every wave |

---

## Architecture

```
com.bugblaster
├── Main                       Entry point; sets L&F and launches GameFrame
├── core/
│   ├── Bug                    Abstract base: position, HP, movement, hit detection
│   ├── AntBug                 1 HP, straight march
│   ├── CockroachBug           3 HP, erratic direction changes
│   ├── FlyBug                 1 HP, fast, cursor-dodging (onCursorNear)
│   ├── SpiderBug              5 HP, slow, periodic pause
│   ├── BeetleBug              8 HP, very slow, armoured
│   ├── Trap                   Sticky glue trap; catches and damages bugs over time
│   ├── Effect                 Abstract timed visual effect
│   ├── SprayEffect            Single-click spray cloud (green mist)
│   ├── StompEffect            Double-click boot + shockwave ring
│   ├── ChargeBlastEffect      Hold-release expanding concentric rings
│   ├── DragTrailEffect        Drag-spray glowing line segment
│   ├── FloatingText           Humorous rising text (kill quotes, score, events)
│   ├── GameState              Score, lives, wave, combo — pure data, no Swing
│   ├── BugSpawner             Wave queue; releases bugs at configurable intervals
│   └── GameController         Owns all entities; processes mouse actions and ticks
└── ui/
    ├── GameFrame              JFrame: GamePanel (centre) + HUDPanel (south)
    ├── GamePanel              Rendering + all MouseListener/MouseMotionListener logic
    └── HUDPanel               Score, wave, lives, charge meter
```

### Key Design Principles

**Model / View separation** — no `javax.swing` import anywhere in `com.bugblaster.core`.
The controller exposes read-only list views; the panel calls controller methods for actions.

**Single-threaded via EDT** — `javax.swing.Timer` fires `update()` on the Event Dispatch
Thread; mouse listeners also run on the EDT. No synchronisation needed.

**Charge mechanic** — `mousePressed` records a timestamp. `mouseReleased` checks elapsed
time; if ≥400 ms and no drag occurred, a charged blast fires. Otherwise `mouseClicked`
handles single/double clicks normally. A `chargeConsumed` flag prevents double-firing.

**Fly dodging** — `Bug.onCursorNear(x, y)` is a no-op in the base class. `FlyBug` overrides
it to steer velocity away from the cursor proportionally to how close it is.

---

## Why This Project Exists

This project is a practical study in every mouse-input event category Java Swing exposes:

| Java event / state | Mechanic |
|--------------------|----------|
| `mouseClicked` (clickCount==1) | Quick spray |
| `mouseClicked` (clickCount==2) | Boot stomp |
| `mousePressed` + `mouseReleased` timing | Charged blast (hold & release) |
| `mouseDragged` (left button) | Spray trail |
| `mouseDragged` (right button) | Trap repositioning |
| `mouseClicked` (right button) | Place trap |
| `mouseMoved` | Cursor tracking for fly AI |

Each mechanic is deliberately matched to a bug type so that students must use *all*
of the above to progress through the later waves.
