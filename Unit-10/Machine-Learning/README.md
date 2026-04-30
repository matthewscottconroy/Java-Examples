# Machine Learning

Eight self-contained Maven modules covering the core algorithms of supervised and unsupervised machine learning. Each module is implemented from scratch in plain Java (no ML libraries), with a real-world scenario, a full test suite, and a README explaining the algorithm's mechanics.

---

## Why Implement ML from Scratch?

Using a library (scikit-learn, Weka) lets you *use* these algorithms. Implementing them from scratch forces you to understand:
- What the model actually optimises
- Why gradient descent works, and when it doesn't
- The connection between loss functions and probability theory
- Where overfitting comes from and how regularisation controls it

---

## Module Index

| # | Module | Algorithm | Scenario | Key Concept |
|---|--------|-----------|----------|-------------|
| 01 | [Linear Regression](01-Linear-Regression/) | OLS via gradient descent | House price predictor | MSE loss, R², feature scaling |
| 02 | [Logistic Regression](02-Logistic-Regression/) | Gradient descent + sigmoid | Spam classifier | Cross-entropy loss, sigmoid, decision threshold |
| 03 | [K-Nearest Neighbours](03-KNN/) | Instance-based learning | Medical diagnosis | Euclidean distance, majority vote, bias-variance |
| 04 | [Decision Tree](04-Decision-Tree/) | Recursive binary splitting | Loan approver | Gini impurity, information gain, overfitting |
| 05 | [Naive Bayes](05-Naive-Bayes/) | Probabilistic generative model | Sentiment analyser | Bayes' theorem, log-space, Laplace smoothing |
| 06 | [K-Means](06-K-Means/) | Unsupervised clustering | Customer segmentation | WCSS, K-Means++, elbow method |
| 07 | [Neural Network](07-Neural-Network/) | MLP + backpropagation | Digit pattern recogniser | Forward/backward pass, ReLU, softmax, He init |
| 08 | [Random Forest](08-Random-Forest/) | Ensemble of decision trees | Credit risk classifier | Bagging, feature subsampling, feature importance |

---

## Taxonomy

```
Machine Learning
│
├── Supervised Learning (labelled training data)
│   │
│   ├── Regression (continuous output)
│   │   └── Linear Regression          → 01-Linear-Regression
│   │
│   └── Classification (discrete output)
│       ├── Parametric (learn a fixed set of parameters)
│       │   ├── Logistic Regression    → 02-Logistic-Regression
│       │   └── Naive Bayes            → 05-Naive-Bayes
│       │
│       ├── Non-parametric (structure grows with data)
│       │   ├── K-Nearest Neighbours   → 03-KNN
│       │   ├── Decision Tree          → 04-Decision-Tree
│       │   └── Random Forest          → 08-Random-Forest
│       │
│       └── Neural (learned representations)
│           └── Multi-Layer Perceptron → 07-Neural-Network
│
└── Unsupervised Learning (no labels)
    └── Clustering
        └── K-Means                    → 06-K-Means
```

---

## Key Themes Across All Modules

### The Bias–Variance Trade-off

Every model balances two sources of error:
- **Bias** — error from wrong assumptions (underfitting)
- **Variance** — error from sensitivity to training data fluctuations (overfitting)

| Model | Bias | Variance | Control |
|-------|------|----------|---------|
| Linear Regression | High | Low | — |
| Deep Decision Tree | Low | High | `maxDepth`, `minSamples` |
| k=1 KNN | Low | High | Increase k |
| Random Forest | Low | Lower than tree | `numTrees`, `maxFeatures` |
| MLP (large) | Low | High | Dropout, regularisation |

### Loss Functions and What They Optimise

| Algorithm | Loss | Geometric interpretation |
|-----------|------|--------------------------|
| Linear Regression | MSE | Minimise squared residuals |
| Logistic Regression | Binary cross-entropy | Maximise log-likelihood of labels |
| MLP | Categorical cross-entropy | Maximise log-likelihood, all classes |
| K-Means | WCSS | Minimise within-cluster variance |
| Decision Tree | Gini impurity | Maximise class separation at each split |

### The Role of Randomness

Several models use randomness to escape local optima or reduce variance:
- **K-Means** — random initialisation (K-Means++ mitigates local optima)
- **Neural Network** — weight initialisation, mini-batch sampling order
- **Random Forest** — bootstrap sampling, random feature subsets
- **Naive Bayes / KNN / Linear / Logistic** — deterministic given data

---

## Running Any Module

```bash
cd <module-name>
mvn compile exec:java   # run the demo
mvn test                # run the test suite
```
