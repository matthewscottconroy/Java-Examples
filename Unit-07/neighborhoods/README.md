# Schelling Segregation Simulator

An interactive Java/Swing simulation of Thomas Schelling's 1971 model of
residential segregation.  The central insight: even mild individual preferences
for neighbors "like me" can produce dramatic large-scale segregation — a striking
example of how macro-level patterns emerge from micro-level rules.

## The Model

The city is a 2D grid of agents.  Each cell is either:
- **Type A** (blue)
- **Type B** (orange)
- **Empty**

Each step, every unsatisfied agent moves to a random empty cell.  An agent is
**satisfied** if at least a threshold fraction of its non-empty neighbors are the
same type.

For example, with threshold = 0.33: an agent needs at least 1/3 of its neighbors
to be the same type.  This sounds very tolerant — yet the simulation reliably
produces nearly complete segregation.

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
java -jar target/schelling-simulation-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

## Controls

| Control | Description |
|---|---|
| **Threshold A / B** sliders | Minimum fraction of same-type neighbors for satisfaction (0–1) |
| **Empty fraction** | Fraction of grid cells left unoccupied |
| **Type B fraction** | Relative proportion of B vs A agents |
| **Grid size** | Number of rows and columns |
| **Neighborhood** | Moore (8), Von Neumann (4), or Extended Moore (24) |
| **Initial condition** | Random, Segregated, Checkerboard, Enclave, Clusters |
| **Speed** | Simulation steps per second |
| **Step** | Advance exactly one step |
| **Play / Pause** | Run continuously |
| **Reset** | Reinitialise with current settings |
| **Paint mode** | Click cells to paint Type A, Type B, or erase |
| **Heatmap** | Color cells by satisfaction level |
| **Export** | Save PNG snapshot or CSV metrics |

## Panels

### Grid View (centre)
The live grid.  Colours: blue = Type A, orange = Type B, dark = empty.
Satisfied agents are shown at full brightness; unsatisfied agents are slightly
dimmed so you can see who is about to move.

### History Chart
Tracks over time:
- Segregation index (0 = fully mixed, 1 = fully segregated)
- Percentage of satisfied agents
- Number of moves per step

### Phase Diagram
Shows the stable segregation level as a function of the threshold parameter,
revealing the sharp phase transition around threshold ≈ 0.5.

## Key Concepts

### Segregation Index
Fraction of same-type neighbors averaged over all occupied cells.
A value near 1.0 means most agents are surrounded by their own type.

### Phase Transition
Below ~0.5 threshold: the system stabilises at partial segregation.
Above ~0.5: the system quickly reaches near-complete segregation.
The transition is sharp — a small change in threshold causes a large change
in outcome.

### Neighborhood Types

| Type | Cells checked | Notes |
|---|---|---|
| **Moore** | 8 surrounding cells (diagonals included) | Default |
| **Von Neumann** | 4 cardinal neighbors (N, S, E, W) | Slower, more structured patterns |
| **Extended Moore** | 24 cells within radius 2 | Smoother, larger-scale clusters |

### Initial Conditions

| Condition | Description |
|---|---|
| **Random** | Agents placed uniformly at random |
| **Segregated** | Type A on left half, Type B on right half |
| **Checkerboard** | Maximum initial mixing — alternating cells |
| **Enclave** | Type B clustered in a central disc, Type A surrounding |
| **Clusters** | Voronoi-seeded clusters (6–24 seeds) — patchy mixing |

Checkerboard and Enclave starts dramatically illustrate how quickly agents
sort themselves even from a structured or well-mixed initial state.

## Asymmetric Thresholds

Type A and Type B can have different satisfaction thresholds.  Setting
threshold A = 0.5 and threshold B = 0.1 lets you explore how tolerance
asymmetry affects the final outcome: the more-tolerant group ends up more
scattered while the less-tolerant group clusters tightly.


## Project Structure

```
neighborhoods/
├── pom.xml
└── src/
    ├── main/java/com/schelling/
    │   ├── Main.java                       # entry point
    │   ├── model/
    │   │   ├── Grid.java                   # 2D grid of AgentType cells
    │   │   ├── AgentType.java              # A, B, EMPTY enum
    │   │   ├── SimulationConfig.java       # immutable config (builder pattern)
    │   │   ├── NeighborhoodType.java       # MOORE, VON_NEUMANN, EXTENDED_MOORE
    │   │   └── InitialCondition.java       # RANDOM, SEGREGATED, CHECKERBOARD, ENCLAVE, CLUSTERS
    │   ├── simulation/
    │   │   └── SchellingSimulation.java    # step(), isStable(), segregation index
    │   └── ui/
    │       ├── MainFrame.java              # top-level JFrame
    │       ├── GridPanel.java              # live grid canvas
    │       ├── ControlPanel.java           # sliders and buttons
    │       ├── HistoryChartPanel.java      # time-series chart
    │       └── PhaseDiagramPanel.java      # threshold vs segregation
    └── test/java/com/schelling/
        ├── SchellingSimulationTest.java
        └── SimulationConfigTest.java
```
