# Introduction to Internal Combustion Engines

This document explains how a 4-stroke internal combustion engine works, what the thermodynamic cycles are, and how a diesel engine differs from a conventional petrol (Otto-cycle) engine.  Everything described here is directly simulated in the accompanying application.

---

## 1. What Is an Internal Combustion Engine?

An **internal combustion engine (ICE)** converts the chemical energy stored in fuel into mechanical rotation.  Burning the fuel inside a sealed cylinder creates rapidly expanding hot gas, which pushes a piston down.  A connecting rod translates that back-and-forth piston motion into the continuous rotation of a **crankshaft** — the same shaft that ultimately turns the wheels of a car or the blade of a generator.

The key mechanical parts are:

| Part | Role |
|------|------|
| **Cylinder** | The sealed bore in which combustion takes place |
| **Piston** | Slides up and down inside the cylinder; transmits gas pressure to the crank |
| **Connecting rod** | Links the piston pin to the crank pin |
| **Crankshaft** | Converts reciprocating motion into rotation; stores kinetic energy via its flywheel |
| **Intake valve** | Opens to admit fresh air (or air-fuel mixture) into the cylinder |
| **Exhaust valve** | Opens to expel burned gases after combustion |

Two geometric quantities define an engine's character:

* **Bore** — the inner diameter of the cylinder.
* **Stroke** — the distance the piston travels between Top Dead Centre (TDC) and Bottom Dead Centre (BDC).
* **Displacement** — bore² × π/4 × stroke × number of cylinders; the swept volume per cycle.
* **Compression ratio (r_c)** — the ratio of the maximum cylinder volume (piston at BDC) to the minimum (piston at TDC).  A higher r_c squeezes the charge more tightly and generally yields better efficiency.

---

## 2. The Four-Stroke Cycle

Both petrol and diesel engines use a **four-stroke cycle**.  One complete cycle spans two full crankshaft revolutions (720°) and visits four distinct strokes:

```
   TDC                     TDC                     TDC                     TDC
    │  ↓ Intake            │  ↑ Compress           │  ↓ Power              │  ↑ Exhaust
    │                      │                       │  ← spark/injection    │
    │  Intake valve open   │  Both valves closed   │  Both valves closed   │  Exhaust valve open
    │                      │                       │                       │
   BDC                     BDC                     BDC                     BDC
  (stroke 1)             (stroke 2)             (stroke 3)             (stroke 4)
```

| Stroke | Crank | What happens |
|--------|-------|-------------|
| **Intake** | 0°–180° | Piston descends; intake valve open; fresh charge drawn into the cylinder |
| **Compression** | 180°–360° | Both valves closed; piston ascends; charge is compressed |
| **Power** | 360°–540° | Ignition occurs at TDC; expanding gases drive the piston down — this is the only stroke that does net work |
| **Exhaust** | 540°–720° | Exhaust valve open; piston ascends; burned gases are expelled |

Only **one stroke in four** is a power stroke.  Multi-cylinder engines stagger the firing of each cylinder so that at least one is always pushing, giving smoother torque delivery.

---

## 3. The Otto Cycle (Petrol / Spark-Ignition)

Most petrol engines use the **Otto cycle**, named after Nikolaus Otto (1876).  It describes the ideal thermodynamic sequence for a spark-ignition engine.

The four thermodynamic processes map directly onto the four physical strokes:

```
  P (pressure)
  │
  │       ③
  │       │╲
  │       │  ╲  ← Isentropic expansion
  │   ②   │    ╲
  │   /            ④
  │  /  ← Isentropic compression    │
  │ /                               │  ← Isochoric heat rejection
  ①  ──────────────────────────────
                                    V (volume)
```

| Segment | Process | Description |
|---------|---------|-------------|
| 1 → 2 | **Isentropic compression** | Charge compressed with no heat transfer; P rises, T rises |
| 2 → 3 | **Isochoric heat addition** | Spark fires at TDC; fuel burns instantly at constant volume; P and T spike |
| 3 → 4 | **Isentropic expansion** | Hot gas pushes piston down; P and T fall |
| 4 → 1 | **Isochoric heat rejection** | Exhaust valve opens at BDC; pressure drops to atmospheric |

**Thermal efficiency** of the ideal Otto cycle depends only on the compression ratio:

```
η_Otto  =  1 − r_c^(1 − γ)
```

where γ ≈ 1.35 is the ratio of specific heats.  A higher compression ratio always improves efficiency, but too high a ratio causes the charge to auto-ignite before the spark ("knock"), which is why high-performance petrol engines require premium (high-octane) fuel.

