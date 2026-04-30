# 06 — Genetic Programming: Formula Discoverer

## The Story

A pricing team observes that revenue changes with discount percentage according to some pattern, but the relationship isn't a simple line. GP discovers the mathematical formula directly from data — no manual feature engineering, no assumed model form. The chromosome isn't a bit string or a list of numbers; it's a full executable expression tree.

---

## GP vs Standard GA

Standard GA evolves fixed-length strings. GP evolves **variable-length programs** — trees where each node is an operator or terminal.

```
Expression: (x * x) - (2 * x) + 1  ≡  x² - 2x + 1

Tree:
      +
     / \
    -   1
   / \
  *   *
 / \ / \
x  x 2  x
```

The chromosome is the tree itself. Crossover swaps subtrees; mutation replaces a subtree with a randomly generated one.

---

## Subtree Crossover

```
Parent 1:   (x + 3)         Parent 2:   (x * x)
              ↓ pick subtree from P2: (x * x)
              ↓ replace subtree in P1 at node "3":

Child:      (x + (x * x))   ≡  x + x²
```

Subtrees from two parents can combine to form useful new expressions, similar to how combining useful subroutines creates more powerful programs.

---

## Bloat

A known problem in GP: trees grow larger over generations without proportional fitness improvement. Countermeasures:
- **Depth limit** — discard or simplify trees exceeding max depth
- **Parsimony pressure** — penalise fitness for tree size
- **Size-fair crossover** — prefer donors of similar size

---

## The Power of GP

GP can discover formulas, programs, or strategies that humans did not anticipate. Real applications include:
- Symbolic regression (this example)
- Circuit design (discovering antenna shapes)
- Trading strategy evolution
- Robot controller discovery (Karl Sims's evolved creatures)
- Automatic feature engineering in ML pipelines

---

## Commands

```bash
mvn compile exec:java
mvn test
```
