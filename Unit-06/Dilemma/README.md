# Dilemma — Iterated Prisoner's Dilemma Tournament

A simulation of Robert Axelrod's famous computer tournaments from the 1980s,
in which researchers submitted strategies that competed against each other in
repeated rounds of the Prisoner's Dilemma. The program is fully extensible:
drop a compiled strategy into `strategies/` and it enters the next tournament.

---

## The Game

Each round, two players simultaneously choose to **Cooperate** or **Defect**.
Their payoff is determined by the classic matrix:

|                 | Opponent Cooperates | Opponent Defects |
|-----------------|---------------------|------------------|
| **I Cooperate** | R = 3 (Reward)      | S = 0 (Sucker)   |
| **I Defect**    | T = 5 (Temptation)  | P = 1 (Punishment)|

The constraints `T > R > P > S` and `2R > T+S` make mutual cooperation
collectively optimal, but individual incentive always favours defection —
the dilemma.

---

## Package Overview

```
com.examples.dilemma.strategy       — Move, Payoff, GameHistory, Strategy interface
com.examples.dilemma.strategy.impl  — Nine famous strategy implementations
com.examples.dilemma.engine         — Game, Tournament, results, and StrategyLoader
com.examples.dilemma.io             — CSV export
com.examples.dilemma.gui            — JavaFX tournament visualizer
com.examples.dilemma.app            — CLI entry point
```

---

## Built-in Strategies

| Strategy | Description |
|---|---|
| **Tit for Tat** | Cooperate first, then copy the opponent's last move. Axelrod's winner. |
| **Always Cooperate** | Cooperate unconditionally. |
| **Always Defect** | Defect unconditionally. |
| **Random** | 50/50 coin flip each round. |
| **Grim Trigger** | Cooperate until betrayed once, then defect forever. |
| **Pavlov** | Win-Stay, Lose-Shift: repeat if rewarded, switch if punished. |
| **Tit for Two Tats** | Only retaliate after two consecutive opponent defections. |
| **Suspicious Tit for Tat** | Open with Defect, then mirror. |
| **Joss** | Like Tit for Tat but defects with 10% probability when it would cooperate. |

---

## Submitting Your Own Strategy

See [`strategies/README.txt`](strategies/README.txt) for step-by-step instructions.
Any `.class` file in `strategies/` that implements `Strategy` is loaded
automatically at runtime via `URLClassLoader`.

---

## Building and Running

```bash
# Run the CLI tournament (default: 200 rounds, all built-in strategies)
mvn exec:java@cli

# Custom rounds and output directory
mvn exec:java@cli -Dexec.args="--rounds 500 --output results/"

# Launch the Swing GUI
mvn exec:java@gui

# Run unit tests
mvn test

# Generate Javadocs (output: target/site/apidocs/index.html)
mvn javadoc:javadoc
```

---

## Output

Two CSV files are written to the `output/` directory (or the directory chosen
in the GUI):

| File | Contents |
|---|---|
| `standings-[timestamp].csv` | Rank, strategy name, total score, avg/round, W/L/D |
| `matches-[timestamp].csv` | Every match: Strategy A, Strategy B, scores, winner |

---

## Tests

Unit tests are in `src/test/java/com/examples/dilemma/` and use **JUnit 5**.
They cover payoff correctness, individual strategy behaviour, and full game
simulation against known outcomes.
