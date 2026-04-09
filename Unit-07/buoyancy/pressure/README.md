# Buoyancy — Pressure Differential Simulator

An interactive Java/Swing simulation that teaches how buoyancy *emerges* from
pressure differences acting on a submerged cylinder's faces — and proves this
is identical to Archimedes' principle.

## The Core Insight

Archimedes' principle is not an independent law; it follows from Pascal's law:

```
P(d) = ρ_fluid × g × d          (pressure at depth d)

F_up   = P(d_bottom) × A_face   (upward push on bottom face)
F_down = P(d_top)    × A_face   (downward push on top face)
F_sides                          (cancel by symmetry → 0)

F_net_up = F_up − F_down
         = ρ_fluid × g × (d_bottom − d_top) × A
         = ρ_fluid × g × h_sub × A
         = ρ_fluid × g × V_sub    ← Archimedes' principle
```

The "Pressure Force Breakdown" panel shows both results side by side and
confirms they agree to numerical precision.

## Visualisation

### Pressure Heatmap

The fluid is coloured as a heat map where colour encodes absolute pressure:

| Colour | Pressure |
|---|---|
| Cyan | Surface (P ≈ 0) |
| Blue | Mid-depth |
| Indigo / Purple | Floor (maximum P) |

The legend on the right edge shows the colour-to-pressure mapping.

### Distributed Face Arrows

When **Face Arrows** are enabled (key **F** or button):

| Arrow | Colour | Face |
|---|---|---|
| Upward arrows, spaced evenly | Green | Bottom face (upward push) |
| Downward arrows, spaced evenly | Red | Top face (if submerged, downward push) |
| Lateral arrows | Yellow | Side faces (shown to illustrate cancellation) |

Arrow length scales with local pressure so deeper objects show longer arrows.

## Interaction

| Action | Effect |
|---|---|
| Click object | Select it |
| Drag object | Move freely through the fluid |
| Right-click object | Delete it |
| Drag surface line | Raise / lower fluid level |
| **P** | Toggle live physics on / off |
| **A** | Add a new object |
| **G** | Toggle pressure grid overlay |
| **F** | Toggle distributed face arrows |
| **R** | Reset velocity |
| **Del** | Remove selected object |

## Controls (right panel)

| Control | Range | Description |
|---|---|---|
| Gravity | 1–25 m/s² | Gravitational acceleration |
| Fluid density | 100–14 000 kg/m³ | Medium density |
| Surface level | 80–600 px | Height of fluid surface |
| Object density | 50–14 000 kg/m³ | Density of selected object |
| Radius | 0.05–0.60 m | Cylinder radius |
| Height | 0.05–1.00 m | Cylinder height |
| Cell size | 8–60 px | Grid / arrow spacing |
| Gravity presets | buttons | Earth / Moon / Jupiter |
| Material presets | buttons | Cork / Pine / Ice / Concrete / Steel |
| Fluid presets | buttons | Fresh water / Sea water / Oil / Mercury |

## Pressure Force Breakdown Panel

The educational heart of this simulator.  Updated every frame for the selected
object:

```
── Bottom face ──────────────────────
  Depth       d_bot =  0.45 m
  Pressure    P_bot = 4 413 Pa
  Area        A     = 0.0707 m²
  Force ↑     F_up  = 312.0 N

── Top face ─────────────────────────
  Depth       d_top =  0.15 m
  Pressure    P_top = 1 471 Pa
  Force ↓     F_dn  = 104.0 N

── Side faces ───────────────────────
  Cancel by symmetry → 0

── Net buoyancy (Σ face forces) ─────
  F_net_faces = 208.0 N

── Archimedes (ρgV) ─────────────────
  F_archimedes = 208.0 N

  Match: ✓ Agree

── Object forces ─────────────────────
  F_gravity = 250.0 N
  F_net     = 42.0 N ↓  (sinks)
  Submerged = 100%
  Status: SINKING
```

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
java -jar target/buoyancy-pressure-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

## Project Structure

```
pressure/
├── pom.xml
└── src/main/java/com/buoyancy/pressure/
    ├── Main.java
    ├── model/
    │   ├── FluidMedium.java      # static fluid with pressure field
    │   └── PressureBody.java     # cylinder with top/bottom face areas
    ├── physics/
    │   └── PressurePhysics.java  # face-by-face + Archimedes + Euler step
    └── ui/
        ├── MainFrame.java        # top-level JFrame
        ├── PressurePanel.java    # canvas: heatmap, arrows, animation loop
        └── ControlPanel.java     # sliders, presets, force breakdown panel
```
