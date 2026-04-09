# Wolfram 1D Elementary Cellular Automata

An interactive explorer for Wolfram's elementary cellular automata — the simplest class of one-dimensional CA, and the system in which Wolfram observed that a single rule (Rule 110) is Turing complete.

## Running

```
mvn exec:java@wolfram
```

---

## What Are Elementary Cellular Automata?

An **elementary cellular automaton** is the simplest possible cellular automaton:

- The grid is a **single row** of cells
- Each cell is **binary** (0 = dead, 1 = alive)
- Each cell's next state depends on **itself and its two nearest neighbors** — three cells total
- The rule is applied simultaneously to all cells

Since there are 3 cells each with 2 possible states, there are 2³ = **8 possible neighborhoods**. The rule specifies a new state (0 or 1) for each neighborhood. Since there are 8 neighborhoods each with 2 possible outputs, there are 2⁸ = **256 possible rules**, numbered 0–255.

### Reading the Rule Number

The rule number is the decimal representation of an 8-bit binary sequence:

```
Neighborhood:  111  110  101  100  011  010  001  000
Rule 110 bits:  0    1    1    0    1    1    1    0   = 01101110₂ = 110₁₀
```

Bit position *i* (counting from the right, starting at 0) encodes the output for neighborhood *i* in binary. So Rule 30 = 00011110₂ means:

```
111→0, 110→0, 101→0, 100→1, 011→1, 010→1, 001→1, 000→0
```

### The Space-Time Diagram

The explorer shows a **space-time diagram**: time flows downward, and each row is one generation. Row 0 (the top) is the initial condition. Each subsequent row is derived from the one above by applying the rule simultaneously to every cell.

This visualization is Wolfram's central tool in *A New Kind of Science* (2002) — a book of ~1,200 pages dedicated largely to exploring what happens when you run all 256 rules from various initial conditions and observe the patterns.

---

## Wolfram's Classification

After extensive exploration, Wolfram proposed classifying CAs (and, by extension, dynamical systems in general) into four behavior classes:

### Class 1 — Fixed Point
The system quickly reaches a uniform state. All cells die or all cells live. Examples: Rule 0 (all die), Rule 255 (all live), Rule 8. Whatever the initial condition, the system converges to a fixed point.

*Analogy: a ball rolling into the bottom of a bowl — all trajectories end at the same place.*

### Class 2 — Periodic / Stable
The system settles into a periodic or spatially localized pattern. Different initial conditions produce different stable patterns, but all are eventually periodic. Examples: Rule 4, Rule 50, Rule 51. Structures form and persist but nothing propagates.

*Analogy: a pendulum oscillating without friction — the state space contains cycles.*

### Class 3 — Chaotic / Aperiodic
The system never settles. The pattern appears random and is sensitive to initial conditions: changing one cell of the initial row can completely alter the distant future. Examples: Rule 30, Rule 22, Rule 86. The patterns are neither uniform nor periodic but show statistical regularities.

*Analogy: turbulent fluid flow — deterministic but unpredictable in practice.*

### Class 4 — Complex
The system produces localized structures that interact in complicated ways. Structures propagate, collide, and create new structures. This is the frontier between order and chaos. Examples: Rule 110, Rule 54. Class 4 systems are rare and disproportionately interesting — Conway's Life also belongs to this class.

*Analogy: life itself — localized structures (organisms) interact and evolve.*

---

## Famous Rules

### Rule 30 — Chaos and Pseudorandomness

Rule 30 begins from a single live cell and immediately produces a pattern that:
- Never becomes periodic
- Passes standard statistical tests for randomness
- Has the property that the center column encodes the first 100,000 digits of an apparently random sequence

Wolfram used Rule 30 as the core of Mathematica's built-in pseudorandom number generator for years. This raises an important conceptual point: **determinism and randomness are not opposites**. Rule 30 is completely deterministic — given the initial state, every future state is fixed. Yet the output is unpredictable in any practical sense. The apparent randomness arises not from any true indeterminism but from computational irreducibility: there is no shortcut to knowing the state at row 1,000,000 other than computing all 1,000,000 rows.

### Rule 90 — The Sierpiński Triangle

