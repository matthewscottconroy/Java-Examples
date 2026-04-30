# 13 — Longest Common Subsequence: The Diff Tool

## The Story

A code review tool needs to show what changed between two versions of a file. `git diff` highlights insertions and deletions — the unchanged lines are the longest common subsequence of the two files. LCS is also behind DNA sequence alignment, plagiarism detection, and spell-checkers (via edit distance, which is directly derived from LCS).

---

## Subsequence vs Substring

A **subsequence** preserves relative order but allows gaps.  
A **substring** is contiguous.

```
String:      A B C D E
Subsequence: A _ C _ E  → "ACE" ✓
Substring:   A B C      → "ABC" ✓ (contiguous)
"AEC" is not a subsequence (order violated)
```

---

## The DP Recurrence

```
LCS(i, j) = length of LCS of a[0..i-1] and b[0..j-1]

Base cases:  LCS(i, 0) = LCS(0, j) = 0

Recurrence:
  if a[i-1] == b[j-1]:  LCS(i,j) = LCS(i-1, j-1) + 1   (characters match)
  else:                  LCS(i,j) = max(LCS(i-1,j), LCS(i,j-1))  (skip one)
```

The table is filled row by row; the bottom-right cell contains the answer.

```
      ""  B  D  C  A  B  A
""  [  0  0  0  0  0  0  0 ]
A   [  0  0  0  0  1  1  1 ]
B   [  0  1  1  1  1  2  2 ]
C   [  0  1  1  2  2  2  2 ]
B   [  0  1  1  2  2  3  3 ]
D   [  0  1  2  2  2  3  3 ]
A   [  0  1  2  2  3  3  4 ]  ← LCS("ABCBDA", "BDCABA") = 4
B   [  0  1  2  2  3  4  4 ]
```

---

## Edit Distance (Levenshtein)

Closely related: the minimum number of single-character operations (insert, delete, substitute) to transform one string into another.

**Relationship to LCS:**
```
editDistance(a, b) = (len(a) - LCS) + (len(b) - LCS)
                   = len(a) + len(b) - 2 * LCS
```

(This formula gives the number of insertions and deletions only; full edit distance also counts substitutions as 1 operation instead of 2.)

---

## Space Optimization

The full O(m·n) table is needed for reconstruction. For just the **length**, only two rows are needed at any time → O(n) space.

---

## Commands

```bash
mvn compile exec:java
mvn test
```
