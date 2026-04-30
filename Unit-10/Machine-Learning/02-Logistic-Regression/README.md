# 02 — Logistic Regression: Spam Classifier

## The Story

An email service needs to flag spam automatically. Each email is represented by five numeric features (exclamation mark ratio, uppercase ratio, link count, length, and spam keyword count). Logistic regression learns a decision boundary in that 5D feature space that separates spam from ham.

---

## From Linear to Logistic

Linear regression predicts a real number. For classification we need a probability in [0,1]. The **sigmoid function** squashes any real value into that range:

```
σ(z) = 1 / (1 + e⁻ᶻ)     z = w₀ + w₁x₁ + ... + wₙxₙ

σ(-∞) → 0     σ(0) = 0.5     σ(+∞) → 1
```

---

## Binary Cross-Entropy Loss

Instead of MSE, logistic regression minimises **binary cross-entropy**, which penalises confident wrong predictions exponentially:

```
L = -(1/m) Σ [ yᵢ log(ŷᵢ) + (1-yᵢ) log(1-ŷᵢ) ]
```

The gradient has a beautifully simple form:
```
∂L/∂wⱼ = (1/m) Σ (ŷᵢ - yᵢ) xᵢⱼ
```

This is structurally identical to the MSE gradient — a consequence of the sigmoid + log-loss pairing.

---

## Decision Boundary

Prediction is:
```
class = 1  if σ(w·x) ≥ 0.5
class = 0  otherwise
```

Threshold 0.5 can be adjusted to trade precision against recall — useful when false positives (legitimate mail marked spam) cost more than false negatives.

---

## Logistic vs Linear Regression

| Property | Linear | Logistic |
|----------|--------|---------|
| Output | Any real number | Probability ∈ (0,1) |
| Loss | MSE | Binary cross-entropy |
| Task | Regression | Binary classification |
| Decision boundary | — | Linear hyperplane |

---

## Commands

```bash
mvn compile exec:java
mvn test
```