Rule 90 is an **additive rule**: the new cell is the XOR (exclusive-or) of its two outer neighbors. XOR is linear over GF(2) (the field with two elements), which means the evolution can be analyzed algebraically.

Starting from a single cell:
- After 2^n steps, the pattern forms a scaled copy of itself (self-similarity)
- The entire pattern is the Sierpiński triangle — a fractal of infinite detail
- Pascal's triangle mod 2 generates the same pattern

Rule 90 shows how fractals can emerge from simple discrete rules without any explicit recursive construction.

### Rule 110 — Turing Completeness

Rule 110 is the most remarkable elementary CA. In its "natural" background state, it produces a complex repeating pattern punctuated by localized structures. These structures:
- Move at different speeds (like "particles" or "gliders")
- Collide and produce new structures
- Can be composed to implement logic gates

Matthew Cook proved in his 1994 PhD thesis (published after a legal dispute in 2004) that Rule 110 is **Turing complete**: given a sufficiently large initial condition, it can simulate any Turing machine. This makes Rule 110 the simplest known Turing-complete system — a one-dimensional, two-state, binary-neighborhood rule that can in principle perform any computation.

The proof is constructive: Cook encoded the structures needed to implement a tag system (itself Turing complete), then showed how to encode any tag system computation in the initial condition of Rule 110.

The philosophical implication is striking: **the threshold of universality is extremely low**. You do not need a complex computer with memory, registers, and a processor. You need a one-dimensional binary tape and a rule that maps three bits to one bit.

### Rule 184 — Traffic Flow

Rule 184 has a direct physical interpretation:
- Cells represent lanes on a one-lane road
- 1 = car present, 0 = empty space
- A car advances (moves right by one cell) if the cell ahead is empty; otherwise it waits

This is exactly the **Nagel–Schreckenberg** one-vehicle-type traffic model. From a random initial condition, Rule 184 reproduces:
- **Free flow**: low density → cars space out, all advance each step
- **Traffic jams**: high density → jams form and propagate backward (leftward) even as individual cars move forward
- **Phase transition**: at density 0.5, the system is exactly at the boundary between free flow and jam

The jam propagation is counterintuitive: a jam can propagate backward at speed c even as the cars that cause it move forward at speed c/2 on average. Rule 184 makes this concrete and visual.

### Rule 150 — Pascal's Triangle

Rule 150 is another additive rule: the new cell is the XOR of all three neighbors (including itself). It produces a self-similar pattern related to Pascal's triangle mod 2 and the Sierpiński triangle, but with a different morphology than Rule 90 due to the center cell's contribution.

---

## Initial Conditions

The explorer offers several initial conditions:

| Condition | Description |
|-----------|-------------|
| Single center cell | The canonical Wolfram starting point; reveals the rule's "natural" behavior |
| Two cells | Two seeds create two expanding patterns that eventually interact |
| Alternating | A background of 01010… tests how the rule handles a periodic input |
| Random (sparse/medium/dense) | Reveals what patterns emerge from "generic" initial conditions |
| All cells alive | The all-1 background; some rules immediately die, others create interesting structure |

For Class 3 rules like Rule 30, the initial condition matters greatly — two similar initial conditions can produce completely different trajectories (sensitive dependence). For Class 1 and 2 rules, all initial conditions lead to the same (or similar) final states.

---

## Save / Load Format

States are saved as `.wca` text files:

```
# Wolfram 1D CA History
rule: 110
width: 200
generations: 500
rows:
0000000000001000000000000000000...  (initial condition)
0000000000011100000000000000000...  (generation 1)
...
```

Each row is a binary string of length `width`. The file captures the entire computed history, so loading and continuing is seamless.

---

## Relationship to Game of Life

Both Rule 110 (1D) and Conway's Life (2D) are:
- **Locally defined**: each cell's update depends only on its immediate neighborhood
- **Turing complete**: capable of universal computation given suitable initial conditions
- **Class 4**: complex, with localized persistent structures

The key difference is dimensionality. In 1D, the space-time diagram is two-dimensional (space along x, time along y) and is easy to visualize. In 2D, the space-time diagram is three-dimensional and harder to display. The 1D case makes it easier to see how structures emerge and interact across time.

For more on the relationship between these systems and the general theory of dynamical systems, see `DYNAMIC_SYSTEMS.md`.
