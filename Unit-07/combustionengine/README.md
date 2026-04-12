# Combustion Engine Simulator

An interactive simulation of a 4-stroke internal combustion engine supporting both **Otto (spark-ignition)** and **Diesel (compression-ignition)** cycles.  Animated cross-sections show pistons, connecting rods, and the crankshaft in real time, while a live P-V diagram traces the thermodynamic cycle as the engine runs.

> **New to combustion engines?**  Read [INTRO.md](INTRO.md) for a plain-language explanation of how engines work, what the P-V diagram means, and how diesel engines differ from petrol engines.

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

### The 4-Stroke Cycle

Each cylinder completes four strokes for every two crankshaft revolutions (720°):

| Stroke      | Crank angle | Description |
|-------------|-------------|-------------|
| Intake      | 0° – 180°   | Piston descends; intake valve open; fresh charge drawn in |
| Compression | 180° – 360° | Both valves closed; piston ascends; charge compressed adiabatically |
| Power       | 360° – 540° | Ignition at TDC; expanding gases drive piston down |
| Exhaust     | 540° – 720° | Exhaust valve open; piston ascends; burned gases expelled |

### Slider-Crank Kinematics

The piston displacement from TDC is computed from the standard slider-crank equation:

```
x(φ) = r · (1 − cos φ) + l − √(l² − r²·sin²φ)
```

where `r` is the crank throw (half-stroke), `l` is the connecting-rod length, and `φ` is the crank angle (0 at TDC).

### Thermodynamics

The simulation supports two idealised cycles:

**Otto cycle** (spark-ignition, petrol engines):

```
Compression:   P · V^γ = const          (isentropic)
Combustion:    ΔT = Q / (m_mix · Cv)    (isochoric — constant volume at TDC)
Expansion:     P(V) = P_peak · (Vc / V)^γ
Efficiency:    η = 1 − r_c^(1 − γ)
```

**Diesel cycle** (compression-ignition, diesel engines):

```
Compression:   P · V^γ = const          (isentropic, higher r_c than Otto)
Combustion:    ΔT = Q / (m_mix · Cp)    (isobaric — constant pressure)
Cutoff ratio:  r_co = T_peak / T_comp   (when fuel injection ends)
Expansion:     P(V) = P_comp · (V_co / V)^γ
Efficiency:    η = 1 − (r_co^γ − 1) / (γ · (r_co − 1) · r_c^(γ−1))
```

See [INTRO.md](INTRO.md) for a detailed explanation of both cycles and how they compare.

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

| Preset | Cycle | Bore × Stroke | Displacement | Comp. ratio |
|--------|-------|--------------|--------------|-------------|
| Economy Inline-4 | Otto   | 78 × 85 mm  | ~1.63 L | 9.5:1  |
| Sport Inline-4   | Otto   | 86 × 86 mm  | ~2.00 L | 12.5:1 |
| Parallel Twin    | Otto   | 72 × 80 mm  | ~652 cc | 11.0:1 |
| Single Cylinder  | Otto   | 76 × 86 mm  | ~390 cc |  8.5:1 |
| Diesel Inline-4  | Diesel | 81 × 96 mm  | ~2.00 L | 18.0:1 |
| Diesel Inline-6  | Diesel | 92 × 100 mm | ~3.20 L | 20.0:1 |

The diesel presets use compression-ignition physics.  No spark plug fires — instead, air is compressed until it is hot enough to ignite injected fuel.  The higher compression ratios (18:1 and 20:1 vs 8–12:1 for petrol) are what make this possible.

---

## P-V Diagram

The bottom panel shows the **pressure-volume diagram** for cylinder 1.  Four numbered state points ①–④ mark the corners of the cycle:

| Segment | Otto (petrol) | Diesel |
|---------|--------------|--------|
| **1→2** Blue | Adiabatic compression | Adiabatic compression |
| **2→3** Red | Isochoric heat addition (vertical — constant volume) | Isobaric heat addition (horizontal — constant pressure) |
| **3→4** Yellow | Adiabatic expansion / power stroke | Adiabatic expansion / power stroke |
| **4→1** Grey | Isochoric heat rejection (exhaust) | Isochoric heat rejection (exhaust) |

A **live dot** tracks the real-time thermodynamic state of cylinder 1 around the loop.  The **area enclosed** by the loop is proportional to the net work done per cycle — a larger loop means more power output.
