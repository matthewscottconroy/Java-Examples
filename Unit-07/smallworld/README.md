# Watts-Strogatz Small-World Network Simulator

A visual, interactive implementation of the Watts-Strogatz small-world network
model from the landmark 1998 paper:

> Watts, D.J. & Strogatz, S.H. (1998).
> *Collective dynamics of 'small-world' networks.*
> **Nature** 393, 440–442.

## The Model

### Ring Lattice
Start with **n** nodes arranged in a ring. Connect each node to its **k**
nearest neighbours on each side (total degree = 2k). The result is a *regular
lattice*: highly clustered but with long average shortest paths.

### Rewiring
Visit every edge in order. With probability **p**, replace (rewire) the edge's
far endpoint with a uniformly-random node — avoiding self-loops and
multi-edges. When p = 0 nothing changes; when p = 1 the graph becomes
essentially random (Erdős-Rényi-like).

### The Small-World Regime
Between those extremes lies the famous small-world regime (roughly p ≈ 0.01 –
0.1). A tiny fraction of long-range shortcut edges collapses average path
length L toward random-graph levels, while clustering coefficient C remains
nearly as high as the regular lattice. In the paper's language:

```
L(p) / L(0)  drops steeply  ←  a few shortcuts suffice
C(p) / C(0)  stays near 1   ←  local structure is preserved
```

This combination — high C, low L — characterises real-world networks:
power grids, neural circuits, social acquaintance graphs, the Internet.

## Metrics

| Symbol | Name | Definition |
|--------|------|-----------|
| **C** | Clustering coefficient | Average fraction of a node's neighbours that are also connected to each other |
| **L** | Average path length | Mean shortest path between all reachable node pairs (BFS) |
| **C(p)/C(0)** | Relative clustering | Normalised to the ring lattice baseline |
| **L(p)/L(0)** | Relative path length | Normalised to the ring lattice baseline |

A disconnected graph has L = ∞ (normalised to 1.0 in the phase diagram).

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
java -jar target/watts-strogatz-simulation-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

### Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| `The parameters 'mainClass' ... are missing` | Old cached plugin resolution | Run `mvn clean exec:java` |
| Blank/grey window on Linux | Missing display / headless JVM | Set `DISPLAY=:0` or run inside a desktop session |
| `Could not find or load main class` | Stale compiled classes | `mvn clean compile exec:java` |
| Tests not discovered | JDK < 17 or missing surefire | Verify `java -version` ≥ 17 |

## UI Guide

### Network Panel (centre)
Nodes sit on a circle. Edges are colour-coded:

| Colour | Meaning |
|--------|---------|
| Grey | Original lattice edge |
| Orange-red | Rewired edge |
| Gold glow | Edge currently being visited (animation) |

**Hover** over any node to see a tooltip with its index, degree, and local
clustering coefficient.

### Control Panel (right)

**Controls**
- **Step 1 edge / Step 10 edges** — advance the rewiring one or ten steps
- **▶ Run / ⏸ Pause** — auto-step at the speed set by the slider
- **Reset** — rebuild the ring lattice and re-queue all edges
- **Run speed** slider — left = slow (500 ms/step), right = fast (20 ms/step)

**Settings** (applied on Reset)
- **n** — number of nodes (6 – 250)
- **k** — neighbourhood half-degree (1 – 20); clamped automatically if n ≤ 2k
- **p** — rewiring probability (slider, 0 – 1)

**Presets** — click to apply immediately (no separate Reset needed):

| Preset | n | k | p | Notes |
|--------|---|---|---|-------|
| Regular (p=0) | 30 | 3 | 0.00 | Pure ring lattice |
| Small-world (p=0.05) | 30 | 3 | 0.05 | Paper's sweet spot |
| Near-random (p=0.5) | 30 | 3 | 0.50 | Transitional |
| Random (p=1) | 30 | 3 | 1.00 | Fully random graph |

**Metrics** — live bar chart of rewiring progress, C(p)/C(0), and L(p)/L(0).

### Phase Diagram Panel (bottom)
Plots the two normalised metrics against p on a **log scale** — exactly the
figure from the original paper.

Click **Run Sweep** to compute 20 log-spaced p values (3 independent runs per
point, averaged). The sweep runs in a background thread. A dashed vertical line
marks the current simulation's p value.

The sweep is specific to the current **(n, k)** settings. If you change n or k
and reset, the panel clears and prompts you to run a new sweep.

### File Menu
- **Export Network as PNG** — saves the network ring diagram as a PNG image

## Parameters and Their Effects

```
Small n (20-40):  Individual rewirings are clearly visible
Large n (100+):   Smoother statistical behaviour, slower metrics
Small k (2-3):    Sparse lattice, dramatic path-length collapse
Large k (6+):     Dense lattice, C stays high longer before collapsing
p = 0.001-0.01:   Onset of small-world effects
p = 0.1-0.3:      Most of the C/L transition is complete
p > 0.5:          Near-random graph; both C and L approach random-graph values
```

## Project Structure

```
src/main/java/com/wattsstrogatz/
├── Main.java                          Entry point
├── model/
│   ├── Edge.java                      Undirected edge with rewire tracking
│   ├── Network.java                   Adjacency-set graph; ring-lattice factory
│   ├── NetworkConfig.java             Immutable (n, k, p, seed) config
│   └── NetworkMetrics.java            C and L computation (BFS)
├── simulation/
│   └── WattsStrogatzSimulation.java   Step-by-step Watts-Strogatz algorithm
└── ui/
    ├── MainFrame.java                 Application window
    ├── NetworkPanel.java              Ring-diagram renderer + hover tooltips
    ├── ControlPanel.java              Controls, settings, presets, metrics
    ├── MetricsPanel.java              Normalised bar-chart metrics display
    └── PhaseDiagramPanel.java         C/L vs p phase-diagram sweep
```

## References

- Watts, D.J. & Strogatz, S.H. (1998). *Collective dynamics of 'small-world'
  networks.* Nature 393, 440–442. https://doi.org/10.1038/30918
- Erdős, P. & Rényi, A. (1959). *On random graphs.* Publicationes Mathematicae
  6, 290–297.
- Newman, M.E.J. (2003). *The structure and function of complex networks.*
  SIAM Review 45(2), 167–256.
