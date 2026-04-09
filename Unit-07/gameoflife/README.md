# Conway's Game of Life

A fully interactive simulator for Conway's Game of Life with customizable rules, a comprehensive pattern library, full save/load support, and complete playback control including step-backward and rewind.

## Build, Run, Test & Docs

**Prerequisites**

| Tool | Minimum version | Check with |
|------|----------------|------------|
| JDK  | 17             | `java -version` |
| Maven | 3.6           | `mvn -version` |

```bash
# Compile
mvn compile

# Run Game of Life
mvn exec:java

# Run Wolfram 1D Cellular Automaton explorer
mvn exec:java@wolfram

# Run the unit test suite
mvn test

# Package a self-contained JAR
mvn package
java -jar target/gameoflife-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

---

## The Rules of Conway's Game of Life

The Game of Life operates on a two-dimensional grid of cells, each either *alive* (on) or *dead* (off). At each tick, every cell's next state is determined simultaneously by applying four rules based on the Moore neighborhood (the eight surrounding cells):

1. **Underpopulation**: A live cell with fewer than 2 live neighbors dies.
2. **Survival**: A live cell with 2 or 3 live neighbors survives.
3. **Overpopulation**: A live cell with more than 3 live neighbors dies.
4. **Reproduction**: A dead cell with exactly 3 live neighbors becomes alive.

In standard notation: **B3/S23** — *born* with 3 neighbors, *survives* with 2 or 3.

These four rules, applied simultaneously to every cell on an infinite grid, define a discrete-time dynamical system capable of universal computation.

---

## Cellular Automata: The Broader Context

Conway's Game of Life is one specific instance of a broader class of systems called **cellular automata** (CA). A cellular automaton is any discrete model in which:

- Space is divided into a regular grid of **cells**
- Each cell is in one of a finite set of **states**
- All cells update **simultaneously** according to a **local rule** that depends only on the cell and its neighbors
- The rule is applied uniformly across the entire grid

CAs model the world as computation: the present state is input; the rule is the program; the next state is output. The insight is that enormously complex global behavior can emerge from the repeated application of a simple local rule.

### Dimensions and Neighborhoods

The most common CAs are:

- **1D (elementary)**: One row of cells; each cell has two neighbors; rules have 2³ = 8 inputs → 256 possible rules (Wolfram's elementary CAs)
- **2D (Conway-style)**: Two-dimensional grid; the most common neighborhood choices are Moore (8 neighbors) and von Neumann (4 neighbors — orthogonal only)
- **3D and higher**: Used in models of chemical reactions, crystal growth, and biological development

### Famous Cellular Automata Beyond Life

| Automaton | Type | Notable for |
|-----------|------|-------------|
| Conway's Life (B3/S23) | 2D Moore | Universal computation |
| HighLife (B36/S23) | 2D Moore | Self-replicating patterns |
| Wolfram Rule 110 | 1D, 2-state | Simplest known Turing-complete system |
| Wolfram Rule 30 | 1D, 2-state | Chaos / pseudorandom generation |
| Brian's Brain | 2D, 3-state | Active, perpetually moving patterns |
| Langton's Ant | 2D Turing machine | Emergent highway behavior |
| Lenia | 2D, continuous | Smooth, biological-looking lifeforms |
| Game of Life in 3D | 3D Moore | Exploratory; many rules stable |

### Why Cellular Automata Matter

CAs are studied across mathematics, computer science, physics, and biology because they sit at the intersection of several deep ideas:

**Emergence**: The most striking feature of Game of Life is that patterns like gliders, guns, and self-replicators emerge from rules that know nothing about such structures. No individual cell "knows" it's part of a glider; the glider is a property of the collective. This is a clean demonstration that complex, organized behavior can arise spontaneously from simple laws with no designer and no top-down instruction.

**Universality**: Conway proved that Game of Life is Turing complete — given a sufficiently elaborate initial configuration, it can simulate any computation that any computer can perform. The same is true of Wolfram's Rule 110. This means these systems are, in principle, as powerful as any digital computer (or any other Turing-equivalent model of computation), despite operating purely through local cell interactions.

**Irreducibility**: For many initial conditions, the only way to know the state at generation 1,000 is to run the simulation for 1,000 steps. There is no formula that "jumps ahead." This property — **computational irreducibility** — is discussed at length in the `DYNAMIC_SYSTEMS.md` file.

**Modeling**: CAs have been used to model fluid dynamics (lattice Boltzmann methods), traffic flow, tumor growth, forest fire spread, chemical reaction-diffusion systems, and many other phenomena. The appeal is that complex, realistic-looking patterns emerge from simple rules without any mathematical analysis of the global system.

---

## Famous Patterns

### Still Lifes (never change)
| Pattern | Cells | Description |
|---------|-------|-------------|
| Block | 4 | Simplest still life — a 2×2 square |
| Beehive | 6 | Oval shape; extremely common in random soups |
| Loaf | 7 | Asymmetric oval; stable |
| Boat | 5 | 5-cell still life with a "prow" |

### Oscillators (periodic)
| Pattern | Period | Cells | Notable |
|---------|--------|-------|---------|
| Blinker | 2 | 3 | Most common oscillator |
| Toad | 2 | 6 | Two offset rows of 3 |
| Beacon | 2 | 8 | Two touching diagonal blocks |
| Pulsar | 3 | 48 | Most common period-3 oscillator |
| Pentadecathlon | 15 | 12 | Longest period among simple oscillators |

### Spaceships (translate across the grid)
| Pattern | Period | Speed | Direction |
|---------|--------|-------|-----------|
| Glider | 4 | c/4 diagonal | NE |
| LWSS | 4 | c/2 | E |
| MWSS | 4 | c/2 | E |
| HWSS | 4 | c/2 | E |

The speed *c/4 diagonal* means the glider advances 1 cell diagonally per 4 generations. The theoretical maximum speed in GoL is *c* (one cell per generation); no pattern can travel faster due to the local interaction rule.

### Guns (emit spaceships indefinitely)
| Pattern | Period | Cells | Notable |
|---------|--------|-------|---------|
| Gosper Glider Gun | 30 | 36 | First discovered; unlimited growth |
| Simkin Glider Gun | 120 | 33 | Smallest known gun |

The discovery of the Gosper Glider Gun in 1970 (by Bill Gosper, in response to Conway's $50 prize) proved that GoL can produce patterns with unbounded population — a non-obvious result.

### Methuselahs (small but long-lived)
| Pattern | Cells | Stabilization Gen | Final Population |
|---------|-------|-------------------|-----------------|
| R-pentomino | 5 | 1,103 | 116 |
| Diehard | 7 | 130 (dies) | 0 |
| Acorn | 7 | 5,206 | 633 |

Methuselahs demonstrate that a tiny initial configuration can produce an extraordinarily long and complex evolution before settling into a final stable or periodic state. The R-pentomino, five simple cells, takes over 1,000 generations to stabilize and produces gliders, spaceships, and dozens of still lifes.

---

## Customizable Rules (B/S Notation)

The simulator supports any B/S rule. Beyond Conway's Life, notable alternatives:

| Rule | Notation | Character |
|------|----------|-----------|
| Conway's Life | B3/S23 | The original |
| HighLife | B36/S23 | Also produces self-replicating "replicators" |
| Day & Night | B3678/S34678 | Dense and sparse regions behave symmetrically |
| Seeds | B2/S | Chaotic bursts; no cell ever survives two generations |
| Maze | B3/S12345 | Produces long winding corridors |
| Life Without Death | B3/S012345678 | Cells born; cells never die; monotone growth |
| Replicator | B1357/S1357 | Everything replicates in a fractal pattern |

To use: open Settings → enter a B/S notation in the Rule field.

---

## Controls

| Action | Control |
|--------|---------|
| Play / Pause | Space bar or ▶ button |
| Step forward | → key or Step button |
| Step backward | ← key or ◀ button |
| Rewind to earliest | Home key or ⏮ button |
| Stop & rewind | ■ button |
| Fast forward 10 gens | ⏭ button |
| Toggle cell | Left click on cell |
| Paint alive | Left drag |
| Erase (paint dead) | Right drag |
| Zoom in/out | Mouse wheel |
| Pan | Middle-click drag or Alt+drag |
| Place pattern | ☰ Pattern → select → click to place |
| Cancel pattern | Escape |

---

## Save / Load Format

States are saved as human-readable `.cgol` text files:

```
# Conway's Game of Life — State File
# Format: CGOL v1
rule: B3/S23
generation: 42
rows: 60
cols: 80
toroidal: false
population: 127
cells:
3,4
3,5
...
```

Each line after `cells:` is a `row,col` pair of a live cell. The format is designed to be editable by hand or generated programmatically.