---

## 4. The Diesel Cycle (Compression-Ignition)

Rudolf Diesel patented his engine in 1892.  The key insight was to compress **air only** — no fuel — to such a high pressure and temperature (~800 K) that injected fuel auto-ignites spontaneously.  There is no spark plug.

Because there is no risk of premature ignition of a fuel-air mixture during the compression stroke, a diesel engine can use a much higher compression ratio (typically 16–22:1 vs 8–13:1 for petrol), giving it a fundamental efficiency advantage.

The thermodynamic cycle differs in one important way:

| Segment | Otto | Diesel |
|---------|------|--------|
| 1 → 2 | Isentropic compression | Isentropic compression (higher r_c) |
| **2 → 3** | **Isochoric** heat addition at TDC (constant **volume**) | **Isobaric** heat addition (constant **pressure**) |
| 3 → 4 | Isentropic expansion | Isentropic expansion |
| 4 → 1 | Isochoric heat rejection | Isochoric heat rejection |

In the diesel cycle, fuel is injected and burns as the piston begins its downward stroke.  The combustion pressure is held roughly constant while the volume increases — this is the **isobaric** (constant-pressure) segment.  The point at which combustion ends is called the **cutoff**, characterised by the **cutoff ratio**:

```
r_co  =  V_cutoff / V_TDC  =  T_peak / T_comp
```

**Thermal efficiency** of the ideal Diesel cycle:

```
η_Diesel  =  1 − (1 / r_c^(γ−1)) · (r_co^γ − 1) / (γ · (r_co − 1))
```

As r_co → 1 (very brief combustion), the Diesel efficiency formula reduces to the Otto efficiency at the same r_c.  For r_co > 1, the term in brackets is always greater than 1, meaning that **at the same compression ratio a Diesel cycle is less efficient than an Otto cycle**.  However, diesel engines operate at compression ratios roughly twice as high, which more than compensates — real diesel engines are typically 35–45% thermally efficient vs 25–35% for petrol engines.

---

## 5. Otto vs Diesel — Side-by-Side Comparison

| Property | Otto (petrol) | Diesel |
|----------|--------------|--------|
| Ignition | Spark plug | Compression heat |
| Compression ratio | 8–13 : 1 | 14–22 : 1 |
| Fuel | Petrol (gasoline), LHV ≈ 44 MJ/kg | Diesel fuel, LHV ≈ 42.5 MJ/kg |
| Heat addition | Constant volume (isochoric) | Constant pressure (isobaric) |
| Peak pressure | Rises sharply at TDC | Stays at compression pressure through combustion |
| Typical efficiency | 25–35% | 35–45% |
| Torque character | High RPM, quick response | High torque at low RPM |
| P-V diagram shape | Tall, narrow loop | Wider loop with flat top |

---

## 6. Reading the P-V Diagram in the Simulator

The bottom panel of the simulator shows a **pressure-volume (P-V) diagram** for cylinder 1.  On this chart:

* The **horizontal axis (V)** spans from the clearance volume at TDC on the left to the maximum volume at BDC on the right.
* The **vertical axis (P)** spans from atmospheric pressure at the bottom to peak pressure at the top.
* The **numbered state points ①–④** mark the corners of the ideal cycle.
* Each curve segment is colour-coded and labelled in the legend.
* The **filled dot** shows the real-time thermodynamic state of cylinder 1, moving around the loop as the engine runs.
* The **area enclosed** by the loop is proportional to the net work done per cycle — a larger loop means more power output.

Increasing the throttle injects more fuel, raising T_peak and expanding the loop.  Switching from an Otto to a Diesel preset changes the loop shape: the isochoric (vertical) heat-addition line becomes an isobaric (horizontal) line, and the expansion curve starts from a larger volume.

---

## 7. Crankshaft Dynamics

The simulation models the crankshaft as a rotating flywheel with rotational inertia I:

```
I · dω/dt  =  T_combustion  −  K_load · ω  −  K_friction · ω
```

Load and friction torques are both proportional to angular velocity ω, which creates a natural equilibrium RPM where net torque is zero.  The throttle controls fuel mass per cycle; more fuel → higher combustion torque → higher equilibrium RPM.

Torque from a single cylinder peaks near 70° after TDC (where the crank geometry is most favourable) and drops to zero at TDC and BDC where the rod and crank are collinear:

```
T  =  (P − P_atm) · A · r · sin(φ + β) / cos β
β  =  arcsin(r · sin φ / l)
```

Multi-cylinder engines overlap their power strokes so that at least one cylinder is always near its torque peak, smoothing the output.
