# 18 — Trie: Search Autocomplete

## The Story

A search bar suggests completions as users type. When someone types "rec", the system needs to quickly find all words starting with that prefix — "recursion", "refactoring", "runtime", etc. A trie is a tree where every path from root to a marked node spells out an inserted word. Autocomplete is just "find the subtree rooted at the last character of the prefix, then collect all paths to marked nodes."

---

## Structure

Each node represents a character. Paths through the tree spell out strings.

```
Words: "app", "apple", "apply", "bat"

root
├── a
│   └── p
│       └── p  [END]
│           ├── l
│           │   ├── e  [END]
│           │   └── y  [END]
└── b
    └── a
        └── t  [END]
```

**Shared prefix** "app" is stored once, not three times.

---

## Operations

| Operation | Time | Description |
|-----------|------|-------------|
| `insert(word)` | O(L) | Walk/create nodes for each character |
| `search(word)` | O(L) | Walk and check the final node is marked |
| `startsWith(prefix)` | O(L) | Walk and check the final node exists |
| `autocomplete(prefix)` | O(L + output) | Walk to prefix, DFS collect all words |

L = length of the word/prefix.

---

## Trie vs HashMap

A `HashMap<String, Value>` gives O(L) average per lookup (due to hashing). A trie also gives O(L), but additionally supports:
- **Prefix queries** — "all words starting with X" — impossible with a HashMap
- **Ordered iteration** — lexicographic DFS
- **Prefix counting** — how many words share a prefix

For pure key-value lookup with no prefix requirements, a HashMap is simpler and often faster in practice.

---

## Space Considerations

Each node uses a `HashMap<Character, Node>` child map. For large alphabets with sparse use, this is efficient (only existing children are stored). For small alphabets (e.g., DNA: 4 bases), a fixed-size array `Node[4]` is faster and simpler.

---

## Real-World Uses

- **Autocomplete / search suggestions** — every major search engine
- **Spell checking** — prefix traversal to find similar words
- **IP routing tables** — longest prefix match on binary tries
- **Compressors** — LZ78 / LZW algorithms build a trie dynamically

---

## Commands

```bash
mvn compile exec:java
mvn test
```
