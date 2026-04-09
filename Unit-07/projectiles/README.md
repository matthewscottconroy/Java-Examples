# Slingshot — Destructible Terrain

A 2D Java Swing game inspired by *Angry Birds* and classic pixel-destruction
games (*Lemmings*, *Worms*, *Donkey Kong*). Launch a stone from a slingshot,
watch it arc through the sky, and blow craters out of procedurally-generated
terrain while pixel debris rains upward.

## Gameplay

| Action | How |
|--------|-----|
| Aim | Click and drag from the slingshot fork to set direction and power |
| Launch | Release the mouse button |
| New stone | Reload automatically after the explosion settles |

The trajectory preview (dashed line) updates live as you drag, so you can
see exactly where the stone will land before you release.

## Physics & Rendering

### Projectile

- **Euler integration** each frame: `vy += gravity * dt; x += vx * dt; y += vy * dt`
- **9-point collision probe**: centre + 8 circumference points checked against
  the terrain's pixel-alpha map
- **Fade trail**: last 60 positions drawn with increasing opacity

### Terrain

- **Procedurally generated** skyline — three octaves of sine noise, clamped
  to keep terrain on-screen
- **Pixel-exact destruction**: an explosion carves a filled circle and sets
  every pixel inside to transparent (air)
- **Scorched rim**: a 4-pixel annular halo is darkened around the blast
- **Layer colouring** (depth below surface):
  - 0–6 px — grass (greens)
  - 7–50 px — dirt (browns)
  - 51+ px — rock (greys)

### Debris fountain

When terrain is destroyed, pixel colours are harvested and used to tint
160 debris particles:

- **65 % fountain** — launched upward ±50° at 80–400 px/s (Lemmings-style geyser)
- **35 % radial** — scattered outward in all directions at 40–240 px/s
- Each particle bounces once off solid terrain (35 % velocity retention) then
  fades out over 0.6–1.8 s

## Build & Run

### Prerequisites

| Tool | Minimum version | Check with |
|------|----------------|------------|
| JDK  | 17             | `java -version` |
| Maven| 3.6            | `mvn -version` |

### Commands

```bash
# Compile
mvn compile

# Run
mvn exec:java

# Run the unit test suite
mvn test

# Package a self-contained JAR
mvn package
java -jar target/projectiles-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

### Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| Black window / no terrain | Display not initialised yet | Wait one second; Swing initialises on the EDT |
| Terrain not regenerating | Game reached GAME_OVER state | Press **R** or click **New Game** in the toolbar |
| `HeadlessException` | No display available | Run inside a desktop session; set `DISPLAY=:0` on Linux |
| Tests fail to compile | Missing JDK / wrong version | Verify `java -version` ≥ 17 |

## Project Structure

```
src/main/java/com/projectiles/
├── Main.java          Entry point — creates GameFrame
├── GameFrame.java     Top-level JFrame; hosts GamePanel
├── GamePanel.java     Main game loop (javax.swing.Timer), state machine,
│                      input handling, rendering orchestration
├── Terrain.java       BufferedImage-backed destructible landscape
├── Projectile.java    Physics body + 9-point collision probe + trail
└── Debris.java        Explosion particle — fountain/radial spawn, bounce, fade
```

### Key constants in `GamePanel`

| Constant | Value | Effect |
|----------|-------|--------|
| `GRAVITY` | 680 px/s² | Arc steepness |
| `LAUNCH_POWER` | 10.0× | Scales drag distance → initial speed |
| `EXPLODE_RADIUS` | 55 px | Crater size |
| `DEBRIS_COUNT` | 160 | Particles per explosion |

## References

- Euler method (numerical integration): any undergraduate numerical methods text
- Watts-Strogatz (1998) — see the companion `smallworld` project for network theory
