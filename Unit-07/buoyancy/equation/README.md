# Buoyancy — Equation Simulator

An interactive Java/Swing simulation that teaches Archimedes' principle through
real-time physics and drag-and-drop experimentation.

## The Physics

The buoyant force is computed directly from Archimedes' principle:

```
F_b = ρ_fluid × g × V_submerged
```

where `V_submerged = π r² h_sub` for an upright cylinder.  The net force
determines whether the object sinks, floats, or rises:

```
F_net = F_gravity − F_buoyancy = m·g − ρ_f·g·V_sub
```

Dynamics use Euler integration with viscous drag (`k = 2.5 s⁻¹`):

```
a  = F_net / m − k·vy
vy += a·dt
y  += vy·PPM·dt        (PPM = 100 px/m)
```

## Interaction

| Action | Effect |
|---|---|
| Click object | Select it |
| Drag object | Move freely; physics resumes on release |
| Right-click object | Delete it |
| Drag surface line | Raise / lower fluid level |
| **P** | Pause / resume physics |
| **A** | Add a new object at a random position |
| **R** | Reset velocity (re-drop all objects) |
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
| Preset | combo | Common materials |
| Fluid presets | buttons | Fresh water / Sea water / Oil / Mercury |

## Live Metrics

The panel updates every frame showing:

- Buoyant force F_b (blue)
- Gravitational force F_g (red)
- Net force F_net (green = rising, red = sinking)
- Velocity, submersion %, mass, volume
- Density ratio ρ_obj / ρ_fluid
- Status: FLOATING / SINKING / RISING / SETTLING / RESTING ON FLOOR

## Force Arrows (on canvas)

| Arrow | Colour | Meaning |
|---|---|---|
| Left of centre ↑ | Green | Buoyant force |
| Right of centre ↓ | Red | Gravitational force |
| Centre | Yellow | Net force (direction = resultant) |

Arrow lengths scale as √F so large forces remain visible without dominating.

## Material Presets

| Preset | ρ (kg/m³) | Behaviour in water |
|---|---|---|
| Balsa wood | 170 | Floats high |
| Cork | 240 | Floats |
| Pine | 530 | Floats ~53% submerged |
| Oak | 700 | Floats ~70% submerged |
| Ice | 917 | Floats ~92% submerged |
| Water | 1 000 | Neutral (sinks very slowly) |
| Sea water | 1 025 | Sinks in fresh water, floats in sea water |
| Concrete | 2 400 | Sinks |
| Aluminium | 2 700 | Sinks |
| Steel | 7 850 | Sinks quickly |
| Lead | 11 340 | Sinks very quickly |
| Mercury | 13 534 | Sinks in water, floats in mercury |

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
java -jar target/buoyancy-equation-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

## Project Structure

```
equation/
├── pom.xml
└── src/main/java/com/buoyancy/equation/
    ├── Main.java
    ├── model/
    │   ├── BuoyancyObject.java   # cylinder with position, density, dynamics
    │   ├── Fluid.java            # fluid medium with wave animation
    │   └── ObjectPreset.java     # material density enum
    ├── physics/
    │   └── BuoyancyPhysics.java  # Archimedes + Euler integration
    └── ui/
        ├── MainFrame.java        # top-level JFrame
        ├── SimulationPanel.java  # canvas, animation loop, mouse/key input
        └── ControlPanel.java     # sliders, presets, live metrics
```
