# 01 — Linear Regression: House Price Predictor

## The Story

A real estate platform wants to estimate house prices from three features: square footage, number of bedrooms, and age. Linear regression learns a weighted sum of those features that minimises prediction error over historical sales data — the simplest supervised learning model and the foundation for everything more complex.

---

## The Model

```
ŷ = w₀ + w₁·size + w₂·bedrooms + w₃·age
```

`w₀` is the **bias** (intercept). `w₁, w₂, w₃` are **feature weights**. Training finds the values of these weights that minimise **mean squared error (MSE)** over the training set.

---

## Batch Gradient Descent

Each epoch, the algorithm computes the gradient of MSE with respect to every weight and takes a step in the opposite direction:

```
MSE = (1/m) Σ (ŷᵢ - yᵢ)²

∂MSE/∂wⱼ = (2/m) Σ (ŷᵢ - yᵢ) · xᵢⱼ

wⱼ ← wⱼ - α · ∂MSE/∂wⱼ
```

| Hyperparameter | Role |
|---------------|------|
| `α` (learning rate) | Step size — too large diverges, too small converges slowly |
| `epochs` | Number of full passes over the training set |

---

## Feature Scaling

Gradient descent is sensitive to feature scale. A feature with range 0–2000 (square footage) produces much larger raw gradients than one with range 1–5 (bedrooms), causing slow or unstable training. Normalising all features to [0,1] before training fixes this.

---

## R² (Coefficient of Determination)

```
R² = 1 - SS_res / SS_tot
```

- R² = 1.0 — perfect fit
- R² = 0.0 — model predicts the mean for every input (no better than baseline)
- R² < 0  — model is worse than predicting the mean

---

## Commands

```bash
mvn compile exec:java
mvn test
```
