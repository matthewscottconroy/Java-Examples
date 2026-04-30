# 04 — Mutual Recursion

Functions that call each other. The classic definition of "even" and "odd" is the simplest example; this module also shows Hofstadter's chaotic sequences and a recursive-descent tokeniser and balanced-parentheses checker.

## Examples

### `isEven` / `isOdd`
```java
static boolean isEven(int n) { return n == 0 || isOdd(n - 1); }
static boolean isOdd(int n)  { return n != 0 && isEven(n - 1); }
```
Two functions that are only well-defined in terms of each other.

### Hofstadter M and F sequences
```
M(0) = 0,  M(n) = n − F(M(n−1))
F(0) = 1,  F(n) = n − M(F(n−1))
```
Mutually recursive with chaotic (non-monotone) behaviour.

### Tokeniser
Three mutually recursive methods: `tokenise`, `readNumber`, `readWord` — each recognises one token kind and hands back to the dispatcher for the next.

### `isBalanced`
A recursive-descent grammar checker for nested parentheses. Uses exception-based signalling to propagate unmatched-bracket errors up the call stack rather than returning a boolean at every level.

## Run

```bash
mvn exec:java
mvn test
```
