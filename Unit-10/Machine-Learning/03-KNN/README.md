# 03 — K-Nearest Neighbours: Medical Diagnosis

## The Story

A clinical decision-support system classifies patients into healthy, pre-diabetic, or diabetic based on four blood-panel readings. KNN stores every historical patient record and, for any new patient, finds the k most similar records and returns the majority diagnosis. No training phase — just a memory of past cases.

---

## How KNN Works

```
Query patient q
    ↓
Compute Euclidean distance to every training patient
    ↓
Select k nearest neighbours
    ↓
Return majority class label among those k neighbours
```

**Euclidean distance** in d dimensions:
```
dist(a, b) = √( Σᵢ (aᵢ - bᵢ)² )
```

---

## Choosing k

| k | Behaviour | Risk |
|---|-----------|------|
| 1 | Memorises training data, jagged boundary | High variance — sensitive to noise |
| Large | Very smooth boundary | High bias — may under-fit |
| √m | Rule of thumb for m training samples | Often a good starting point |

The optimal k is chosen by cross-validation on a held-out set.

---

## The Bias–Variance Trade-off

KNN makes the bias–variance trade-off visible and adjustable via a single number. This makes it an excellent teaching model even though it is rarely used at scale (O(m) prediction cost makes it slow on large datasets).

---

## KNN vs Parametric Models

| Property | KNN | Logistic Regression |
|----------|-----|-------------------|
| Training | Store data (O(m)) | Gradient descent (O(m·epochs)) |
| Prediction | O(m) search | O(n) dot product |
| Boundary shape | Arbitrary | Linear only |
| Interpretability | "Similar cases" | Weight magnitudes |
| Memory | O(m) | O(n) |

---

## Commands

```bash
mvn compile exec:java
mvn test
```
