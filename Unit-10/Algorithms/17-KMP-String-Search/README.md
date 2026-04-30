# 17 — KMP String Search: Log Pattern Finder

## The Story

A monitoring system needs to scan multi-gigabyte log files for error patterns in real time. The naïve approach — slide the pattern over the text and compare — is O(n·m) and too slow. KMP scans the text exactly once (O(n)) by precomputing a "failure function" that tells it how far to skip when a mismatch occurs, avoiding re-examining characters it has already seen.

---

## Why Naïve Search is Slow

```
Text:    A A A A A A A A B
Pattern: A A A A B

Position 0: A A A A A ≠ B → mismatch at position 4
Naïve: restart from position 1, compare again from scratch.
```

In the worst case (text = "AAA...A", pattern = "AA...AB"), naïve does O(n·m) comparisons.

---

## The Failure Function

The failure function `fail[i]` = length of the longest proper prefix of `pattern[0..i]` that is also a suffix.

```
Pattern: A A B A A B
Index:   0 1 2 3 4 5
fail:    0 1 0 1 2 3
```

`fail[5] = 3` means: the 3-character prefix "AAB" equals the 3-character suffix "AAB" in "AABAAB".

---

## How KMP Uses the Failure Function

When a mismatch occurs after matching `j` characters, we know the last `j` characters of text match the first `j` characters of pattern. Instead of restarting at j=0, we jump to `j = fail[j-1]` — the longest prefix of the already-matched portion that is also a suffix. This portion is "guaranteed to still match."

```
Text:    A B C A B C D
Pattern: A B C A B D
              ^mismatch at position 5 (D≠D... wait)

After matching "ABCAB", mismatch at 'D' vs 'D'... let me redo:
Text:    A B C A B C A B C D
Pattern: A B C A B D
Matched: A B C A B — mismatch at C vs D (position 5)
fail[4] = 2 → "AB" is longest prefix-suffix of "ABCAB"
Jump j=2, continue comparing from text position 5 with pattern position 2.
```

No character in the text is ever compared twice → O(n).

---

## Commands

```bash
mvn compile exec:java
mvn test
```
