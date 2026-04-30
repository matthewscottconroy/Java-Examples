# 11 — Flyweight: The Word Processor

## The Story

You open a 500-page novel in a word processor. The document contains 200,000
characters. Ninety-eight percent of them are in the same font: Times New Roman,
12pt, normal weight. About 1,000 are in **bold** for emphasis. About 500 headings
use Arial 18pt Bold.

If every character object stored its own copy of the font family name, the point
size, and the bold/italic flags, you would allocate those four fields 200,000 times.
Worse, most of those 200,000 copies would be byte-for-byte identical.

The **Flyweight** pattern stores that shared state once. Only the per-character
data that *cannot* be shared — the character itself and its pixel position — is
stored in each object. Everything else is shared.

Result: 200,000 tiny `CharacterGlyph` objects (a char and two ints each) plus
**3** shared `FontStyle` flyweight objects, instead of 200,000 large font objects.

---

## The Problem It Solves

When an application needs to create a very large number of fine-grained objects
whose state is mostly identical, memory cost explodes. Flyweight separates state
into **intrinsic** (shared, immutable — lives in the flyweight) and **extrinsic**
(unique per object — passed in at use time).

---

## Structure

```
FontStyle          ← Flyweight (intrinsic: fontFamily, sizePt, bold, italic)
FontStyleFactory   ← Flyweight Factory (cache: key → FontStyle)
CharacterGlyph     ← Context / client (extrinsic: char, x, y + reference to shared FontStyle)
```

---

## When to Use It

| Situation | Example |
|-----------|---------|
| Millions of similar fine-grained objects | Characters in a document, particles in a game |
| Most object state can be made external | Position / identity vs. appearance |
| Memory cost of object count is prohibitive | Forest of trees, GUI icons |

---

## Commands

```bash
mvn compile exec:java   # run the demo
mvn test                # run the unit tests
```
