# Glass Jar — Bouncing Balls Simulator

An interactive Java/Swing physics sandbox where the program window *is* the jar.
Drag the window around the screen and watch the balls slosh, collide, and settle
under simulated gravity.

## The Physics

Each ball is a 2-D disc.  Mass uses a 2-D analogue of volume:

```
mass = density × π × radius²
```

Every simulation tick (60 fps) the engine:

1. **Applies gravity** — adds `g × dt` to each ball's `vy`.
2. **Applies window inertia** — when the jar moves, each ball receives a kick
   equal to `−windowVelocity × INERTIA_FACTOR` (0.65).  A jar moving right
   pushes the balls left relative to the jar.
3. **Integrates positions** — Euler step: `x += vx·dt`, `y += vy·dt`.
4. **Resolves wall collisions** — reflect and damp (restitution = 0.70);
   floor friction decays `vx` by 2% each frame the ball rests on the bottom.
5. **Resolves ball–ball collisions** — 5 impulse-resolution iterations per
   frame for stable stacking.  Impulse is mass-weighted so heavy balls deflect
   light ones much more than vice versa.

## Interaction

| Action | Effect |
|---|---|
| **Click** empty jar space | Add a ball at that point |
| **Drag** a ball | Pick up and throw it |
| **Drag** the window title-bar | Slosh all balls via inertia |

## Controls

| Control | Description |
|---|---|
| **Gravity slider** (0 – 2 000 px/s²) | Strength of downward acceleration |
| **Moon** button | g = 162 px/s² |
| **Earth** button | g = 800 px/s² (default) |
| **Jupiter** button | g = 2 000 px/s² |
| **S / M / L** | Radius of next ball (Small / Medium / Large) |
| **Light / Med / Heavy** | Density of next ball |
| **Add Ball** | Drop one from the top-centre of the jar |
| **Clear** | Remove all balls |

Ball density affects mass directly: a **Heavy** ball has 3.5× the density of a
**Light** ball.  At the same radius, a Heavy ball weighs ~11.7× more than a
Light ball and is much harder to deflect in a collision.

## Build, Run, Test & Docs

**Prerequisites**

| Tool | Minimum version | Check with |
|------|----------------|------------|
| JDK  | 17             | `java -version` |
| Maven | 3.6           | `mvn -version` |

```bash
# Compile
mvn compile

# Run
mvn exec:java

# Run the unit test suite
mvn test

# Package a self-contained JAR
mvn package
java -jar target/bouncingballs-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

## Project Structure

```
bouncingballs/
├── pom.xml
└── src/
    ├── main/java/com/bouncingballs/
    │   ├── Main.java                   # entry point
    │   ├── model/
    │   │   └── Ball.java               # position, velocity, mass, hit-testing
    │   ├── physics/
    │   │   └── PhysicsEngine.java      # gravity, wall/ball collisions, inertia
    │   └── ui/
    │       ├── MainFrame.java          # top-level JFrame (the "jar")
    │       ├── BallPanel.java          # canvas, animation loop, mouse input
    │       └── ControlPanel.java       # gravity slider, size/density controls
    └── test/java/com/bouncingballs/
        ├── BallTest.java               # Ball construction, mass, hit-testing
        └── PhysicsEngineTest.java      # gravity, walls, collisions, inertia
```
