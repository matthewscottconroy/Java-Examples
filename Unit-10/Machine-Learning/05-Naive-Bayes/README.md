# 05 — Naive Bayes: Sentiment Analyser

## The Story

A product review platform needs to tag reviews as positive, negative, or neutral in real time. Naive Bayes trains in milliseconds, classifies in microseconds, and requires only a small training set — ideal for text where words are the natural features and the vocabulary is large.

---

## Bayes' Theorem

```
P(class | words) ∝ P(class) × Π P(wordᵢ | class)
```

- **P(class)** — prior: fraction of training documents in each class
- **P(word | class)** — likelihood: how often this word appears in class documents
- **∝** — "proportional to" (we only need the relative ranking across classes)

The "naïve" assumption is that words are **conditionally independent given the class**. This is never strictly true (words cluster in topics), but the resulting model is surprisingly effective.

---

## Log-Space Computation

Multiplying many small probabilities causes floating-point underflow. The solution is to work in log-space and add instead of multiply:

```
log P(class | words) ∝ log P(class) + Σ log P(wordᵢ | class)
```

---

## Laplace Smoothing

A word that never appeared in class-A training documents would give P(word|A) = 0, zeroing out the entire product. **Laplace smoothing** adds α to every word count:

```
P(word | class) = (count(word, class) + α) / (total_words_in_class + α × |vocabulary|)
```

With α = 1 no probability is ever exactly zero. Unseen words get a small but non-zero probability.

---

## Naive Bayes vs Logistic Regression for Text

| Property | Naive Bayes | Logistic Regression |
|----------|------------|-------------------|
| Training | Count words (O(m·n)) | Gradient descent (O(m·n·epochs)) |
| High-dimensional input | Handles well | Needs regularisation |
| Small datasets | Works with very little data | Needs more data |
| Calibration | Poor (probabilities skewed) | Well-calibrated |
| Speed | Extremely fast | Slower |

---

## Commands

```bash
mvn compile exec:java
mvn test
```
