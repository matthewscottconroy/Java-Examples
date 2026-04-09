# Markov Monopoly

An interactive console application that teaches Markov chains through four
classic examples and a full Monopoly simulation.

## What Is a Markov Chain?

A Markov chain is a random process where the future depends only on the
current state — not on any history of how you got there.  Formally:

```
P(X_{n+1} = j | X_n = i, X_{n-1}, ...) = P(X_{n+1} = j | X_n = i)
```

The transition matrix T encodes these probabilities: T[i][j] is the
probability of moving from state i to state j in one step.

## Build, Run, Test & Docs

**Prerequisites**

| Tool | Minimum version | Check with |
|------|----------------|------------|
| JDK  | 17             | `java -version` |
| Maven | 3.6           | `mvn -version` |

```bash
# Compile
mvn compile

# Run (interactive console menu)
mvn exec:java

# Run the unit test suite
mvn test

# Package a self-contained JAR
mvn package
java -jar target/markovmonopoly-1.0-SNAPSHOT.jar

# Generate Javadoc API documentation
mvn javadoc:javadoc
# then open target/site/apidocs/index.html
```

The main menu offers three sections:

```
[1] Classic Markov Chain Examples
[2] Markov Chain Editor
[3] Monopoly Simulator
[q] Quit
```

## Section 1 — Classic Examples

| Example | Concept demonstrated |
|---|---|
| **Weather Model** | Introduction to chains, stationary distribution, convergence |
| **Gambler's Ruin** | Absorbing chains, ruin probability, expected game duration |
| **PageRank** | Markov chains as the basis of Google's search algorithm |
| **Ehrenfest Urn** | Birth-death chain, detailed balance, thermodynamic entropy |

Each example prints a step-by-step explanation, the transition matrix, and
key results (stationary distribution, absorption probabilities, etc.).

### Weather Model
Two states: Sunny, Rainy.  Shows how repeated matrix multiplication
converges to the stationary distribution regardless of starting state.

### Gambler's Ruin
A gambler starts with $k and plays until $0 (ruined) or $N (wins).
The absorbing Markov chain gives exact ruin probability and expected
number of bets before the game ends.

### PageRank
Models the web as a Markov chain: pages are states, hyperlinks are
transitions.  The stationary distribution equals the PageRank scores.
Demonstrates the "random surfer" model with a damping factor.

### Ehrenfest Urn
N molecules distributed between two chambers.  Each step, one random
molecule moves.  Demonstrates detailed balance, reversibility, and why
entropy increases toward equilibrium.

## Section 2 — Markov Chain Editor

An interactive REPL for building and analyzing custom chains:

| Command | Description |
|---|---|
| **New chain** | Define states and transition probabilities interactively |
| **Add/remove states** | Modify the chain structure |
| **Set transitions** | Enter probabilities row by row |
| **Analyze** | Compute stationary distribution, classification, convergence |
| **Save / Load** | Persist chains to JSON files |
| **Simulate** | Run the chain for N steps and print the state sequence |

### Analysis outputs

- **Stationary distribution π** — long-run fraction of time in each state
- **State classification** — recurrent, transient, absorbing
- **Communication classes** — which states can reach which
- **Expected return time** — E[T_i | start at i] = 1/π_i
- **Absorption probabilities** — if absorbing states exist

## Section 3 — Monopoly Simulator

Simulates the board game and builds the Markov chain from the observed
transitions.

### Monopoly as a Markov Chain

The board has 40 squares plus a conceptual Jail state = 41 states.
Special rules (Go to Jail, Community Chest, Chance cards, three-doubles
rule) create a non-uniform stationary distribution.

### Simulation menu

| Option | Description |
|---|---|
| **Run simulation** | Simulate N turns and record state transitions |
| **Build Markov chain** | Construct the transition matrix from simulation data |
| **Analyze chain** | Compute stationary distribution, most-visited squares |
| **Print board** | Show all 40 squares with visit frequencies |
| **Compare theory vs simulation** | Side-by-side of analytical and empirical results |

### Key findings

Running a large simulation reveals which squares are visited most:
- **Jail / Just Visiting** is the most-visited single space
- **Illinois Ave (red), B&O Railroad, and Tennessee Ave** consistently rank high
- **Mediterranean and Baltic (purple)** are among the least visited


## Project Structure

```
markovmonopoly/
├── pom.xml
└── src/main/java/com/markovmonopoly/
    ├── Main.java                          # entry point, main menu
    ├── core/
    │   ├── MarkovChain.java               # chain: states + transition matrix
    │   ├── TransitionMatrix.java          # matrix operations, normalization
    │   ├── MarkovAnalysis.java            # stationary dist, classification
    │   ├── StateClass.java                # recurrent/transient/absorbing enum
    │   └── SimulationStats.java           # step counter, state history
    ├── editor/
    │   └── MarkovChainEditor.java         # interactive chain builder REPL
    ├── examples/
    │   ├── ExampleRunner.java             # example selection menu
    │   ├── WeatherExample.java
    │   ├── GamblersRuinExample.java
    │   ├── PageRankExample.java
    │   └── EhrenfestExample.java
    ├── monopoly/
    │   ├── board/
    │   │   ├── MonopolyBoard.java         # 40-square board definition
    │   │   ├── BoardSpace.java            # space name, type, group
    │   │   └── SpaceType.java             # GO, PROPERTY, RAILROAD, etc.
    │   ├── simulation/
    │   │   ├── MonopolySimulator.java     # turn-by-turn simulator
    │   │   ├── MonopolyGame.java          # game state (player, cards)
    │   │   ├── Player.java
    │   │   ├── Dice.java / DiceRoll.java
    │   │   ├── CardDeck.java / Card.java / CardEffect.java
    │   │   └── SimulationStats.java
    │   └── markov/
    │       └── MonopolyMarkovChainBuilder.java  # build chain from sim data
    ├── io/
    │   └── MarkovChainIO.java             # JSON save/load
    └── ui/
        ├── ConsoleMenu.java               # reusable numbered-menu widget
        └── TableFormatter.java            # ASCII table formatting
```
