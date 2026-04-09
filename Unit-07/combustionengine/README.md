# Combustion Engine — Otto-Cycle Simulator

An interactive simulation of a 4-stroke internal combustion engine. Animated cross-sections show pistons, connecting rods, and the crankshaft in real time, while a live P-V diagram traces the thermodynamic cycle as the engine runs.

---

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
java -jar target/combustionengine-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

---

## How It Works

### The 4-Stroke Otto Cycle

Each cylinder completes four strokes for every two crankshaft revolutions (720°):

| Stroke      | Crank angle | Description |
|-------------|-------------|-------------|
| Intake      | 0° – 180°   | Piston descends; intake valve open; fresh air-fuel charge drawn in |
| Compression | 180° – 360° | Both valves closed; piston ascends; charge compressed adiabatically |
| Power       | 360° – 540° | Spark fires at TDC; combustion drives piston down |
| Exhaust     | 540° – 720° | Exhaust valve open; piston ascends; burned gases expelled |

### Slider-Crank Kinematics

The piston displacement from TDC is computed from the standard slider-crank equation:

```
x(φ) = r · (1 − cos φ) + l − √(l² − r²·sin²φ)
```

where `r` is the crank throw (half-stroke), `l` is the connecting-rod length, and `φ` is the crank angle (0 at TDC).

### Thermodynamics

The simulation uses an idealised **Otto cycle** with instantaneous heat release at TDC:

**Compression (isentropic):**
```
P · V^γ = P_atm · V_max^γ      γ = 1.35
```

**Peak pressure after combustion:**
```
Q     = η_c · m_fuel · LHV
ΔT    = Q / (m_mix · Cv)
T_peak = T_comp + ΔT
P_peak = P_comp · T_peak / T_comp
```

**Expansion (isentropic):**
```
P(V) = P_peak · (V_min / V)^γ
```

**Thermal efficiency:**
```
η_th = 1 − r_c^(1 − γ)
```

Higher compression ratios improve efficiency but require higher-octane fuel to avoid knock.

### Crankshaft Dynamics

The crankshaft is modelled as a flywheel with rotational inertia `I`:

```
I · dω/dt = T_combustion − K_load · ω − K_fric · ω
```

Load and friction torques are proportional to angular velocity, creating a natural RPM limit where the engine reaches equilibrium.

### Torque Generation

```
β  = arcsin(r · sin φ / l)
T  = (P − P_atm) · A · r · sin(φ + β) / cos β
```

Torque is zero at TDC and BDC (where the crank and rod are collinear) and peaks near 70° into the power stroke.

### Multi-Cylinder Phasing

For a 4-cylinder engine the firing order is **1-3-4-2**, with power strokes spaced 180° apart so that at least one cylinder is always delivering torque:

```
cycleOffset_i = firingOrderPhase(i) × (720° / numCylinders)
```

---

## Interaction

| Control | Action |
|---------|--------|
| **Start** button / `Space` | Starter motor — kicks crankshaft to ~200 RPM |
| **Throttle** slider | Controls fuel injection (0 = idle/off, 100% = full) |
| **Preset** combo | Switch between engine configurations |
| **Pause** / `P` | Freeze the simulation |
| **Reset** / `R` | Cold-restart with current preset |

---

## Engine Presets

| Preset | Bore × Stroke | Displacement | Comp. ratio |
|--------|--------------|--------------|-------------|
| Economy Inline-4 | 78 × 85 mm | ~1.63 L | 9.5:1 |
| Sport Inline-4   | 86 × 86 mm | ~2.00 L | 12.5:1 |
| Parallel Twin    | 72 × 80 mm | ~652 cc | 11.0:1 |
| Single Cylinder  | 76 × 86 mm | ~390 cc |  8.5:1 |

---

## P-V Diagram

The bottom panel shows the **pressure-volume diagram** for cylinder 1:

- **Blue curve** — adiabatic compression
- **Red vertical line** — isochoric heat addition at TDC
- **Yellow curve** — adiabatic expansion (power stroke)
- **Grey vertical line** — isochoric heat rejection at BDC
- **Live dot** — current state of cylinder 1, colour-coded by stroke phase

The area enclosed by the Otto cycle curves is proportional to the **net work** done per cycle.
