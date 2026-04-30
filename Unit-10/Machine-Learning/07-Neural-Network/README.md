# 07 — Neural Network (MLP): Digit Pattern Recogniser

## The Story

A postal sorting system reads handwritten digits from envelopes. A multi-layer perceptron (MLP) learns to recognise 4×4 binary pixel patterns for digits 0–4. Unlike all previous models in this collection, an MLP learns **intermediate representations** — the hidden layer neurons detect edges and strokes that the output layer combines into digit predictions.

---

## Architecture

```
Input layer         Hidden layer        Output layer
(16 pixels)         (12 neurons)        (5 classes, softmax)

 x₁ ─┐
 x₂ ─┤               h₁ ─┐
 x₃ ─┼─ [weights W₁] h₂ ─┼─ [weights W₂] ─ P(digit 0)
 ... ─┤              ...  ─┤               ─ P(digit 1)
 x₁₆─┘               h₁₂─┘               ─ ...
```

Each connection has a learnable weight. Each neuron computes a weighted sum of its inputs, then applies an activation function.

---

## Activation Functions

**ReLU** (hidden layers) — allows gradients to flow while introducing non-linearity:
```
ReLU(z) = max(0, z)
```

**Softmax** (output layer) — converts raw scores into a probability distribution:
```
softmax(zᵢ) = exp(zᵢ) / Σⱼ exp(zⱼ)     sums to 1.0
```

---

## Backpropagation

Training uses the **chain rule** to compute how much each weight contributed to the error:

```
Forward:   x → a₁ → a₂ → loss
Backward:  ∂loss/∂W₂ = ∂loss/∂a₂ · ∂a₂/∂W₂   (output layer)
           ∂loss/∂W₁ = ∂loss/∂a₂ · ∂a₂/∂a₁ · ∂a₁/∂W₁   (hidden layer)
```

Each layer's gradient depends on the layer above it — propagating error "backwards" through the network.

---

## He Initialisation

Weights are sampled from N(0, √(2/fan_in)). This keeps the variance of activations stable across many ReLU layers, preventing the vanishing/exploding gradient problems that plague naive random initialisation.

---

## Why Hidden Layers Matter

XOR cannot be classified by any linear model (logistic regression, linear SVM) — it requires a non-linear boundary. A single hidden layer with enough neurons can approximate any continuous function (**universal approximation theorem**). The MLP in this module demonstrates this by learning XOR with 4 hidden neurons.

---

## Commands

```bash
mvn compile exec:java
mvn test
```
