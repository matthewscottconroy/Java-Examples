# 16 — Huffman Coding: Log Compression

## The Story

Log files are dominated by a small set of repeated tokens: `INFO`, `ERROR`, spaces, common field names. A fixed-width encoding (ASCII: 8 bits per character) wastes space on rare characters while not rewarding common ones. Huffman coding assigns shorter bit strings to frequent characters and longer strings to rare ones, reducing the average bits per character — often by 40–60% for typical text.

---

## The Greedy Insight

Build a binary tree where each leaf is a character. The code for a character is the path from the root to its leaf (0 for left, 1 for right). Characters at shallow levels have short codes; characters at deep levels have long codes.

The greedy strategy: always merge the two lowest-frequency nodes. This ensures high-frequency nodes end up near the root (short codes) and low-frequency nodes end up near the leaves (long codes).

---

## Building the Tree

1. Create one leaf node per character, keyed by frequency.
2. Insert all leaves into a min-heap.
3. Repeat until one node remains:
   a. Extract the two lowest-frequency nodes.
   b. Create a parent node with frequency = sum of children.
   c. Re-insert the parent.
4. The remaining node is the root.

```
Frequencies: a=4, b=2, c=1, d=1
               ↓ merge c(1) and d(1)
PQ: [a:4, b:2, cd:2]
               ↓ merge b(2) and cd(2)
PQ: [a:4, bcd:4]
               ↓ merge a(4) and bcd(4)
Root: [abcd:8]

Codes: a=0, b=10, c=110, d=111
```

---

## Prefix-Free Property

No code is a prefix of another. This means decoding is unambiguous: walk the tree bit-by-bit; when you hit a leaf, output the character and return to the root. No delimiter needed.

---

## Optimality

Huffman coding produces the **optimal prefix-free code** for a given set of character frequencies — no other prefix-free code achieves a lower expected bit length. This is the greedy algorithm's guarantee.

It's the foundation of DEFLATE (used in ZIP, gzip, PNG) and many other compression formats.

---

## Commands

```bash
mvn compile exec:java
mvn test
```
